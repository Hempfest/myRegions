package com.github.sanctum.regions.subcommand;

import com.github.sanctum.labyrinth.formatting.pagination.EasyPagination;
import com.github.sanctum.regions.api.SubCommand;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.impl.PlayerRegion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ListCommand extends SubCommand {
	public ListCommand() {
		super("list");
	}

	@Override
	public boolean player(Player player, String label, String[] args) {

		PlayerRegion[] regions = RegionsAPI.getInstance().getPlayerManager().getRegions(player);
		EasyPagination<PlayerRegion> regionPagination = new EasyPagination<>(player, regions);
		regionPagination.limit(5);
		regionPagination.setHeader((player1, chunks) -> chunks.then("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").style(ChatColor.STRIKETHROUGH));
		regionPagination.setFormat((playerRegion, integer, chunks) -> chunks.then(playerRegion.getName()).command("region info " + playerRegion.getName()).then(" / ").then(playerRegion.getTotalBlocks()).hover("&fThe block count.").color(ChatColor.GRAY).then("b").color(ChatColor.YELLOW).style(ChatColor.BOLD));
		regionPagination.setFooter((player1, chunks) -> chunks.then("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").style(ChatColor.STRIKETHROUGH));
		regionPagination.send(1);
		return true;
	}
}
