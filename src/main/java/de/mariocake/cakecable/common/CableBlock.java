package de.mariocake.cakecable.common;

import de.mariocake.cakecable.CakeCableMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CableBlock extends Block {
	public static final BooleanProperty NORTH = BooleanProperty.of("north");
	public static final BooleanProperty EAST = BooleanProperty.of("east");
	public static final BooleanProperty SOUTH = BooleanProperty.of("south");
	public static final BooleanProperty WEST = BooleanProperty.of("west");
	public static final BooleanProperty UP = BooleanProperty.of("up");
	public static final BooleanProperty DOWN = BooleanProperty.of("down");

	private static HashMap<BooleanProperty, VoxelShape> connectionShapes = new HashMap<>();

	public static final Identifier IDENTIFIER = CakeCableMod.Identifier("cable_block");
	private static VoxelShape baseShape;

	public CableBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState()
				.with(NORTH, false)
				.with(EAST, false)
				.with(SOUTH, false)
				.with(WEST, false)
				.with(UP, false)
				.with(DOWN, false)
		);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
		super.appendProperties(builder);
	}



	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		updateConnections(state, world, pos);

		world.updateNeighbors(pos, this);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		updateConnections(state, world, pos);
	}

	private void updateConnections(BlockState state, World world, BlockPos pos) {
		state = state.with(NORTH, world.getBlockState(pos.north()).getBlock() instanceof CableBlock)
				.with(EAST, world.getBlockState(pos.east()).getBlock() instanceof CableBlock)
				.with(SOUTH, world.getBlockState(pos.south()).getBlock() instanceof CableBlock)
				.with(WEST, world.getBlockState(pos.west()).getBlock() instanceof CableBlock)
				.with(UP, world.getBlockState(pos.up()).getBlock() instanceof CableBlock)
				.with(DOWN, world.getBlockState(pos.down()).getBlock() instanceof CableBlock);

		world.setBlockState(pos, state);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			player.sendMessage(Text.of("Hello, world!"), false);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		// teleport player in a random direction, before teleporting we need to check if the player will not be stuck in a block

		boolean isStuck = true;
		do{
			double x = player.getX() + world.random.nextInt(10) - 5;
			double y = player.getY() + world.random.nextInt(10) - 5;
			double z = player.getZ() + world.random.nextInt(10) - 5;

			if(world.getBlockState(new BlockPos(x, y, z)).isAir() && world.getBlockState(new BlockPos(x, y + 1, z)).isAir()){
				//player.teleport(x, y, z);
				player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
				isStuck = false;
			}
		}while (isStuck);
		super.onBreak(world, pos, state, player);

		world.updateNeighbors(pos, this);
	}

	static {
		// create the shapes for the connections
		float blockSize = 6f/16f;
		float bottomLeft = (1f - blockSize) / 2f;
		float topRight = bottomLeft + blockSize;

		connectionShapes.put(NORTH, VoxelShapes.cuboid(bottomLeft, bottomLeft, 0, topRight, topRight, bottomLeft));
		connectionShapes.put(EAST, VoxelShapes.cuboid(topRight, bottomLeft, bottomLeft, 1, topRight, topRight));
		connectionShapes.put(SOUTH, VoxelShapes.cuboid(bottomLeft, bottomLeft, topRight, topRight, topRight, 1));
		connectionShapes.put(WEST, VoxelShapes.cuboid(0, bottomLeft, bottomLeft, bottomLeft, topRight, topRight));
		connectionShapes.put(UP, VoxelShapes.cuboid(bottomLeft, topRight, bottomLeft, topRight, 1, topRight));
		connectionShapes.put(DOWN, VoxelShapes.cuboid(bottomLeft, 0, bottomLeft, topRight, bottomLeft, topRight));

		baseShape = VoxelShapes.cuboid(bottomLeft, bottomLeft, bottomLeft, topRight, topRight, topRight);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {

		List<VoxelShape> shapesToCombine = new ArrayList<>();

		BooleanProperty[] directions = {NORTH, EAST, SOUTH, WEST, UP, DOWN};
		for(BooleanProperty direction : directions){
			if(state.get(direction)){
				shapesToCombine.add(connectionShapes.get(direction));
			}
		}

		return VoxelShapes.union(baseShape, shapesToCombine.toArray(VoxelShape[]::new));
	}
}
