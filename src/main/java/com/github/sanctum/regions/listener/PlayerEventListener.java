package com.github.sanctum.regions.listener;

import com.github.sanctum.labyrinth.data.Region;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.regions.api.RegionsAPI;
import com.github.sanctum.regions.impl.SpawnRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class PlayerEventListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent e) {
		SpawnRegion spawn = RegionsAPI.getInstance().getSpawnManager().getSpawn();
		if (spawn != null) {
			e.setRespawnLocation(spawn.getSpawnpoint());
			TaskScheduler.of(() -> {
				Region.Resident resident = Region.Resident.get(e.getPlayer());
				resident.setPastSpawn(false);
				resident.setSpawnTagged(true);
			}).scheduleLater(1);
		}
	}

}
