package de.mariocake.cakecable.common;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;

import java.util.Optional;

public class ConnectedBlock {
	public BlockEntity blockEntity;

	public Optional<SidedInventory> getSidedInventory() {
		if (blockEntity instanceof SidedInventory) {
			return Optional.of((SidedInventory) blockEntity);
		}
		return Optional.empty();
	}
}
