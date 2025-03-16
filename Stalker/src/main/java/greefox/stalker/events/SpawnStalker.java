package greefox.stalker.events;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import greefox.stalker.Stalker;
import greefox.stalker.structures.Cross;
import greefox.stalker.structures.Door;
import greefox.stalker.structures.Dungeon;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnStalker implements Listener {
    public FileConfiguration config = Stalker.getInstance().getConfig();


    private final Map<Player, Zombie> stalkerMap = new HashMap<>();
    private final Random random = new Random();

    public SpawnStalker(Stalker plugin) {}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getOnlinePlayers().forEach(this::spawnStalker);
    }

    @EventHandler
    public void onHuskTarget(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager.getType() == EntityType.HUSK
                && "stalker".equalsIgnoreCase(damager.getCustomName())
                && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTarget(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Husk stalker) || !(event.getDamager() instanceof Player player)) return;
        if (!"stalker".equalsIgnoreCase(stalker.getCustomName())) return;

        Location teleportLocation = findValidLocationAround(player);
        stalker.teleport(teleportLocation);
        event.setCancelled(true);
    }

    public void spawnStalker(Player target) {
        if (target == null || stalkerMap.containsKey(target)) return;

        Location spawnLocation = findValidLocationAround(target);
        Husk stalker = target.getWorld().spawn(spawnLocation, Husk.class);
        stalker.setCustomName("stalker");
        stalker.setInvisible(true);
        stalker.setSilent(true);
        stalker.setCollidable(false);
        stalker.setAI(true);
        stalker.setInvulnerable(true);

        stalkerMap.put(target, stalker);
        new DynamicLighting(Stalker.getInstance()).startTracking(target);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!target.isOnline() || stalker.isDead()) {
                    stalker.remove();
                    stalkerMap.remove(target);
                    cancel();
                    return;
                }

                double distance = stalker.getLocation().distance(target.getLocation());

                if (distance > 40) {
                    stalker.teleport(findValidLocationAround(target));
                } else if (distance > 15) {
                    stalker.setAI(true);
                    stalker.setTarget(target);
                } else {
                    stalker.setAI(false);
                    stalker.setTarget(null);
                    stalker.teleport(stalker.getLocation().setDirection(target.getLocation().subtract(stalker.getLocation()).toVector()));
                }

                if (isPlayerLookingAt(target, stalker)) {
                    performSpookyEffects(target, stalker);
                }
            }
        }.runTaskTimer(Stalker.getInstance(), 0L, 10L);
    }

    private void performSpookyEffects(Player target, Husk stalker) {
        World world = target.getWorld();
        Location stalkerLocation = stalker.getLocation();

        if (random.nextDouble() < 0.05) {
            world.spawnParticle(Particle.LANDING_OBSIDIAN_TEAR, stalkerLocation.clone().add(0, 1, 0), 100);
        }
        if (random.nextDouble() < 0.009) {
            Location creeperLocation = target.getLocation().clone().subtract(target.getLocation().getDirection().multiply(4));
            creeperLocation.setY(target.getLocation().getY());
            world.spawn(creeperLocation, Creeper.class);
        }
        if (random.nextDouble() < 0.001) {
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

            if (players.isEmpty()) {
            }

            int randomIndex = ThreadLocalRandom.current().nextInt(players.size());
            String name = players.get(randomIndex).getName();
            Bukkit.broadcastMessage("<" + name + ">" + " ?");
        }

        if (config.getBoolean("structures.dungeon.enable")) {
            if (random.nextDouble() < 0.009) {
                world.strikeLightning(stalkerLocation);
                try {
                    new Dungeon(Stalker.getInstance()).spawnDungeon(target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (config.getBoolean("structures.cross.enable")) {

            if (random.nextDouble() < 0.0001) {
                try {
                    new Cross(Stalker.getInstance()).spawnCross(target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                world.strikeLightning(stalkerLocation);
            }
        }
        if (random.nextDouble() < 0.1) {
            world.setStorm(true);
        }
        if (random.nextDouble() < 0.008) {
            Location breakLocation = target.getLocation().clone().add(random.nextDouble() * 4 - 2, -1, random.nextDouble() * 4 - 2);
            if (breakLocation.getBlock().getType().isBlock()) {
                breakLocation.getBlock().setType(Material.AIR);
                target.playSound(breakLocation, Sound.BLOCK_GRASS_BREAK, 10.0f, 1.0f);
            }
        }
        if (random.nextDouble() < 0.009)
            world.playSound(stalkerLocation, Sound.ENTITY_PLAYER_BREATH, 1f, 0.2f);
        if (random.nextDouble() < 0.008)
            world.playSound(stalkerLocation, Sound.BLOCK_STONE_STEP, 1.0f, 1.0f);
        if (config.getBoolean("structures.door.enable")) {

            if (random.nextDouble() < 0.001) {
                new Door().placeOakDoor(stalkerLocation);
                world.playSound(stalkerLocation, Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);
            }
        }
        if (config.getBoolean("effects.night_effects.enable")) {
            if (random.nextDouble() < 0.5) {
                new Night().startEffects(target);
            }
        }
        if (config.getBoolean("effects.cave_effects.enable")) {
            if (random.nextDouble() < 0.5) {
                new Night().startEffects(target);
            }
        }
    }

    public boolean isPlayerLookingAt(Player player, Entity entity) {
        Vector toEntity = entity.getLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return player.getEyeLocation().getDirection().normalize().angle(toEntity) < Math.toRadians(30);
    }

    private Location findValidLocationAround(Player player) {
        Location playerLocation = player.getLocation();
        int searchRadius = 8;
        Location fallbackLocation = playerLocation.clone().add(5, 0, 5);

        for (int attempts = 0; attempts < 50; attempts++) {
            int dx = random.nextInt(searchRadius * 2 + 1) - searchRadius;
            int dz = random.nextInt(searchRadius * 2 + 1) - searchRadius;
            int dy = random.nextInt(3) - 1;

            Location potentialLocation = playerLocation.clone().add(dx, dy, dz);
            if (isValidLocation(potentialLocation)) {
                return potentialLocation;
            }
        }
        return fallbackLocation;
    }

    private boolean isValidLocation(Location location) {
        return location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()
                && location.getBlock().getType() == Material.AIR
                && location.clone().add(0, 1, 0).getBlock().getType() == Material.AIR;
    }
}
