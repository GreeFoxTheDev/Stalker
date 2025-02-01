package greefox.stalker;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import jdk.jfr.FlightRecorder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class SpawnStalker implements Listener {

    public SpawnStalker(Stalker plugin) {
    }

    private final Map<Player, Zombie> stalkerMap = new HashMap<>();

    private File dungeon_stalker = new File(Stalker.getInstance().getDataFolder(), "structures/dungeon_stalker.schem");


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        spawnStalker(event.getPlayer());
    }
    @EventHandler
    public void onHuskTarget(EntityDamageByEntityEvent event) {

        if (event.getDamager().getType().equals(EntityType.HUSK) && Objects.requireNonNull(event.getEntity().getCustomName()).equalsIgnoreCase("stalker") && event.getEntity() instanceof Player) {
            // Cancel the event to stop the Husk from attacking the player
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
        stalker.setAI(true); // Enable AI for pathfinding
        stalker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 255, true, false));

        stalkerMap.put(target, stalker); // Track the stalker

        DynamicLighting light = new DynamicLighting(Stalker.getInstance());
        light.startTracking(target);

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

                // If too far, make the stalker teleport towards the player
                if (distance > 40) {
                    Location teleport = findValidLocationAround(target);
                    assert teleport != null;
                    stalker.teleport(teleport);
                }
                if (distance > 15 && distance <= 40) {
                    stalker.setAI(true);
                    stalker.setTarget(target); // Pathfind toward the player
                } else if (distance <= 15) {
                    stalker.setAI(false);
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
                    if (Math.random() < 0.0007) {
                        if (world.getBlockAt(tunnelLocation).getType() != Material.AIR) {
                            // Build the tunnel at the chosen location
                            buildPredefinedTunnel(tunnelLocation);
                        }
                    }
                    if (Math.random() < 0.05) {
                        Location look = stalkerLocation.clone().add(0, 1, 0);
                        stalker.getWorld().spawnParticle(Particle.LANDING_OBSIDIAN_TEAR, look, 100, 0d, 0d, 0d);
                    }
                    if (Math.random() < 0.009) {
                        Location targetLocation = target.getLocation();
                        Vector direction = targetLocation.getDirection();

                        // Calculate the location behind the player
                        Location creeperLocation = targetLocation.clone().subtract(direction.multiply(4));
                        creeperLocation.setY(targetLocation.getY()); // Ensure the same height

                        // Spawn a creeper at the calculated location
                        target.getWorld().spawn(creeperLocation, Creeper.class);
                    }
                    if (Math.random() < 0.001) {
                        Objects.requireNonNull(stalkerLocation.getWorld()).strikeLightning(stalkerLocation);

                        com.sk89q.worldedit.world.World bWorld = new BukkitWorld(target.getWorld());
                        try {
                            loadSchematic(target, bWorld);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        target.sendMessage("3");

                    }

                }


                // Occasionally break blocks nearby
                if (Math.random() < 0.008) {
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

                if (Math.random() < 0.01) {
                    stalker.getWorld().playSound(stalker.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1f, 0.2f);
                }

                if (Math.random() < 0.008) {

                    stalker.getWorld().playSound(stalker.getLocation(), Sound.BLOCK_STONE_STEP, 1.0f, 1.0f);
                }
                if (Math.random() < 0.001) {
                    placeOakDoor(stalkerLocation);

                    stalker.getWorld().playSound(stalker.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);


                }


            }


        }.runTaskTimer(Stalker.getInstance(), 0L, 10L); // Repeat every 10 ticks
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
        int number = random.nextInt(4) + 1;

        // Materials for the tunnel
        Material wallMaterial = Material.AIR;
        Material floorMaterial = Material.STONE_BRICKS;
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

    public void placeOakDoor(Location bottomBlockLocation) {
        // Set the bottom block to OAK_DOOR
        Block bottomBlock = bottomBlockLocation.getBlock();
        bottomBlock.setType(Material.OAK_DOOR);

        // Configure the bottom part of the door
        BlockData bottomBlockData = bottomBlock.getBlockData();
        if (bottomBlockData instanceof org.bukkit.block.data.type.Door doorData) {
            doorData.setHalf(Bisected.Half.BOTTOM); // This is the bottom part
            doorData.setFacing(BlockFace.NORTH);    // Set the direction (change to your desired direction)
            doorData.setHinge(org.bukkit.block.data.type.Door.Hinge.LEFT);     // Optional: hinge position
            bottomBlock.setBlockData(doorData);
        }

        // Set the top block to OAK_DOOR
        Block topBlock = bottomBlockLocation.clone().add(0, 1, 0).getBlock();
        topBlock.setType(Material.OAK_DOOR);

        // Configure the top part of the door
        BlockData topBlockData = topBlock.getBlockData();
        if (topBlockData instanceof org.bukkit.block.data.type.Door doorData) {
            doorData.setHalf(Bisected.Half.TOP); // This is the top part
            doorData.setFacing(BlockFace.NORTH); // Same direction as the bottom
            doorData.setHinge(Door.Hinge.LEFT);  // Same hinge position
            topBlock.setBlockData(doorData);
        }


    }

    private Location findValidLocationAround(Player player) {
        Random random = new Random();
        Location playerLocation = player.getLocation();
        int searchRadius = 8; // Radius around the player to search for valid spots

        for (int attempts = 0; attempts < 20; attempts++) { // Try up to 20 random locations
            int dx = random.nextInt(searchRadius * 2 + 1) - searchRadius;
            int dz = random.nextInt(searchRadius * 2 + 1) - searchRadius;

            Location potentialLocation = playerLocation.clone().add(dx, 0, dz);
            if (isValidLocation(potentialLocation)) {
                return potentialLocation;
            }
        }

        return null; // No valid location found
    }


    private boolean isValidLocation(Location location) {
        Location blockBelow = location.clone().subtract(0, 1, 0); // Block below the target
        Location blockAt = location.clone();                     // Target block
        Location blockAbove = location.clone().add(0, 1, 0);     // Block above the target

        // Ensure the block below is solid, and the current and above blocks are air
        return blockBelow.getBlock().getType().isSolid()
                && blockAt.getBlock().getType() == Material.AIR
                && blockAbove.getBlock().getType() == Material.AIR;
    }

    private void loadSchematic(Player player, com.sk89q.worldedit.world.World bWorld) throws IOException {

        ClipboardFormat format = ClipboardFormats.findByAlias("sponge");
        try (ClipboardReader reader = format.getReader(new FileInputStream(dungeon_stalker))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {
                BlockVector3 pasteLocation = BlockVector3.at(player.getLocation().getBlockX(),
                        player.getLocation().getBlockY()-50,
                        player.getLocation().getBlockZ());

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pasteLocation)
                        .ignoreAirBlocks(false)
                        .copyEntities(true)
                        .build();
                Operations.complete(operation);
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
