package greefox.stalker.events;

import greefox.stalker.Stalker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class OpenDoor implements Listener {
    public FileConfiguration config = Stalker.getInstance().getConfig();

    public OpenDoor(Stalker plugin) {
    }

    @EventHandler
    public void openDoor(PlayerInteractEvent event) {
        boolean explosion = config.getBoolean("structures.door.explosion", true);
        double explosionPower = config.getDouble("structures.door.explosion_power", 4.0);
        if (explosion) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().equals(Material.OAK_DOOR)) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock.hasMetadata("custom_door")) {
                    String data = clickedBlock.getMetadata("custom_door").getFirst().asString();
                    if ("special_door".equals(data)) {

                        BukkitRunnable task = new BukkitRunnable() {
                            @Override
                            public void run() {
                                assert clickedBlock.getLocation().getWorld() != null;
                                clickedBlock.getLocation().getWorld().createExplosion(clickedBlock.getLocation(), (float) explosionPower);
                            }
                        };

                        task.runTaskLater(Stalker.getInstance(), 20L);
                    }
                }
            }
        }
    }
}
