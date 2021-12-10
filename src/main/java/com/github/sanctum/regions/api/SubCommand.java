package com.github.sanctum.regions.api;

import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Mailer;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SubCommand {

	private final String label;
	private String permission;
	private String permissionMsg = "&cYou don't have permission <permission>";

	public SubCommand(String label) {
		this.label = label;
	}

	public @NotNull String getLabel() {
		return this.label;
	}

	public @Nullable String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public @NotNull String getNoPermissionMessage() {
		return permissionMsg.replace("<permission>", getPermission());
	}

	public void setNoPermissionMessage(@NotNull String permissionMsg) {
		this.permissionMsg = permissionMsg;
	}

	public boolean testPermission(Player target) {
		if (getPermission() == null) return true;
		if (!target.hasPermission(getPermission())) {
			Mailer.empty(target).chat(getNoPermissionMessage()).queue();
			return false;
		}
		return true;
	}

	public abstract boolean player(Player player, String label, String[] args);

	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	public @Nullable List<String> tab(Player player, String alias, String[] args) {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList())).get();
	}

}
