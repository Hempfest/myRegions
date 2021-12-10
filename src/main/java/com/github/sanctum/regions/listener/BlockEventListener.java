package com.github.sanctum.regions.listener;

import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.data.RegionServicesManager;
import com.github.sanctum.labyrinth.event.RegionBuildEvent;
import com.github.sanctum.labyrinth.event.RegionDestroyEvent;
import com.github.sanctum.labyrinth.event.RegionPVPEvent;
import com.github.sanctum.labyrinth.event.custom.DefaultEvent;
import com.github.sanctum.labyrinth.event.custom.Subscribe;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.library.Cuboid;
import com.github.sanctum.labyrinth.library.Deployable;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.regions.api.PlayerRegionManager;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.impl.PlayerRegion;
import com.github.sanctum.regions.impl.RegionUtility;
import com.github.sanctum.regions.impl.SpawnRegion;
import java.text.NumberFormat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

public final class BlockEventListener implements Listener {

	private static final int MAX_DRAW_DISTANCE = 2500;

	@Subscribe
	public void onPvP(RegionPVPEvent e) {
		if (e.getRegion() instanceof SpawnRegion) {
			Region.Resident resident = Region.Resident.get(e.getPlayer());
			Region.Resident target = Region.Resident.get(e.getTarget());
			if (resident.isSpawnTagged()) {
				if (!target.isSpawnTagged()) {
					resident.setSpawnTagged(false);
				} else {
					e.setCancelled(true);
				}
			} else {
				if (target.isSpawnTagged()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@Subscribe
	public void onBuild(RegionBuildEvent e) {

		Region r = e.getRegion();

		Cuboid.Flag flag = r.getFlag("build").orElse(null);
		if (flag != null) {

			if (!flag.isEnabled()) {
				if (e.getPlayer().hasPermission("region.staff")) return;
				if (!r.isMember(e.getPlayer()) || r.getOwner() != null && !r.getOwner().getName().equalsIgnoreCase(e.getPlayer().getName())) {
					e.setCancelled(true);
				}
			}

		}

	}

	@Subscribe
	public void onBuild(RegionDestroyEvent e) {

		Region r = e.getRegion();

		Cuboid.Flag flag = r.getFlag("break").orElse(null);
		if (flag != null) {

			if (!flag.isEnabled()) {
				if (e.getPlayer().hasPermission("region.staff")) return;
				if (!r.isMember(e.getPlayer()) || r.getOwner() != null && !r.getOwner().getName().equalsIgnoreCase(e.getPlayer().getName())) {
					e.setCancelled(true);
				}
			}

		}

	}


	String getLocation(Location location) {
		NumberFormat format = NumberFormat.getNumberInstance();
		return "X: " + format.format(location.getX()) + " Y: " + format.format(location.getY()) + " Z: " + format.format(location.getZ());
	}

	@Subscribe(priority = Vent.Priority.HIGHEST, processCancelled = true)
	public void onInteract(DefaultEvent.Interact e) {
		if (!RegionsAPI.getInstance().getTool().isSimilar(e.getItem())) return;
		if (e.getPlayer().isSneaking()) {
			Cuboid.Selection selection = Cuboid.Selection.source(e.getPlayer());
			selection.setPos1(null);
			selection.setPos2(null);
			new FancyMessage("&aCuboid selection reset!").send(e.getPlayer()).queue();
			e.setResult(Event.Result.DENY);
			return;
		}
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getBlock().isPresent()) {
			Block b = e.getBlock().get();
			Cuboid.Selection selection = Cuboid.Selection.source(e.getPlayer());
			if (selection.getPos2() != null && selection.getPos2().distanceSquared(b.getLocation()) > MAX_DRAW_DISTANCE) {
				new FancyMessage("&cCuboid selection too large!").send(e.getPlayer()).queue();
				new FancyMessage("&aCuboid selection reset!").send(e.getPlayer()).queue();
				selection.setPos1(null);
				selection.setPos2(null);
				e.setResult(Event.Result.DENY);
				return;
			}
			selection.setPos1(b.getLocation());
			new FancyMessage("&dCuboid position #1 set @ &r" + getLocation(b.getLocation())).send(e.getPlayer()).queue();
			if (selection.getPos2() != null) {
				PlayerRegionManager playerManager = RegionsAPI.getInstance().getPlayerManager();
				if (playerManager.getOwnedBlocks(e.getPlayer()) >= playerManager.getMaxBlocks(e.getPlayer())) {
					// you're at or over your block limit!
				} else {
					if ((playerManager.getOwnedBlocks(e.getPlayer()) + selection.toCuboid().getTotalBlocks()) >= playerManager.getMaxBlocks(e.getPlayer())) {
						// you're trying to claim too much, it will go over your limit we cant do that
					} else {
						showParameter(selection).deploy();
					}
				}
			}
			e.setResult(Event.Result.DENY);
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getBlock().isPresent()) {
			Block b = e.getBlock().get();
			Cuboid.Selection selection = Cuboid.Selection.source(e.getPlayer());
			if (selection.getPos1() != null && selection.getPos1().distanceSquared(b.getLocation()) > MAX_DRAW_DISTANCE) {
				new FancyMessage("&cCuboid selection too large!").send(e.getPlayer()).queue();
				new FancyMessage("&aCuboid selection reset!").send(e.getPlayer()).queue();
				selection.setPos1(null);
				selection.setPos2(null);
				e.setResult(Event.Result.DENY);
				return;
			}
			selection.setPos2(b.getLocation());
			new FancyMessage("&dCuboid position #2 set @ &r" + getLocation(b.getLocation())).send(e.getPlayer()).queue();
			if (selection.getPos1() != null) {
				PlayerRegionManager playerManager = RegionsAPI.getInstance().getPlayerManager();
				if (playerManager.getOwnedBlocks(e.getPlayer()) >= playerManager.getMaxBlocks(e.getPlayer())) {
					// you're at or over your block limit!
				} else {
					if ((playerManager.getOwnedBlocks(e.getPlayer()) + selection.toCuboid().getTotalBlocks()) >= playerManager.getMaxBlocks(e.getPlayer())) {
						// you're trying to claim too much, it will go over your limit we cant do that
					} else {
						showParameter(selection).deploy();
					}
				}
			}
			e.setResult(Event.Result.DENY);
		}
	}

	Deployable<Player> showParameter(Cuboid.Selection selection) {
		return Deployable.of(selection.getPlayer(), unused -> {
			if (RegionsAPI.getInstance().isRegion(selection.getPos1()) || RegionsAPI.getInstance().isRegion(selection.getPos2())) {
				Region r = RegionServicesManager.getInstance().get(selection.getPos1()) != null ? RegionServicesManager.getInstance().get(selection.getPos1()) : RegionServicesManager.getInstance().get(selection.getPos2());
				if (r instanceof PlayerRegion && ((PlayerRegion) r).getOwner().getUniqueId().equals(selection.getPlayer().getUniqueId())) {
					Player player = selection.getPlayer();
					Message message = new FancyMessage(RegionUtility.getInstance().getMessage("region-creation-laced-verify", player)).then(" ").then(RegionUtility.getInstance().getMessage("region-creation-laced-verify-button", player)).action(() -> {
						PlayerRegion region = selection.toCuboid().toRegion(key -> new PlayerRegion(key.getValue().getKey(), key.getValue().getValue(), player.getUniqueId()));
						region.setPassthrough(true);
						region.load();
						Mailer.empty(player).action(RegionUtility.getInstance().getMessage("region-laced-creation", player).replace("{0}", region.getName())).queue();
					}).hover("&6Click to create a laced region here.").then(" ").then(RegionUtility.getInstance().getMessage("region-creation-laced-display-verify-button", player)).hover(RegionUtility.getInstance().getMessage("region-creation-laced-display-verify", player)).action(() -> {
						TaskScheduler.of(() -> player.teleport(r.getCenter())).scheduleLater(2);
						r.getBoundary(player).deploy(action -> action.box(Material.BLUE_STAINED_GLASS, 120));
						selection.toCuboid().getBoundary(player).deploy(action -> action.box(Material.RED_STAINED_GLASS, 120));
					});
					message.send(player).queue();
					Cuboid.Boundary boundary = selection.toCuboid().getBoundary(player);
					TaskScheduler.of(() -> player.teleport(selection.toCuboid().toRegion().getCenter())).scheduleLater(2);
					boundary.deploy(action -> action.box(Material.RED_STAINED_GLASS, 120));
				} else {
					new FancyMessage("&cThere is already a region here that you don't own!").send(unused).queue();
					selection.setPos1(null);
					selection.setPos2(null);
				}
			} else {
				Player player = selection.getPlayer();
				Message message = new FancyMessage(RegionUtility.getInstance().getMessage("region-creation-verify", player)).then(" ").then(RegionUtility.getInstance().getMessage("region-creation-verify-button", player)).action(() -> {
					PlayerRegion region = selection.toCuboid().toRegion(key -> RegionsAPI.getInstance().getPlayerManager().newRegion(player, key.getValue()));
					if (region == null) return;
					region.load();
					Mailer.empty(player).action(RegionUtility.getInstance().getMessage("region-creation", player).replace("{0}", region.getName())).queue();
				}).hover("&eClick to create a region here.");
				message.send(player).queue();
				Cuboid.Boundary boundary = selection.toCuboid().getBoundary(player);
				TaskScheduler.of(() -> player.teleport(selection.toCuboid().toRegion().getCenter())).scheduleLater(2);
				boundary.deploy(action -> action.box(Material.RED_STAINED_GLASS, 120));
			}
		});
	}

}
