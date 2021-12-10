package com.github.sanctum.regions.subcommand;

import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.string.DefaultColor;
import com.github.sanctum.regions.api.SubCommand;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.impl.PlayerRegion;
import java.util.Arrays;
import org.bukkit.entity.Player;

public class NameCommand extends SubCommand {

	public NameCommand() {
		super("name");
	}

	@Override
	public boolean player(Player player, String label, String[] args) {

		if (args.length > 1) {
			PlayerRegion[] regions = RegionsAPI.getInstance().getPlayerManager().getRegions(player);
			PlayerRegion target = Arrays.stream(regions).filter(r -> r.getId().toString().equalsIgnoreCase(args[0])).findFirst().orElse(null);
			if (target != null) {
				target.setName(args[1]);
				new FancyMessage("Name changed to").then(" ").then(args[1]).style(DefaultColor.GALAXY).send(player).queue();
			}
			return true;
		}

		return false;
	}
}
