package greefox.stalker.events;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import greefox.stalker.Stalker;
import greefox.stalker.structures.Cross;
import greefox.stalker.structures.Door;
import greefox.stalker.structures.Dungeon;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class SpawnStalker implements Listener {

    private final Map<Player, Zombie> stalkerMap = new HashMap<>();

    public SpawnStalker(Stalker plugin) {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            spawnStalker(target);
        }
    }

    @EventHandler
    public void onHuskTarget(EntityDamageByEntityEvent event) {

        if (event.getDamager().getType().equals(EntityType.HUSK) && Objects.requireNonNull(event.getEntity().getCustomName()).equalsIgnoreCase("stalker") && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerTarget(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType().equals(EntityType.HUSK) && event.getDamager() instanceof Player player && Objects.requireNonNull(event.getEntity().getCustomName()).equalsIgnoreCase("stalker")) {


            Location teleportLocation = findValidLocationAround(player);

            if (teleportLocation != null) {
                // Teleport the entity to the valid location
                event.getEntity().teleport(teleportLocation);
            } else {
                return;
            }
            event.setCancelled(true);

        }
    }

    public void spawnStalker(Player target) {
        if (target == null || stalkerMap.containsKey(target)) return;

        Location spawnLocation = target.getLocation().clone().add(5, 0, 5);
        Husk stalker = target.getWorld().spawn(spawnLocation, Husk.class);
        stalker.setCustomName("stalker");
        stalker.setInvisible(true);
        stalker.setSilent(true);
        stalker.setCollidable(false);
        stalker.setAI(true);
        stalker.setInvulnerable(true);
        //stalker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 255, true, false));

        stalkerMap.put(target, stalker);

        DynamicLighting light = new DynamicLighting(Stalker.getInstance());
        light.startTracking(target);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || stalker.isDead()) {
                    stalker.remove();
                    stalkerMap.remove(target);
                    cancel();
                    return;
                }

                Location playerLocation = target.getLocation();
                Location stalkerLocation = stalker.getLocation();
                double distance = stalkerLocation.distance(playerLocation);

                if (distance > 40) {
                    Location teleport = findValidLocationAround(target);
                    if (teleport != null) stalker.teleport(teleport);
                } else if (distance > 15) {
                    stalker.setAI(true);
                    stalker.setTarget(target);
                } else {
                    stalker.setAI(false);
                    stalker.setTarget(null);
                    stalker.teleport(stalkerLocation.setDirection(playerLocation.subtract(stalkerLocation).toVector()));
                }

                World world = target.getWorld();
                Location tunnelLocation = new Location(world,
                        playerLocation.getBlockX() + (int) (Math.random() * 20 - 10),
                        world.getHighestBlockYAt(playerLocation.getBlockX(), playerLocation.getBlockZ()) - 3,
                        playerLocation.getBlockZ() + (int) (Math.random() * 20 - 10));

                if (isPlayerLookingAt(target, stalker)) {
                    if (Math.random() < 0.0007 && world.getBlockAt(tunnelLocation).getType() != Material.AIR) {
                        buildPredefinedTunnel(tunnelLocation);
                    }
                    if (Math.random() < 0.05) {
                        stalker.getWorld().spawnParticle(Particle.LANDING_OBSIDIAN_TEAR, stalkerLocation.clone().add(0, 1, 0), 100);
                    }
                    if (Math.random() < 0.009) {
                        Location creeperLocation = playerLocation.clone().subtract(playerLocation.getDirection().multiply(4));
                        creeperLocation.setY(playerLocation.getY());
                        world.spawn(creeperLocation, Creeper.class);
                    }
                    if (Math.random() < 0.009) {
                        world.strikeLightning(stalkerLocation);
                        try {
                            Dungeon dungeon = new Dungeon();
                            dungeon.loadSchematic(target, new BukkitWorld(world));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (Math.random() < 0.0001) {
                        try {
                            new Cross(Stalker.getInstance()).spawnCross(target);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        world.strikeLightning(stalkerLocation);
                    }
                    if (Math.random() < 0.1) {
                        world.setStorm(true);
                    }
                }

                if (Math.random() < 0.008) {
                    Location breakLocation = playerLocation.clone().add(Math.random() * 4 - 2, -1, Math.random() * 4 - 2);
                    if (breakLocation.getBlock().getType().isBlock()) {
                        breakLocation.getBlock().setType(Material.AIR);
                        target.playSound(breakLocation, Sound.BLOCK_GRASS_BREAK, 10.0f, 1.0f);
                    }
                }

                if (Math.random() < 0.009)
                    stalker.getWorld().playSound(stalkerLocation, Sound.ENTITY_PLAYER_BREATH, 1f, 0.2f);
                if (Math.random() < 0.008)
                    stalker.getWorld().playSound(stalkerLocation, Sound.BLOCK_STONE_STEP, 1.0f, 1.0f);
                if (Math.random() < 0.001) {
                    Door door = new Door();
                    door.placeOakDoor(stalkerLocation);
                    stalker.getWorld().playSound(stalkerLocation, Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);
                }
                if (Math.random() < 0.5) {
                    Night night = new Night();
                    night.startEffects(target);
                }
            }
        }.runTaskTimer(Stalker.getInstance(), 0L, 10L);
    }

    public boolean isPlayerLookingAt(Player player, Entity entity) {
        Vector toEntity = entity.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return player.getEyeLocation().getDirection().normalize().angle(toEntity) < Math.toRadians(30);
    }

    private void buildPredefinedTunnel(Location location) {
        int size = new Random().nextInt(4) + 1;

        for (int x = -size / 2; x <= size / 2; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = -size / 2; z <= size / 2; z++) {
                    Location blockLocation = location.clone().add(x, y, z);
                    if (y == 0) {
                        blockLocation.getBlock().setType(Material.STONE_BRICKS);
                    } else if (y == size - 1 || x == -size / 2 || x == size / 2 || z == -size / 2 || z == size / 2) {
                        blockLocation.getBlock().setType(Material.AIR);
                    } else {
                        blockLocation.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }

    private Location findValidLocationAround(Player player) {
        Random random = new Random();
        Location playerLocation = player.getLocation();
        int searchRadius = 8;

        for (int attempts = 0; attempts < 20; attempts++) {
            int dx = random.nextInt(searchRadius * 2 + 1) - searchRadius;
            int dz = random.nextInt(searchRadius * 2 + 1) - searchRadius;

            Location potentialLocation = playerLocation.clone().add(dx, 0, dz);
            if (isValidLocation(potentialLocation)) {
                return potentialLocation;
            }
        }

        return null;
    }


    private boolean isValidLocation(Location location) {
        Location blockBelow = location.clone().subtract(0, 1, 0);
        Location blockAt = location.clone();
        Location blockAbove = location.clone().add(0, 1, 0);

        return blockBelow.getBlock().getType().isSolid()
                && blockAt.getBlock().getType() == Material.AIR
                && blockAbove.getBlock().getType() == Material.AIR;
    }


}
