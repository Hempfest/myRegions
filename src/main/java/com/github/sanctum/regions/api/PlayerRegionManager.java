package com.github.sanctum.regions.api;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.SimpleKeyedValue;
import com.github.sanctum.labyrinth.library.Cuboid;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.permissions.Permissions;
import com.github.sanctum.regions.impl.PlayerRegion;
import java.util.Arrays;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerRegionManager {

	@Nullable
	public abstract PlayerRegion newRegion(OfflinePlayer owner, SimpleKeyedValue<Location, Location> locations);

	@Nullable
	public abstract PlayerRegion newRegion(Cuboid.Selection selection);

	@NotNull
	public abstract PlayerRegion[] getRegions(OfflinePlayer owner);

	public abstract boolean isPlayer(Location location);

	public @Nullable PlayerRegion getRegion(Location location) {
		return RegionsAPI.getInstance().getRegion(() -> PlayerRegion.class, location);
	}

	public long getOwnedBlocks(OfflinePlayer player) {
		long count = 0;
		for (PlayerRegion reg : getRegions(player)) {
			count += reg.getTotalBlocks();
		}
		return count;
	}

	public long getMaxBlocks(OfflinePlayer player) {
		long max = 0;
		Permissions permissions = LabyrinthProvider.getInstance().getServicesManager().load(Permissions.class);
		if (permissions == null) return 0;
		if (player.isOp() || permissions.getUser(player).getInheritance().test("region.cap.infinite")) return 999999999;
		String node = Arrays.stream(permissions.getUser(player).getPermissions()).filter(s -> s.contains("region.cap")).findFirst().orElseGet(() -> Arrays.stream(permissions.getUser(player).getGroup().getPermissions()).filter(s -> s.contains("region.cap")).findFirst().orElse(null));
		if (node != null) {
			String l = node.substring(12);
			if (StringUtils.use(l).isLong()) {
				max = Long.parseLong(l);
			}
		}
		return max;
	}

}
