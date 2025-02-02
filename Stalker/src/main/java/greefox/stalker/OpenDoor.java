package greefox.stalker;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

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
                    clickedBlock.getLocation().clone().add(0, 1, 0).getBlock().setType(Material.AIR);
                    clickedBlock.setType(Material.AIR);
                    clickedBlock.getLocation().clone().add(0, -1, 0).getBlock().setType(Material.AIR);
                    clickedBlock.getWorld().createExplosion(clickedBlock.getLocation(), 4.0f);
                }
            }
        }
    }
}
