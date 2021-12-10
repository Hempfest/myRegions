package com.github.sanctum.regions.impl;

import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.regions.api.SubCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RegionCommandBase {

	private final Set<SubCommand> subCommands = new HashSet<>();

	private final Command link;

	public RegionCommandBase() {
		this.link = new CommandPoint();
	}

	public void registerSubCommand(SubCommand command) {
		subCommands.add(command);
	}

	public void unregisterSubCommand(SubCommand command) {
		subCommands.remove(command);
	}

	@Ordinal(420)
	Command getLink() {
		return link;
	}

	class CommandPoint extends Command {
		CommandPoint() {
			super("region");
		}

		@Override
		public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

			if (args.length >= 1) {
				if (sender instanceof Player) {
					for (SubCommand cmd : RegionCommandBase.this.subCommands) {
						if (cmd.getLabel().equalsIgnoreCase(args[0])) {
							if (cmd.testPermission((Player) sender)) {
								return cmd.player((((Player) sender).getPlayer()), commandLabel, Arrays.copyOfRange(args, 1, args.length));
							}
							break;
						}
					}
				} else {
					for (SubCommand cmd : RegionCommandBase.this.subCommands) {
						if (cmd.getLabel().equalsIgnoreCase(args[0])) {
							return cmd.console(sender, commandLabel, Arrays.copyOfRange(args, 1, args.length));
						}
					}
				}
			}

			return true;
		}

		@NotNull
		@Override
		public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
			if (args.length == 1) {
				return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, RegionCommandBase.this.subCommands.stream().map(SubCommand::getLabel).collect(Collectors.toList())).get();
			}
			for (SubCommand cmd : RegionCommandBase.this.subCommands) {
				if (cmd.getLabel().equalsIgnoreCase(args[0])) {
					List<String> list = cmd.tab((Player) sender, alias, Arrays.copyOfRange(args, 1, args.length));
					return list == null ? new ArrayList<>() : list;
				}
			}
			return super.tabComplete(sender, alias, args);
		}

	}

}
