package de.mariocake.cakecable.common;

import de.mariocake.cakecable.CakeCableMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CableEntity extends BlockEntity {

	private CableNetwork network;

	public CableEntity(BlockPos pos, BlockState state) {
		super(CakeCableMod.CABLE_ENTITY, pos, state);
	}

	public CableNetwork getNetwork() {
		return network;
	}

	public void setNetwork(CableNetwork cableNetwork) {
		this.network = cableNetwork;
	}
}
