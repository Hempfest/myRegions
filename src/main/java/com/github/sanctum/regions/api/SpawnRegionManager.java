package com.github.sanctum.regions.api;

import com.github.sanctum.regions.impl.SpawnRegion;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public abstract class SpawnRegionManager {

	@Nullable public abstract SpawnRegion newRegion(Location point1, Location point2);

	@Nullable public abstract SpawnRegion getSpawn();

	public abstract boolean isSpawn(Location location);

}
