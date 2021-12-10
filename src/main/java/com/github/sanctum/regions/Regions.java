package com.github.sanctum.regions;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.command.CommandRegistration;
import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.data.RegionServicesManager;
import com.github.sanctum.labyrinth.data.Registry;
import com.github.sanctum.labyrinth.data.SimpleKeyedValue;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.interfacing.OrdinalProcedure;
import com.github.sanctum.labyrinth.library.Cuboid;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.TypeFlag;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.regions.api.ControlledRegion;
import com.github.sanctum.regions.api.PlayerRegionManager;
import com.github.sanctum.regions.api.SpawnRegionManager;
import com.github.sanctum.regions.api.SubCommand;
import com.github.sanctum.regions.impl.RegionUtility;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.api.Savable;
import com.github.sanctum.regions.impl.PlayerRegion;
import com.github.sanctum.regions.impl.RegionCommandBase;
import com.github.sanctum.regions.impl.SpawnCommandBase;
import com.github.sanctum.regions.impl.SpawnRegion;
import com.github.sanctum.regions.listener.BlockEventListener;
import com.github.sanctum.regions.listener.PlayerEventListener;
import com.github.sanctum.regions.subcommand.InfoCommand;
import com.github.sanctum.regions.subcommand.ListCommand;
import com.github.sanctum.regions.subcommand.NameCommand;
import com.github.sanctum.regions.subcommand.ToolCommand;
import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Regions extends JavaPlugin implements RegionsAPI {

	private final RegionCommandBase regionCommandBase = new RegionCommandBase();
	private final SpawnCommandBase spawnCommandBase = new SpawnCommandBase();
	private final PlayerRegionManager playerRegionManager = new PlayerRegionManager() {
		@Override
		public @Nullable PlayerRegion newRegion(OfflinePlayer owner, SimpleKeyedValue<Location, Location> locations) {
			if (isRegion(locations.getKey()) || isRegion(locations.getValue())) return null;
			return new PlayerRegion(locations.getKey(), locations.getValue(), owner.getUniqueId());
		}

		@Override
		public @Nullable PlayerRegion newRegion(Cuboid.Selection selection) {
			if (isRegion(selection.getPos1()) || isRegion(selection.getPos2())) return null;
			return new PlayerRegion(selection.getPos1(), selection.getPos2(), selection.getPlayer().getUniqueId());
		}

		@Override
		public @NotNull PlayerRegion[] getRegions(OfflinePlayer owner) {
			return Arrays.stream(getPlayerRegions()).filter(r -> r.getOwner().getUniqueId().equals(owner.getUniqueId()) || r.getMembers().contains(owner)).toArray(PlayerRegion[]::new);
		}

		@Override
		public boolean isPlayer(Location location) {
			return Arrays.stream(getPlayerRegions()).anyMatch(playerRegion -> playerRegion.contains(location));
		}
	};
	private final SpawnRegionManager spawnRegionManager = new SpawnRegionManager() {
		@Override
		public @Nullable SpawnRegion newRegion(Location point1, Location point2) {
			if (isRegion(point2) || isRegion(point2)) return null;
			if (getSpawn() != null) throw new IllegalStateException("Spawn region already exists!");
			Location highest;
			Location lowest;
			if (point1.getY() > point2.getY()) {
				highest = point1;
				lowest = point2;
			} else {
				highest = point2;
				lowest = point1;
			}
			highest.setY(250);
			lowest.setY(0);
			return new SpawnRegion(highest, lowest);
		}

		@Override
		public @Nullable SpawnRegion getSpawn() {
			return getSpawnRegions().length == 0 ? null : getSpawnRegions()[0];
		}

		@Override
		public boolean isSpawn(Location location) {
			return Arrays.stream(getSpawnRegions()).anyMatch(playerRegion -> playerRegion.contains(location));
		}
	};

	@Override
	public void onEnable() {
		// Plugin startup logic
		LabyrinthProvider.getInstance().getServicesManager().register(this, this, ServicePriority.High);
		LabyrinthProvider.getService(Service.VENT).subscribeAll(this, new BlockEventListener(), new PlayerEventListener());
		Arrays.stream(Bukkit.getOfflinePlayers()).forEach(RegionUtility::load);
		RegionUtility.load();

		Registry<SubCommand> registry = new Registry<>(SubCommand.class).source(this).pick("com.github.sanctum.regions.subcommand");
		registry.operate().getData().forEach(command -> getCommandBase().registerSubCommand(command));

		CommandRegistration.use(OrdinalProcedure.select(regionCommandBase, 420).cast(() -> Command.class));
		CommandRegistration.use(spawnCommandBase);

	}

	int getCharLength(char c) {
		switch (c) {
			case ':':
			case 'i':
				return 1;
			case 'l':
				return 2;
			case '*':
			case 't':
				return 3;
			case 'f':
			case 'k':
				return 4;
		}
		return 5;
	}

	public PaginatedList<Cuboid.Flag> getFlags(Player player, Region region) {

		Set<Cuboid.Flag> n = region.getFlags().stream().sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).collect(Collectors.toCollection(LinkedHashSet::new));
		return new PaginatedList<>(n)
				.limit(5)
				.start((pagination, page, max) -> {
					for (int i = 0; i < 15; i++) {
						player.sendMessage(" ");
					}
					new FancyMessage("&e" + region.getName() + ": &rFlags").send(player).deploy();
					new FancyMessage("--------------------------------------").color(Color.AQUA).style(ChatColor.STRIKETHROUGH).send(player).deploy();
				})
				.decorate((pagination, f, page, max, placement) -> {
					Message m;
					if (f.isValid()) {

						m = new FancyMessage().then(f.getId() + ";").color(Color.OLIVE).then("Enable").color(f.isEnabled() ? Color.YELLOW : Color.AQUA).hover("Click to enable this flag.").action(() -> {
							f.setEnabled(true);
							Schedule.sync(() -> getFlags(player, region).get(page)).waitReal(1);
						}).then(" ").then("Disable").color(!f.isEnabled() ? Color.YELLOW : Color.AQUA).hover("Click to disallow this flag.").action(() -> {
							f.setEnabled(false);
							Schedule.sync(() -> getFlags(player, region).get(page)).waitReal(1);
						});
						for (Message.Chunk c : m) {
							String text = c.getText();
							if (text.contains(";")) {
								c.replace(";", " &8" + Strings.repeat(".", ((180 - text.replace(";", "").chars().reduce(0, (p, i) -> p + getCharLength((char) i) + 1)) / 2)) + " ");
							}
						}

						m.send(player).deploy();
					} else {
						new FancyMessage("&c&m" + f.getId()).hover("Click to remove me i no longer work. (#").action(() -> {
							region.removeFlag(f);
							Schedule.sync(() -> getFlags(player, region).get(page)).waitReal(1);
						}).send(player).deploy();
					}
				})
				.finish(builder -> builder.setPrefix("&b&m--------------------------------------").setPlayer(player));
	}

	@Override
	public void onDisable() {
		Arrays.stream(getAll()).forEach(Savable::save);
	}

	PlayerRegion[] getPlayerRegions() {
		return RegionServicesManager.getInstance().getAll().stream().filter(r -> r instanceof PlayerRegion).map(PlayerRegion.class::cast).toArray(PlayerRegion[]::new);
	}

	SpawnRegion[] getSpawnRegions() {
		return RegionServicesManager.getInstance().getAll().stream().filter(r -> r instanceof SpawnRegion).map(SpawnRegion.class::cast).toArray(SpawnRegion[]::new);
	}

	@Override
	public @NotNull ItemStack getTool() {
		return Items.edit(edit -> edit.setType(Material.GOLDEN_AXE).setTitle("&6Region &rwand").setLore("&f&oUsed to make cuboid selections.", " ", "&f* Left-click to adjust selection #1", "&f* Right-click to adjust selection #2", "&f* Crouch-click to clear your selection").build());
	}

	@Override
	public @NotNull RegionCommandBase getCommandBase() {
		return regionCommandBase;
	}

	@Override
	public ControlledRegion[] getAll() {
		return RegionServicesManager.getInstance().getAll().stream().filter(r -> r instanceof ControlledRegion).map(ControlledRegion.class::cast).toArray(ControlledRegion[]::new);
	}

	@Override
	public @NotNull PlayerRegionManager getPlayerManager() {
		return playerRegionManager;
	}

	@Override
	public @NotNull SpawnRegionManager getSpawnManager() {
		return spawnRegionManager;
	}

	@Override
	public boolean isRegion(Location location) {
		return RegionServicesManager.getInstance().get(location) != null;
	}

	@Override
	public @Nullable Region getRegion(Location location) {
		return getRegion(location, false);
	}

	@Override
	public @Nullable Region getRegion(Location location, boolean passthrough) {
		return RegionServicesManager.getInstance().get(location, passthrough);
	}

	@Override
	public <T extends ControlledRegion> @Nullable T getRegion(TypeFlag<T> flag, Location location) {
		if (flag.getType() == PlayerRegion.class) {
			if (getPlayerManager().isPlayer(location)) {
				return flag.cast(RegionServicesManager.getInstance().getAll().stream().filter(r -> r instanceof PlayerRegion && r.contains(location)).findFirst().orElse(null));
			}
		}
		if (flag.getType() == SpawnRegion.class) {
			if (getSpawnManager().isSpawn(location)) {
				return flag.cast(RegionServicesManager.getInstance().getAll().stream().filter(r -> r instanceof SpawnRegion && r.contains(location)).findFirst().orElse(null));
			}
		}
		Region reg = RegionServicesManager.getInstance().getAll().stream().filter(r -> flag.getType().isAssignableFrom(r.getClass()) && r.contains(location) && !r.isPassthrough()).findFirst().orElse(null);
		if (reg != null) {
			return flag.cast(reg);
		}
		return null;
	}

}
