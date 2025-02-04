package greefox.stalker.events;

import greefox.stalker.Stalker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class OpenDoor implements Listener {

    public OpenDoor(Stalker plugin) {
    }

    @EventHandler
    public void openDoor(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.OAK_DOOR)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock.hasMetadata("custom_door")) {
                String data = clickedBlock.getMetadata("custom_door").get(0).asString();
                if ("special_door".equals(data)) {

                    BukkitRunnable task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            clickedBlock.getLocation().getWorld().createExplosion(clickedBlock.getLocation(), 4.0f);
                        }
                    };

                    task.runTaskLater(Stalker.getInstance(), 20L);
                }
            }
        }
    }
}
