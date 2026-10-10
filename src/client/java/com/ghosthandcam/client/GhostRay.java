package com.ghosthandcam.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Casts a ray from the detached camera that passes through every block that
 * can't be used, and stops at the first block that can (chest, door, lever...).
 */
public final class GhostRay {
	/**
	 * How far your real body may be from what the camera is using, while the camera
	 * is detached. 64 is the game's maximum for the block interaction range attribute.
	 */
	public static final double BODY_RANGE = 64.0;

	private GhostRay() {}

	/**
	 * Returns the first usable block under the camera crosshair, within the normal
	 * reach of the camera, or null.
	 */
	public static BlockHitResult cast(ClientLevel level, LocalPlayer player, Entity camera) {
		// The camera itself only reaches as far as a normal player (the client-side
		// range is kept at its default; see LocalPlayerMixin).
		double cameraReach = player.blockInteractionRange();

		Vec3 start = camera.getEyePosition(1.0F);
		Vec3 dir = camera.getViewVector(1.0F);
		Vec3 end = start.add(dir.scale(cameraReach));

		Vec3 from = start;
		for (int i = 0; i < 256; i++) {
			BlockHitResult hit = level.clip(new ClipContext(
					from, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, camera));
			if (hit.getType() == HitResult.Type.MISS) {
				return null;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (isUsable(state)) {
				return withinBodyRange(player, pos) ? hit : null;
			}

			// Not usable: slide the ray start a little past the hit and try again.
			from = hit.getLocation().add(dir.scale(0.1));
			if (from.distanceToSqr(start) >= cameraReach * cameraReach) {
				return null;
			}
		}
		return null;
	}

	/** The server checks reach from your real body, so don't offer targets it would ignore. */
	private static boolean withinBodyRange(LocalPlayer player, BlockPos pos) {
		return player.getEyePosition().distanceTo(Vec3.atCenterOf(pos)) <= BODY_RANGE - 1.0;
	}

	private static boolean isUsable(BlockState state) {
		if (state.hasBlockEntity()) {
			return true; // chests, barrels, furnaces, signs, beds, hoppers, ...
		}
		return state.is(BlockTags.BUTTONS)
				|| state.is(BlockTags.DOORS)
				|| state.is(BlockTags.TRAPDOORS)
				|| state.is(BlockTags.FENCE_GATES)
				|| state.is(BlockTags.ANVIL)
				|| state.is(Blocks.LEVER)
				|| state.is(Blocks.CRAFTING_TABLE)
				|| state.is(Blocks.CARTOGRAPHY_TABLE)
				|| state.is(Blocks.LOOM)
				|| state.is(Blocks.SMITHING_TABLE)
				|| state.is(Blocks.STONECUTTER)
				|| state.is(Blocks.GRINDSTONE)
				|| state.is(Blocks.NOTE_BLOCK)
				|| state.is(Blocks.REPEATER)
				|| state.is(Blocks.COMPARATOR);
	}
}
