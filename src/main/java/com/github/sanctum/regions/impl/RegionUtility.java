package com.github.sanctum.regions.impl;

import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.data.RegionFlag;
import com.github.sanctum.labyrinth.library.Cuboid;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.regions.Regions;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionUtility {

	private static final FileList list = FileList.search(JavaPlugin.getPlugin(Regions.class));
	private static RegionUtility regionUtility;

	RegionUtility() {}

	public static RegionUtility getInstance() {
		return regionUtility != null ? regionUtility : (regionUtility = new RegionUtility());
	}

	public static void load() {
		FileManager spawn = list.get("spawn", null, FileType.JSON);
		for (String regionId : spawn.getRoot().getKeys(false)) {
			String name = spawn.getRoot().getNode(regionId).getNode("name").toPrimitive().getString();
			List<String> members = spawn.getRoot().getNode(regionId).getNode("members").toPrimitive().getStringList();
			List<Cuboid.Flag> toAdd = new ArrayList<>();
			Location spawnPoint = spawn.getRoot().getNode(regionId).getNode("spawn").toBukkit().getLocation();
			Location start = spawn.getRoot().getNode(regionId).getNode("pos1").toBukkit().getLocation();
			Location end = spawn.getRoot().getNode(regionId).getNode("pos2").toBukkit().getLocation();
			for (String flag : spawn.getRoot().getNode(regionId).getNode("flags").getKeys(false)) {
				Cuboid.Flag copy = new RegionFlag(flag);
				copy.setEnabled(spawn.getRoot().getNode(regionId).getNode("flags").getNode(flag).toPrimitive().getBoolean());
				toAdd.add(copy);
			}
			SpawnRegion reg = new SpawnRegion(start, end, HUID.fromString(regionId), members);
			reg.setName(name);
			if (spawnPoint != null) {
				reg.setSpawnpoint(spawnPoint);
			}
			for (Cuboid.Flag t : toAdd) {
				if (reg.hasFlag(t)) {
					reg.getFlag(t.getId()).get().setEnabled(t.isEnabled());
				} else {
					reg.addFlag(t);
				}
			}
			Deployable.of(reg, Region::load).queue(TimeUnit.SECONDS.toMillis(2));
		}
	}

	public static void load(OfflinePlayer player) {
		FileManager user = list.get(player.getUniqueId().toString(), "Users", FileType.JSON);
		for (String regionId : user.getRoot().getKeys(false)) {
			String name = user.getRoot().getNode(regionId).getNode("name").toPrimitive().getString();
			String owner = user.getRoot().getNode(regionId).getNode("owner").toPrimitive().getString();
			List<String> members = user.getRoot().getNode(regionId).getNode("members").toPrimitive().getStringList();
			List<Cuboid.Flag> toAdd = new ArrayList<>();
			Location start = user.getRoot().getNode(regionId).getNode("pos1").toBukkit().getLocation();
			Location end = user.getRoot().getNode(regionId).getNode("pos2").toBukkit().getLocation();
			for (String flag : user.getRoot().getNode(regionId).getNode("flags").getKeys(false)) {
				Cuboid.Flag copy = new RegionFlag(flag);
				copy.setEnabled(user.getRoot().getNode(regionId).getNode("flags").getNode(flag).toPrimitive().getBoolean());
				toAdd.add(copy);
			}
			PlayerRegion reg = new PlayerRegion(start, end, UUID.fromString(owner), HUID.fromString(regionId), members);
			reg.setName(name);
			for (Cuboid.Flag t : toAdd) {
				if (reg.hasFlag(t)) {
					reg.getFlag(t.getId()).get().setEnabled(t.isEnabled());
				} else {
					reg.addFlag(t);
				}
			}
			Deployable.of(reg, Region::load).queue(TimeUnit.SECONDS.toMillis(2));
		}
	}

	FileManager checkConfig() {
		FileManager manager = list.get("config", null, FileType.JSON);
		if (!manager.getRoot().exists()) {
			list.copy("config.data", manager);
			manager.getRoot().reload();
		}
		return manager;
	}

	public String getMessage(String path) {
		String result = checkConfig().read(c -> c.getString("Messages." + path));
		if (result != null) {
			return StringUtils.use(result).translate();
		} else return null;
	}

	public String getMessage(String path, OfflinePlayer target) {
		String result = checkConfig().read(c -> c.getString("Messages." + path));
		if (result != null) {
			return StringUtils.use(result).translate(target);
		} else return null;
	}

}
