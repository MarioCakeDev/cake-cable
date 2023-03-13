package de.mariocake.cakecable.common;

import de.mariocake.cakecable.CakeCableMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CableBlock extends Block {
	public static final Identifier IDENTIFIER = CakeCableMod.Identifier("cable_block");

	public CableBlock(Settings settings) {
		super(settings);
	}



	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			player.sendMessage(Text.of("Hello, world!"), false);
		}

		return ActionResult.SUCCESS;
	}
}
