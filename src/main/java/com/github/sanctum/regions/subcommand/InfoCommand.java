package com.github.sanctum.regions.subcommand;

import com.github.sanctum.labyrinth.formatting.FancyMessageChain;
import com.github.sanctum.regions.Regions;
import com.github.sanctum.regions.api.SubCommand;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.impl.PlayerRegion;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class InfoCommand extends SubCommand {

	public InfoCommand() {
		super("info");
	}

	@Override
	public boolean player(Player player, String label, String[] args) {

		if (args.length > 0) {
			PlayerRegion[] regions = RegionsAPI.getInstance().getPlayerManager().getRegions(player);
			PlayerRegion target = Arrays.stream(regions).filter(r -> r.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);
			if (target != null) {
				FancyMessageChain chain = new FancyMessageChain();
				chain.append(fancy -> {
					fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
				});
				chain.append(fancy -> {
					fancy.then("Name:").then(" ").then(target.getName()).color(ChatColor.YELLOW).hover("Click to change the name").suggest("/region name " + target.getId() + " ");
				});
				chain.append(fancy -> {
					fancy.then("Owner:").then(" ").then(target.getOwner().getName()).color(ChatColor.YELLOW);
				});
				chain.append(fancy -> {
					fancy.then("Members:").then(" [").then(target.getMembers().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", "))).color(ChatColor.YELLOW).then("]");
				});
				chain.append(fancy -> {
					fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
				});
				chain.append(fancy -> {
					fancy.then("[Boundary]").color(ChatColor.YELLOW).hover("&6Click to show the boundary.").action(() -> {
						target.getBoundary(player).deploy(action -> action.box(Material.RED_STAINED_GLASS, 120));
					});
				});
				chain.append(fancy -> {
					fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
				});
				chain.append(fancy -> {
					fancy.then("[Flags]").color(ChatColor.GREEN).hover("&2Click to modify region flags.").action(() -> {
						JavaPlugin.getPlugin(Regions.class).getFlags(player, target).get(1);
					});
				});
				chain.append(fancy -> {
					fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
				});
				chain.append(fancy -> {
					fancy.then("[Passthrough]").color(target.isPassthrough() ? ChatColor.DARK_AQUA : ChatColor.AQUA).hover("&2Click to change pass-through.").action(() -> {
						target.setPassthrough(!target.isPassthrough());
						player.performCommand("region info " + args[0]);
					});
				});
				chain.append(fancy -> {
					fancy.then(".......................................................").color(ChatColor.DARK_GRAY);
				});

				chain.send(player).queue();
			}
			return true;
		}

		return false;
	}
}
