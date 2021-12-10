package com.github.sanctum.regions.impl;

import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.FancyMessageChain;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Cuboid;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.task.TaskMonitor;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.regions.Regions;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.api.SubCommand;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class SpawnCommandBase extends Command {

	//private final Set<SubCommand> subCommands = new HashSet<>();

	public SpawnCommandBase() {
		super("spawn");
		setAliases(Collections.singletonList("setspawn"));
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

		if (sender instanceof Player) {
			Player p = (Player) sender;
			SpawnRegion spawn = RegionsAPI.getInstance().getSpawnManager().getSpawn();
			if (args.length == 0) {
				if (commandLabel.equalsIgnoreCase("spawn")) {
					if (spawn != null) {
						if (spawn.getSpawnpoint() != null) {
							p.teleport(spawn.getSpawnpoint());
							TaskScheduler.of(() -> {
								Region.Resident resident = Region.Resident.get(p);
								resident.setPastSpawn(false);
								resident.setSpawnTagged(true);
							}).scheduleLater(1);
						}
					} else {
						Mailer.empty(p).chat("&cSpawn doesn't currently exist!").queue();
					}
				}
				if (commandLabel.equalsIgnoreCase("setspawn")) {
					if (spawn != null) {
						if (p.hasPermission("region.staff")) {
							spawn.setSpawnpoint(p.getLocation());
							new FancyMessage("&bSpawn location updated!").send(p).queue();
						}
					} else {
						if (p.hasPermission("region.staff")) {
							Cuboid.Selection selection = Cuboid.Selection.source(p);
							if (selection.getPos1() == null && selection.getPos2() == null) {
								new FancyMessage("&cYou need to make a cuboid selection first!").send(p).queue();
							} else {
								SpawnRegion newSpawn = RegionsAPI.getInstance().getSpawnManager().newRegion(selection.getPos1(), selection.getPos2());
								if (newSpawn != null) {
									if (newSpawn.load()) {
										newSpawn.setSpawnpoint(p.getLocation());
										new FancyMessage("&bSpawn location updated!").send(p).queue();
										newSpawn.getBoundary(p).deploy(action -> action.box(Material.RED_STAINED_GLASS, 120));
									}
								}
							}
						}
					}
				}
				return true;
			}

			if (args.length == 1) {
				if (commandLabel.equalsIgnoreCase("spawn")) {
					if (args[0].equalsIgnoreCase("info")) {
						if (p.hasPermission("region.staff")) {
							if (spawn != null) {
								FancyMessageChain chain = new FancyMessageChain();
								chain.append(fancy -> {
									fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
								});
								chain.append(fancy -> {
									fancy.then("Name:").then(" ").then(spawn.getName()).color(ChatColor.YELLOW);
								});
								chain.append(fancy -> {
									fancy.then("Members:").then(" [").then(spawn.getMembers().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", "))).color(ChatColor.YELLOW).then("]");
								});
								chain.append(fancy -> {
									fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
								});
								chain.append(fancy -> {
									fancy.then("[Boundary]").color(ChatColor.YELLOW).hover("&6Click to show the boundary.").action(() -> {
										TaskScheduler.of(() -> spawn.getBoundary(p).deploy(action -> action.box(Material.RED_STAINED_GLASS, 120))).scheduleAsync();
									});
								});
								chain.append(fancy -> {
									fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
								});
								chain.append(fancy -> {
									fancy.then("[Flags]").color(ChatColor.GREEN).hover("&2Click to modify region flags.").action(() -> {
										JavaPlugin.getPlugin(Regions.class).getFlags(p, spawn).get(1);
									});
								});
								chain.append(fancy -> {
									fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
								});
								chain.append(fancy -> {
									fancy.then("[Passthrough]").color(spawn.isPassthrough() ? ChatColor.DARK_AQUA : ChatColor.AQUA).hover("&2Click to change pass-through.").action(() -> {
										spawn.setPassthrough(!spawn.isPassthrough());
										p.performCommand("spawn info");
									});
								});
								chain.append(fancy -> {
									fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
								});

								chain.send(p).queue();
							} else {
								new FancyMessage("&cThere is no spawn region currently set!").send(p).queue();
							}
						}
					}
					if (args[0].equalsIgnoreCase("set")) {
						if (spawn != null) {
							if (p.hasPermission("region.staff")) {
								spawn.setSpawnpoint(p.getLocation());
								new FancyMessage("&bSpawn location updated!").send(p).queue();
							}
						} else {
							if (p.hasPermission("region.staff")) {
								Cuboid.Selection selection = Cuboid.Selection.source(p);
								if (selection.getPos1() == null && selection.getPos2() == null) {
									new FancyMessage("&cYou need to make a cuboid selection first!").send(p).queue();
								} else {
									SpawnRegion newSpawn = RegionsAPI.getInstance().getSpawnManager().newRegion(selection.getPos1(), selection.getPos2());
									if (newSpawn != null) {
										if (newSpawn.load()) {
											newSpawn.setSpawnpoint(p.getLocation());
											new FancyMessage("&bSpawn location updated!").send(p).queue();
											newSpawn.getBoundary(p).deploy(action -> action.box(Material.RED_STAINED_GLASS, 120));
										}
									}
								}
							}
						}
					}
				}
				return true;
			}

		}

		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (alias.equalsIgnoreCase("spawn")) {
			return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, "info", "set").get();
		}
		return super.tabComplete(sender, alias, args);
	}
}
