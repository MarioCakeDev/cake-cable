package de.mariocake.cakecable.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class CableNode {
	public CableEntity cableEntity;

	public CableNode(CableEntity cableEntity) {
		this.cableEntity = cableEntity;
	}

	@Nullable
	public CableNode north;

	@Nullable
	public CableNode east;

	@Nullable
	public CableNode south;

	@Nullable
	public CableNode west;

	@Nullable
	public CableNode up;

	@Nullable
	public CableNode down;

	@Nullable
	public CableNode get(Direction direction) {
		return switch (direction) {
			case NORTH -> north;
			case EAST -> east;
			case SOUTH -> south;
			case WEST -> west;
			case UP -> up;
			case DOWN -> down;
		};
	}

	public void set(Direction direction, CableNode other) {
		BlockPos otherPosition = other.cableEntity.getPos();
		BlockPos thisPosition = cableEntity.getPos();
		BlockPos expectedPosition = thisPosition.offset(direction);

		if (!otherPosition.equals(expectedPosition)) {
			// node at position [x,y,z] need to be adjacent to this node at position [x,y,z]
			throw new IllegalArgumentException("node at position " + otherPosition + " need to be adjacent in direction " + direction + " to this node at position " + thisPosition + ". Expected position " + expectedPosition + " but got " + otherPosition);
		}

		switch (direction) {
			case NORTH -> north = other;
			case EAST -> east = other;
			case SOUTH -> south = other;
			case WEST -> west = other;
			case UP -> up = other;
			case DOWN -> down = other;
		}
	}

	public void remove(Direction direction) {
		switch (direction) {
			case NORTH -> north = null;
			case EAST -> east = null;
			case SOUTH -> south = null;
			case WEST -> west = null;
			case UP -> up = null;
			case DOWN -> down = null;
		}
	}

	public CableNode[] getAdjacentNodes() {
		return Arrays.stream(Direction.values())
			.map(this::get)
			.filter(Objects::nonNull)
			.toArray(CableNode[]::new);
	}
}
