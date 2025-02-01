package greefox.stalker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class DynamicLighting1 implements Listener {

    private final Map<Location, Material> lightMap = new HashMap<>();

    public DynamicLighting1(Stalker plugin) {
    }

//    @EventHandler
//    public void Move(PlayerMoveEvent event) {
//        if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.TORCH)) {
//            Location location = event.getPlayer().getLocation().clone().add(0, 1, 0);
//            Player player = event.getPlayer();
//            int lightInt = 15;
//            createLight(player, location, lightInt);
//        }
//    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location prevLocation = event.getFrom().clone().add(0, 1, 0);
        Location newLocation = player.getLocation().clone().add(0, 1, 0);

        if (!newLocation.equals(prevLocation)) {
            // Remove light at previous location
            removeLight(prevLocation);

            // If holding a torch, place light at new location
            if (player.getInventory().getItemInMainHand().getType() == Material.TORCH) {
                createLight(player, newLocation, 15);
            }
        }
    }


    private void createLight(Player player, Location location, int lightInt) {
        World world = location.getWorld();
        if (world == null) return;

        Material originalBlock = world.getBlockAt(location).getType(); // Store original block

        // Only place light if the block is AIR or WATER
        if (originalBlock == Material.AIR || originalBlock == Material.CAVE_AIR || originalBlock == Material.WATER) {
            Light light = (Light) Material.LIGHT.createBlockData();
            light.setWaterlogged(originalBlock == Material.WATER); // Set waterlogged if underwater
            light.setLevel(lightInt);

            world.setBlockData(location, light); // Actually place the light
            lightMap.put(location, originalBlock); // Store the original block
        }
    }

    private void removeLight(Location location) {
        World world = location.getWorld();
        //if (world == null || !lightMap.containsKey(location)) return;

        Bukkit.getLogger().info("1");

        Material originalBlock = lightMap.get(location); // Get the original block
        world.getBlockAt(location).setType(Material.AIR); // Restore the original block
        Bukkit.getLogger().info("2");

        lightMap.remove(location); // Remove from tracking
    }



}
