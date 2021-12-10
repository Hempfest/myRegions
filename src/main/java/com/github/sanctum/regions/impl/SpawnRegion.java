package com.github.sanctum.regions.impl;

import com.github.sanctum.labyrinth.data.FileList;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.data.Node;
import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.regions.Regions;
import com.github.sanctum.regions.api.ControlledRegion;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnRegion extends Region implements ControlledRegion {

	private static final FileList list = FileList.search(JavaPlugin.getPlugin(Regions.class));

	private Location spawn_point;

	public SpawnRegion(Location point1, Location point2) {
		super(point1, point2, JavaPlugin.getPlugin(Regions.class), HUID.fromString("XXXX-XXXX-XXXX"));
		setName("spawn");
	}

	public SpawnRegion(Location point1, Location point2, HUID id, List<String> members) {
		super(point1, point2, JavaPlugin.getPlugin(Regions.class), id);
		for (String mem : members) {
			addMember(Bukkit.getOfflinePlayer(UUID.fromString(mem)));
		}
	}

	public Location getSpawnpoint() {
		return spawn_point;
	}

	public void setSpawnpoint(Location spawnpoint) {
		this.spawn_point = spawnpoint;
	}

	@Override
	public void save() {
		FileManager spawn = list.get("spawn", null, FileType.JSON);
		Node region = spawn.getRoot().getNode(getId().toString());
		Node name = region.getNode("name");
		Node loc1 = region.getNode("pos1");
		Node loc2 = region.getNode("pos2");
		Node spawnPoint = region.getNode("spawn");
		Node members = region.getNode("members");
		Node flags = region.getNode("flags");
		name.set(getName());
		if (getSpawnpoint() != null) {
			spawnPoint.set(getSpawnpoint());
		}
		loc1.set(getStartingPoint());
		loc2.set(getEndingPoint());
		members.set(getMembers().stream().map(player -> player.getUniqueId().toString()).collect(Collectors.toList()));
		for (Flag flag : getFlags()) {
			flags.getNode(flag.getId()).set(String.valueOf(flag.isEnabled()));
		}
		region.save();
	}

	@Override
	public void delete() {
		if (remove()) {
			list.get("spawn", null, FileType.JSON).getRoot().delete();
		}
	}
}
