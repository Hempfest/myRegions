package com.github.sanctum.regions.api;

import com.github.sanctum.regions.impl.PlayerRegion;
import com.github.sanctum.regions.impl.SpawnRegion;

public interface ControlledRegion extends Savable {

	default boolean isPlayerOwned() {
		return this instanceof PlayerRegion;
	}

	default boolean isSpawnOwned() {
		return this instanceof SpawnRegion;
	}

	default PlayerRegion getAsPlayerRegion() {
		return (PlayerRegion) this;
	}

	default SpawnRegion getAsSpawnRegion() {
		return (SpawnRegion) this;
	}

}