package com.ghosthandcam.client.mixin;

import com.ghosthandcam.client.GhostRay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow public HitResult hitResult;
	@Shadow public LocalPlayer player;
	@Shadow public ClientLevel level;
	@Shadow @Final public Options options;
	@Shadow public abstract Entity getCameraEntity();

	/** Right-click was held within this many ms ago, so a click is about to be processed. */
	@Unique private static final long GHOSTCAM_GRACE_MS = 200L;
	@Unique private long ghostcam$lastUseDown = 0L;

	/**
	 * After vanilla picks what is under the crosshair, swap in a ghost target when:
	 * the camera is detached from the player (Freecam etc.) and right-click is held.
	 * Left-click (breaking) is deliberately left alone.
	 */
	@Inject(method = "pick(F)V", at = @At("TAIL"))
	private void ghostcam$pick(float partialTicks, CallbackInfo ci) {
		Entity camera = this.getCameraEntity();
		if (camera == null || this.player == null || this.level == null || camera == this.player) {
			return;
		}

		long now = System.currentTimeMillis();
		if (this.options.keyUse.isDown()) {
			this.ghostcam$lastUseDown = now;
		}
		if (now - this.ghostcam$lastUseDown > GHOSTCAM_GRACE_MS) {
			return;
		}

		// Keep entity targets (villagers, item frames...) exactly as vanilla picked them.
		if (this.hitResult instanceof EntityHitResult) {
			return;
		}

		BlockHitResult ghost = GhostRay.cast(this.level, this.player, camera);
		if (ghost != null) {
			this.hitResult = ghost;
		}
	}
}
