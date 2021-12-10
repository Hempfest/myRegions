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
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerRegion extends Region implements ControlledRegion {

	private static final FileList list = FileList.search(JavaPlugin.getPlugin(Regions.class));

	public PlayerRegion(Location point1, Location point2, UUID owner) {
		super(point1, point2, JavaPlugin.getPlugin(Regions.class), HUID.randomID());
		setOwner(owner);
	}

	public PlayerRegion(Location point1, Location point2, UUID owner, HUID id, List<String> members) {
		super(point1, point2, JavaPlugin.getPlugin(Regions.class), id);
		setOwner(owner);
		for (String mem : members) {
			addMember(Bukkit.getOfflinePlayer(UUID.fromString(mem)));
		}
	}

	@Override
	public @NotNull OfflinePlayer getOwner() {
		return super.getOwner();
	}

	@Override
	public void save() {
		FileManager user = list.get(getOwner().getUniqueId().toString(), "Users", FileType.JSON);
		Node region = user.getRoot().getNode(getId().toString());
		Node name = region.getNode("name");
		Node owner = region.getNode("owner");
		Node loc1 = region.getNode("pos1");
		Node loc2 = region.getNode("pos2");
		Node members = region.getNode("members");
		Node flags = region.getNode("flags");
		name.set(getName());
		owner.set(getOwner().getUniqueId().toString());
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
			FileManager user = list.get(getOwner().getUniqueId().toString(), "Users", FileType.JSON);
			Node region = user.getRoot().getNode(getId().toString());
			region.delete();
			JavaPlugin.getPlugin(Regions.class).getLogger().info("- Deleted region " + '"' + getId().toString() + "(" + getName() + ")" + '"');
		}
	}
}
