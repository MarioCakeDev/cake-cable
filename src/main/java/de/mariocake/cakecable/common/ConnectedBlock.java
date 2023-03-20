package de.mariocake.cakecable.common;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SidedInventory;

import java.util.Optional;

public class ConnectedBlock {
	public BlockEntity blockEntity;

	public Optional<SidedInventory> getSidedInventory() {
		if (blockEntity instanceof SidedInventory sidedInventory) {
			return Optional.of(sidedInventory);
		}
		return Optional.empty();
	}
}
