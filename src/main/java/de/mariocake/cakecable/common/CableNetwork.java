package de.mariocake.cakecable.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CableNetwork {

	private final UUID uuid = UUID.randomUUID();

	List<CableEntity> cables = new ArrayList<>();

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
		cables.add(cableEntity);
		cableEntity.setNetwork(this);
	}

	public void merge(CableNetwork other) {
		for (CableEntity cableEntity : other.cables) {
			addCable(cableEntity);
		}
	}

	public void removeCable(CableEntity cableEntity) {
		cables.remove(cableEntity);
		cableEntity.setNetwork(null);
	}
}

