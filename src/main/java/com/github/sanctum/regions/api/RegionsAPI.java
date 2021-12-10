package com.github.sanctum.regions.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.library.TypeFlag;
import com.github.sanctum.regions.impl.RegionCommandBase;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RegionsAPI {

	static RegionsAPI getInstance() {
		return LabyrinthProvider.getInstance().getServicesManager().load(RegionsAPI.class);
	}

	@NotNull ItemStack getTool();

	@NotNull RegionCommandBase getCommandBase();

	@NotNull ControlledRegion[] getAll();

	@NotNull PlayerRegionManager getPlayerManager();

	@NotNull SpawnRegionManager getSpawnManager();

	boolean isRegion(Location location);

	@Nullable Region getRegion(Location location);

	@Nullable Region getRegion(Location location, boolean passthrough);

	@Nullable <T extends ControlledRegion> T getRegion(TypeFlag<T> flag, Location location);

}
