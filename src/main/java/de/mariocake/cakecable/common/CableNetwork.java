package de.mariocake.cakecable.common;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class CableNetwork {

	private final UUID uuid = UUID.randomUUID();

	CableNode root;
	Dictionary<BlockPos, CableNode> cables;

	public static void initializeNetwork(CableEntity cableEntity) {
		if(cableEntity.getNetwork() != null){
			throw new IllegalArgumentException("CableEntity already has a network");
		}

		CableNetwork network = new CableNetwork();
		CableNetwork.initializeNetwork(cableEntity, Objects.requireNonNull(cableEntity.getWorld()), network);
		network.reconnectFromRoot();
	}

	private static void initializeNetwork(CableEntity cableEntity, World world, CableNetwork network){
		network.addCable(cableEntity);

		BlockPos pos = cableEntity.getPos();

		for (Direction direction : Direction.values()) {
			BlockPos blockPositionToCheck = pos.offset(direction);
			if (!(world.getBlockEntity(blockPositionToCheck) instanceof CableEntity cableEntityToCheck)) {
				continue;
			}

			if(cableEntityToCheck.getNetwork() == null){
				CableNetwork.initializeNetwork(cableEntityToCheck, cableEntity.getWorld(), network);
				continue;
			}

			if (cableEntityToCheck.getNetwork() != network) {
				network.mergeFrom(cableEntityToCheck.getNetwork(), pos);
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other){
			return true;
		}
		if (other == null || getClass() != other.getClass()){
			return false;
		}

		CableNetwork that = (CableNetwork) other;

		return uuid.equals(that.uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	public void addCable(CableEntity cableEntity) {
		var newCableNode = new CableNode(cableEntity);
		if (root == null) {
			root = newCableNode;
			cables = new Hashtable<>();
		}else {
			for (Direction direction : Direction.values()) {
				BlockPos blockPositionToCheck = cableEntity.getPos().offset(direction);
				if (cables.get(blockPositionToCheck) != null) {
					CableNode nodeInNetwork = cables.get(blockPositionToCheck);
					nodeInNetwork.set(direction.getOpposite(), newCableNode);
					newCableNode.set(direction, nodeInNetwork);
				}
			}
		}

		cableEntity.setNetwork(this);
		cables.put(cableEntity.getPos(), newCableNode);
	}

	public void mergeFrom(CableNetwork other, BlockPos mergePoint) {
		CableNode thisRoot = cables.get(mergePoint);
		if (thisRoot == null) {
			throw new IllegalArgumentException("mergePoint is not a valid merge point");
		}

		for (Direction direction : Direction.values()) {
			BlockPos blockPositionToCheck = mergePoint.offset(direction);
			if (other.cables.get(blockPositionToCheck) != null) {
				CableNode node = other.cables.get(blockPositionToCheck);
				node.set(direction.getOpposite(), thisRoot);
				thisRoot.set(direction, node);
			}
		}
	}

	public void removeCable(CableEntity cableEntity) {
		CableNode removedNode = cables.remove(cableEntity.getPos());

		if (removedNode == null) {
			throw new IllegalArgumentException("cableEntity is not part of this network");
		}

		for (Direction direction : Direction.values()) {
			CableNode adjacentNode = removedNode.get(direction);
			if (adjacentNode != null) {
				adjacentNode.remove(direction.getOpposite());
			}
			removedNode.remove(direction);
		}

		cableEntity.setNetwork(null);
	}

	public CableNetwork[] split(BlockPos splitPoint) {
		CableNode splitNode = cables.get(splitPoint);
		if (splitNode == null) {
			throw new IllegalArgumentException("splitPoint is not a valid split point");
		}

		CableNode[] adjacentNodes = splitNode.getAdjacentNodes();
		removeCable(splitNode.cableEntity);

		List<Set<CableNode>> connectedNodeSets = new ArrayList<>();
		for (CableNode adjacentNode : adjacentNodes) {
			if(connectedNodeSets.stream().anyMatch(set -> set.contains(adjacentNode))){
				continue;
			}

			var iterable = new CableNodeDepthFirstIterable(adjacentNode);
			var connectedNodes = new HashSet<CableNode>();
			iterable.forEach(connectedNodes::add);
			connectedNodeSets.add(connectedNodes);
		}

		if(connectedNodeSets.size() == 1){
			return new CableNetwork[]{this};
		}

		var networks = new CableNetwork[connectedNodeSets.size()];
		int index = 0;
		for(Set<CableNode> connectedNodeSet : connectedNodeSets){
			var network = new CableNetwork();
			network.setNewRoot(connectedNodeSet.stream().findFirst().orElseThrow());
			networks[index++] = network;
		}

		return networks;
	}

	private void setNewRoot(CableNode newRoot) {
		root = newRoot;
		cables = new Hashtable<>();
		var iterable = new CableNodeDepthFirstIterable(root);
		for (CableNode node : iterable) {
			cables.put(node.cableEntity.getPos(), node);
			node.cableEntity.setNetwork(this);
		}
	}

	public void reconnectFromRoot() {
		this.setNewRoot(root);
	}
}

