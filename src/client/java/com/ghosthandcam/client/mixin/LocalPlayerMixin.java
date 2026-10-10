package com.ghosthandcam.client.mixin;

import com.ghosthandcam.client.GhostRay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * While the camera is detached (Freecam), the world you host lets your real body
 * be up to 64 blocks from whatever the camera uses. The camera itself still only
 * reaches a normal distance. When the camera is not detached everything is normal.
 *
 * The server is what checks reach, so its copy of your player is the one raised.
 * The client copy is always kept at the normal default, so the camera's own reach
 * never grows (the server syncs its value to the client, which is undone here).
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Inject(method = "tick()V", at = @At("TAIL"))
	private void ghostcam$reach(CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer self = (LocalPlayer) (Object) this;
		// Freecam's camera entity can also be a LocalPlayer; only touch the real one.
		if (mc.player != self) {
			return;
		}

		Entity camera = mc.getCameraEntity();
		boolean detached = camera != null && camera != self;

		// Client side: always the normal default.
		ghostcam$set(self, ghostcam$normalReach());

		// Server side (only exists when you host, e.g. singleplayer or Essential).
		IntegratedServer server = mc.getSingleplayerServer();
		if (server != null) {
			double target = detached ? GhostRay.BODY_RANGE : ghostcam$normalReach();
			// Server objects must only be changed from the server thread.
			server.execute(() -> {
				ServerPlayer serverPlayer = server.getPlayerList().getPlayer(self.getUUID());
				if (serverPlayer != null) {
					ghostcam$set(serverPlayer, target);
				}
			});
		}
	}

	@Unique
	private static double ghostcam$normalReach() {
		return Attributes.BLOCK_INTERACTION_RANGE.value().getDefaultValue();
	}

	@Unique
	private static void ghostcam$set(Player player, double value) {
		AttributeInstance range = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (range != null && range.getBaseValue() != value) {
			range.setBaseValue(value);
		}
	}
}
