package greefox.stalker;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.common.value.qual.IntRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Stalker extends JavaPlugin {

    private final Map<Player, Zombie> stalkerMap = new HashMap<>();

    @Override
    public void onEnable() {
        //spawnStalker(Bukkit.getPlayer("GreeFox")); // Replace with dynamic player detection
        for (Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage("1");
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                spawnStalker(event.getPlayer());
            }
        }, this);
    }

    public void spawnStalker(Player target) {
        if (target == null || stalkerMap.containsKey(target)) return;

        Location spawnLocation = target.getLocation().clone().add(5, 0, 5);
        Husk stalker = target.getWorld().spawn(spawnLocation, Husk.class);
        stalker.setInvisible(true);
        stalker.setSilent(true);
        stalker.setCollidable(false);
        stalker.setGlowing(true);
        stalker.setAI(true); // Enable AI for pathfinding
        stalker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 255, true, false));

        stalkerMap.put(target, stalker); // Track the stalker

        new BukkitRunnable() {
            @Override
            public void run() {


                if (!target.isOnline() || stalker.isDead()) {
                    stalker.remove();
                    stalkerMap.remove(target); // Clean up the map
                    cancel();
                    return;
                }

                Location playerLocation = target.getLocation();
                Location stalkerLocation = stalker.getLocation();
                double distance = stalkerLocation.distance(playerLocation);

                // If too far, make the stalker walk toward the player
                if (distance > 40){
                    stalker.teleport(target.getLocation());
                }
                if (distance > 10 && distance <= 40) {
                    stalker.setTarget(target); // Pathfind toward the player
                } else if (distance <= 10) {
                    stalker.setTarget(null); // Stop moving when close
                    stalker.teleport(stalkerLocation.setDirection(playerLocation.subtract(stalkerLocation).toVector())); // Face the player
                }

                World world = target.getWorld();
                int randomX = target.getLocation().getBlockX() + (int) (Math.random() * 20 - 10); // Random X offset
                int randomZ = target.getLocation().getBlockZ() + (int) (Math.random() * 20 - 10); // Random Z offset
                int randomY = world.getHighestBlockYAt(randomX, randomZ) - 3; // Start 3 blocks below the surface

                Location tunnelLocation = new Location(world, randomX, randomY, randomZ);

                // Check if the chosen location is valid (not in air, not already in a cave)
                if (isPlayerLookingAt(target, stalker)) {
                    if (Math.random() < 0.01) {
                        if (world.getBlockAt(tunnelLocation).getType() != Material.AIR) {
                            // Build the tunnel at the chosen location
                            buildPredefinedTunnel(tunnelLocation);
                        }
                    }
                    if (Math.random() < 0.5) {
                        Location look = stalkerLocation.clone().add(0,1,0);
                        stalker.getWorld().spawnParticle(Particle.SOUL, look, 20, 0d, 0d, 0d);
                    }
                }


                // Occasionally break blocks nearby
                if (Math.random() < 0.001) {
                    Location breakLocation = playerLocation.clone().add(
                            Math.random() * 4 - 2, // Random x offset
                            -1, // Slightly below player level
                            Math.random() * 4 - 2 // Random z offset
                    );
                    Material blockType = breakLocation.getBlock().getType();
                    if (blockType != Material.AIR && blockType.isBlock()) {
                        breakLocation.getBlock().setType(Material.AIR);

                        // Play the block's breaking sound
//                        Sound breakSound = getBreakSound(blockType);
//                        if (breakSound != null) {
//                            target.playSound(breakLocation, breakSound, 1.0f, 1.0f);
//                        }
                        target.playSound(breakLocation, Sound.BLOCK_GRASS_BREAK, 10.0f, 1.0f);
                    }
                }

                // Play walking sounds
                if (Math.random() < 0.002) {
                    stalker.getWorld().playSound(stalker.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1f, 0.2f);
                }

                if (Math.random() < 0.005) {

                    stalker.getWorld().playSound(stalker.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
                }
                if (Math.random() < 0.001) {
                    stalker.getLocation().getBlock().setType(Material.OAK_DOOR);

                    stalker.getWorld().playSound(stalker.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);


                }


            }


        }.runTaskTimer(this, 0L, 10L); // Repeat every 10 ticks
    }

    public boolean isPlayerLookingAt(Player player, Entity entity) {
        Location eyeLocation = player.getEyeLocation();
        Vector toEntity = entity.getLocation().toVector().subtract(eyeLocation.toVector()).normalize();
        Vector playerDirection = eyeLocation.getDirection().normalize();
        double angle = playerDirection.angle(toEntity);
        return angle < Math.toRadians(30); // Check if within 30 degrees
    }

    private void buildPredefinedTunnel(Location location) {
        World world = location.getWorld();

        // Tunnel dimensions (3x3)
        Random random = new Random();
        int number = random.nextInt(3) + 2;

        // Materials for the tunnel
        Material wallMaterial = Material.AIR;
        Material floorMaterial = Material.AIR;
        Material ceilingMaterial = Material.AIR;

        // Loop through the tunnel area and place blocks
        for (int x = -number / 2; x <= number / 2; x++) {
            for (int y = 0; y < number; y++) {
                for (int z = -number / 2; z <= number / 2; z++) {
                    Location blockLocation = location.clone().add(x, y, z);

                    if (y == 0) {
                        // Floor
                        blockLocation.getBlock().setType(floorMaterial);
                    } else if (y == number - 1) {
                        // Ceiling
                        blockLocation.getBlock().setType(ceilingMaterial);
                    } else if (x == -number / 2 || x == number / 2 || z == -number / 2 || z == number / 2) {
                        // Walls
                        blockLocation.getBlock().setType(wallMaterial);
                    } else {
                        // Air inside the tunnel (empty space)
                        blockLocation.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }
}
