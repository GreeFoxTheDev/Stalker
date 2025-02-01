package greefox.stalker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class DynamicLighting implements Listener {
    private final HashMap<UUID, Location> lastLightLocation = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> lightTasks = new HashMap<>();
    private final Stalker plugin;

    public DynamicLighting(Stalker plugin) {
        this.plugin = plugin;
    }

    public void startTracking(Player player) {
        if (lightTasks.containsKey(player.getUniqueId())) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopTracking(player);
                    return;
                }

                Location newLocation = player.getLocation().clone().add(0, 1, 0);
                int lightLevel = getMaxLightLevel(player);

                if (lightLevel == 0) {
                    removeLight(player);
                    return;
                }

                if (!newLocation.equals(lastLightLocation.get(player.getUniqueId()))) {
                    removeLight(player);
                    createLight(player, newLocation, lightLevel);
                    lastLightLocation.put(player.getUniqueId(), newLocation);
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 1L); // Runs every 2 ticks (0.1s)
        lightTasks.put(player.getUniqueId(), task);
    }

    public void stopTracking(Player player) {
        UUID uuid = player.getUniqueId();
        if (lightTasks.containsKey(uuid)) {
            lightTasks.get(uuid).cancel();
            lightTasks.remove(uuid);
        }
        removeLight(player);
        lastLightLocation.remove(uuid);
    }

    private int getMaxLightLevel(Player player) {
        int mainHandLight = getLightLevel(player.getInventory().getItemInMainHand());
        int offHandLight = getLightLevel(player.getInventory().getItemInOffHand());

        return Math.max(mainHandLight, offHandLight);
    }

    private int getLightLevel(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;

        return switch (item.getType()) {
            case TORCH -> 14;
            case LANTERN -> 15;
            case SOUL_LANTERN, SOUL_TORCH -> 10;
            case GLOWSTONE, REDSTONE_LAMP -> 15;
            case CANDLE -> 3;  // Base candle light level
            case CANDLE_CAKE -> 12;
            default -> 0;
        };
    }

    private void createLight(Player player, Location location, int lightLevel) {
        if (location.getWorld() == null) return;
        World world = location.getWorld();

        Light light = (Light) Material.LIGHT.createBlockData();
        light.setWaterlogged(world.getBlockAt(location).getType() == Material.WATER);
        light.setLevel(lightLevel);

        player.sendBlockChange(location, light);
    }

    private void removeLight(Player player) {
        UUID uuid = player.getUniqueId();
        if (!lastLightLocation.containsKey(uuid)) return;

        Location location = lastLightLocation.get(uuid);
        if (location.getWorld() != null) {
            player.sendBlockChange(location, location.getWorld().getBlockAt(location).getBlockData());
        }
        lastLightLocation.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopTracking(event.getPlayer());
    }
}
