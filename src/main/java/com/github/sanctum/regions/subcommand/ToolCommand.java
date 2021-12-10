package com.github.sanctum.regions.subcommand;

import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.regions.Regions;
import com.github.sanctum.regions.api.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ToolCommand extends SubCommand {
	private final Regions regions;

	public ToolCommand(Regions regions) {
		super("tool");
		this.regions = regions;
	}

	@Override
	public boolean player(Player player, String label, String[] args) {

		if (!player.getInventory().contains(regions.getTool())) {
			player.getWorld().dropItem(player.getEyeLocation(), regions.getTool());
			new FancyMessage("Here's a new region wand!").color(ChatColor.GREEN).send(player).queue();
		}

		return true;
	}
}
