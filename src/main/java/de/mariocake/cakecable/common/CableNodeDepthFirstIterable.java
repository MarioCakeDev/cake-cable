package de.mariocake.cakecable.common;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CableNodeDepthFirstIterable implements ICableNodeIterable {
	private final CableNode root;

	private Direction[] directionOrder = getDefaultDirectionOrder();

	private Direction[] getDefaultDirectionOrder() {
		return Direction.values();
	}

	public void setDirectionOrder(Direction[] directionOrder) {
		Set<Direction> set = new HashSet<>(Arrays.asList(directionOrder));
		if (set.size() != directionOrder.length) {
			throw new IllegalArgumentException("directionOrder must not contain duplicates");
		}
		if (set.size() != 6) {
			throw new IllegalArgumentException("directionOrder must contain exactly 6 elements");
		}
		this.directionOrder = directionOrder;
	}

	public CableNodeDepthFirstIterable(CableNode root) {
		this.root = root;
	}

	@NotNull
	@Override
	public Iterator<CableNode> iterator() {
		return new CableNodeDepthFirstIterator();
	}

	private class CableNodeDepthFirstIterator implements Iterator<CableNode> {
		private CableNode current = root;
		private final Queue<CableNode> queue = new java.util.LinkedList<>();
		private final Set<CableNode> visited = new HashSet<>();

		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public CableNode next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			CableNode result = current;
			for (Direction direction : directionOrder) {
				CableNode node = current.get(direction);
				if (node != null && !visited.contains(node)) {
					queue.add(node);
				}
			}
			visited.add(current);
			current = queue.poll();

			return result;
		}
	}
}
