package de.mariocake.cakecable.common;

import de.mariocake.cakecable.CakeCableMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CableBlock extends Block implements BlockEntityProvider {
	public static final BooleanProperty NORTH = BooleanProperty.of("north");
	public static final BooleanProperty EAST = BooleanProperty.of("east");
	public static final BooleanProperty SOUTH = BooleanProperty.of("south");
	public static final BooleanProperty WEST = BooleanProperty.of("west");
	public static final BooleanProperty UP = BooleanProperty.of("up");
	public static final BooleanProperty DOWN = BooleanProperty.of("down");

	private static final Logger LOGGER = LoggerFactory.getLogger(CableBlock.class);

	private static final HashMap<BooleanProperty, VoxelShape> connectionShapes = new HashMap<>();

	public static final Identifier IDENTIFIER = CakeCableMod.Identifier("cable_block");
	private static final VoxelShape baseShape;

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

		CableEntity cableEntity = (CableEntity)world.getBlockEntity(pos);

		addToNetworkAndMergeAdjacentNetworks(world, pos, cableEntity);

		CableNetwork network = Objects.requireNonNull(cableEntity).getNetwork();

		Objects.requireNonNull(placer).sendSystemMessage(Text.of("Network size: " + network.cables.size()));

		world.updateNeighbors(pos, this);
	}

	private void addToNetworkAndMergeAdjacentNetworks(World world, BlockPos pos, CableEntity cableEntity) {
		Set<CableNetwork> networks = getAdjacentCableNetworks(world, pos);
		switch (networks.size()) {
			case 0 -> {
				CableNetwork network = new CableNetwork();
				network.addCable(cableEntity);
			}
			case 1 -> {
				CableNetwork network = networks.iterator().next();
				network.addCable(cableEntity);
			}
			default -> {
				CableNetwork network = new CableNetwork();
				network.addCable(cableEntity);
				for (CableNetwork cableNetwork : networks) {
					network.mergeFrom(cableNetwork, cableEntity.getPos());
				}
				network.reconnectFromRoot();
			}
		}
	}

	private Set<CableNetwork> getAdjacentCableNetworks(World world, BlockPos pos) {
		Set<CableNetwork> networks = new HashSet<>();

		Direction[] directions = Direction.values();
		for (Direction direction : directions) {
			BlockPos neighborPos = pos.offset(direction);
			BlockEntity blockEntity = world.getBlockEntity(neighborPos);
			if (blockEntity instanceof CableEntity cableEntity) {
				networks.add(cableEntity.getNetwork());
			}
		}

		return networks;
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
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);

		super.onBreak(world, pos, state, player);

		world.updateNeighbors(pos, this);

		CableEntity cableEntity = (CableEntity)world.getBlockEntity(pos);
		CableNetwork network = Objects.requireNonNull(cableEntity).getNetwork();

		CableNetwork[] networks = network.split(pos);
		for(int i = 0; i < networks.length; i++) {
			CableNetwork cableNetwork = networks[i];
			player.sendSystemMessage(Text.of("Network " + i + " size: " + cableNetwork.cables.size()));
		}
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

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CableEntity(pos, state);
	}
}
