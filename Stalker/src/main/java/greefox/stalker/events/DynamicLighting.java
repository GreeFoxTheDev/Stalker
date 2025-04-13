package greefox.stalker.events;

import greefox.stalker.Stalker;
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
import java.util.Set;
import java.util.UUID;

import static org.bukkit.Material.*;

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

                int lightLevel = getMaxLightLevel(player);

                if (lightLevel == 0) {
                    removeLight(player);
                    return;
                }

                Location candidate = findAirAbove(player.getLocation());
                if (candidate != null && !candidate.equals(lastLightLocation.get(player.getUniqueId()))) {
                    removeLight(player);
                    createLight(player, candidate, lightLevel);
                    lastLightLocation.put(player.getUniqueId(), candidate);
                }


//                if (!newLocation.equals(lastLightLocation.get(player.getUniqueId()))) {
//                    removeLight(player);
//                    createLight(player, newLocation, lightLevel);
//                    lastLightLocation.put(player.getUniqueId(), newLocation);
//                }
            }
        };

        task.runTaskTimer(plugin, 0L, 0L);
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
            case LANTERN, GLOWSTONE, SEA_LANTERN, LAVA_BUCKET -> 12;
            case SOUL_LANTERN, SOUL_TORCH, REDSTONE_TORCH, REDSTONE_LAMP, JACK_O_LANTERN, BLAZE_ROD -> 10;
            case CANDLE -> 3;
            default -> 0;
        };
    }

    private void createLight(Player player, Location location, int lightLevel) {
        if (location.getWorld() == null) return;
        if (location.getWorld().getBlockAt(location).getType().isSolid() || location.getWorld().getBlockAt(location).getType().equals(Material.LAVA))
            return;
        World world = location.getWorld();

        Light light = (Light) Material.LIGHT.createBlockData();
        light.setWaterlogged(world.getBlockAt(location).getType() == Material.WATER);
        light.setLevel(lightLevel);

        player.sendBlockChange(location, light);
    }

    private Location findAirAbove(Location base) {
        for (int y = 1; y <= 2; y++) {
            Location test = base.clone().add(0, y, 0);
            if (isReplaceable(test)) return test;
        }
        // Try diagonals if needed
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                Location test = base.clone().add(dx, 1, dz);
                if (isReplaceable(test)) return test;
            }
        }
        return null;
    }

    private boolean isReplaceable(Location location) {
        Material type = location.getBlock().getType();

        Material[] notReplaceable = {
                // A
                ACTIVATOR_RAIL,

                // B
                BAMBOO, BEETROOT_SEEDS, ACACIA_BUTTON, // Represents Button; adjust as needed
                BIRCH_BUTTON, DARK_OAK_BUTTON, JUNGLE_BUTTON, OAK_BUTTON, SPRUCE_BUTTON,

                // C
                WHITE_CARPET, // Represents Carpet; include other colors as needed
                CAVE_VINES, CAVE_VINES_PLANT, CARROT, CHORUS_FLOWER, CHORUS_PLANT, COBWEB, COCOA_BEANS, BRAIN_CORAL, // Represents Coral; include other types as needed
                BRAIN_CORAL_FAN, // Represents Coral Fan; include other types as needed

                // D
                DEAD_BUSH, DETECTOR_RAIL,

                // E
                END_GATEWAY, END_PORTAL,

                // F
                FIRE, DANDELION, // Represents Flower; include other types as needed
                FLOWER_POT, FROGSPAWN, WARPED_FUNGUS, // Represents Fungus; include other types as needed
                // "Funky Portal" does not have a corresponding Material constant

                // G
                GLOW_LICHEN, SHORT_GRASS, TALL_GRASS,

                // H
                HANGING_ROOTS, PLAYER_HEAD, // Represents Head; include other types as needed

                // K
                KELP,

                // L
                LADDER, LAVA,
                // "Leftover" is not a specific item; please specify
                LEVER, LIGHT, LILY_PAD,

                // M
                MANGROVE_PROPAGULE, MELON_STEM, MOSS_CARPET, BROWN_MUSHROOM, // Represents Mushroom; include RED_MUSHROOM if needed

                // N
                NETHER_PORTAL, NETHER_SPROUTS, NETHER_WART,

                // P
                PINK_PETALS, PITCHER_PLANT, PITCHER_POD, POWDER_SNOW, POWERED_RAIL, ACACIA_PRESSURE_PLATE, // Represents Pressure Plate; include other types as needed
                BIRCH_PRESSURE_PLATE, DARK_OAK_PRESSURE_PLATE, JUNGLE_PRESSURE_PLATE, OAK_PRESSURE_PLATE, SPRUCE_PRESSURE_PLATE, PUMPKIN_SEEDS,

                // R
                RAIL, COMPARATOR, REDSTONE, REPEATER, REDSTONE_TORCH, WARPED_ROOTS, // Represents Roots; include CRIMSON_ROOTS if needed

                // S
                OAK_SAPLING, // Represents Sapling; include other types as needed
                SCULK_VEIN, SEA_PICKLE, SEAGRASS,
                // "Shrub" does not have a corresponding Material constant
                ACACIA_SIGN, // Represents Sign; include other types as needed
                BIRCH_SIGN, DARK_OAK_SIGN, JUNGLE_SIGN, OAK_SIGN, SPRUCE_SIGN, SMALL_DRIPLEAF, SNOW, SPORE_BLOSSOM, STRING, STRUCTURE_VOID, SUGAR_CANE, SWEET_BERRIES,

                // T
                TORCH,
                // "Torch (Burnt-out)" does not have a corresponding Material constant
                TORCHFLOWER_SEEDS, TRIPWIRE_HOOK, TURTLE_EGG, TWISTING_VINES,

                // V
                VINE,

                // W
                WATER, WEEPING_VINES, WHEAT};
        Set<Material> specialItemSet = Set.of(notReplaceable);
        return !type.isSolid() && !specialItemSet.contains(type);

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
