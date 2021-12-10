package com.github.sanctum.regions.subcommand;

import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.api.SubCommand;
import com.github.sanctum.regions.impl.PlayerRegion;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MemberCommand extends SubCommand {
	public MemberCommand() {
		super("members");
		setPermission("region.share");
	}

	@Override
	public boolean player(Player player, String label, String[] args) {

		if (args.length == 3) {
			String regionName = args[0];
			String argument = args[2];
			PlayerRegion[] regions = RegionsAPI.getInstance().getPlayerManager().getRegions(player);
			PlayerRegion target = Arrays.stream(regions).filter(r -> r.getName().equalsIgnoreCase(regionName)).findFirst().orElse(null);
			LabyrinthUser member = LabyrinthUser.get(argument);
			if (!member.isValid()) {
				// player unknown
				return true;
			}
			if (target != null) {
				switch (args[1].toLowerCase(Locale.ROOT)) {
					case "add":
						target.addMember(member.toBukkit());
						// message new member & sender
						break;
					case "remove":
						target.removeMember(member.toBukkit());
						// message old member & sender
						break;
				}
			}
		}

		return true;
	}

	@Override
	public @NotNull List<String> tab(Player player, String alias, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, Arrays.stream(RegionsAPI.getInstance().getPlayerManager().getRegions(player)).map(Region::getName).collect(Collectors.toList()))
				.then(TabCompletionIndex.TWO, "add", "remove")
				.then(TabCompletionIndex.THREE, Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList()))
				.get();
	}
}
