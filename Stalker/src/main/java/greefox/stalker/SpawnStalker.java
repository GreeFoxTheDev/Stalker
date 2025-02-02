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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
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

    private final Map<Player, Zombie> stalkerMap = new HashMap<>();
    private final File dungeon_stalker = new File(Stalker.getInstance().getDataFolder(), "structures/dungeon_stalker.schem");

    public SpawnStalker(Stalker plugin) {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        spawnStalker(event.getPlayer());
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
        stalker.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, -1, 255, true, false));

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
                    if (Math.random() < 0.1) {
                        world.strikeLightning(stalkerLocation);
                        try {
                            loadSchematic(target, new BukkitWorld(world));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        target.sendMessage("3");
                    }
                    if (Math.random() < 0.1) {
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

                if (Math.random() < 0.01)
                    stalker.getWorld().playSound(stalkerLocation, Sound.ENTITY_PLAYER_BREATH, 1f, 0.2f);
                if (Math.random() < 0.008)
                    stalker.getWorld().playSound(stalkerLocation, Sound.BLOCK_STONE_STEP, 1.0f, 1.0f);
                if (Math.random() < 0.1) {
                    placeOakDoor(stalkerLocation);
                    stalker.getWorld().playSound(stalkerLocation, Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);
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


    public void placeOakDoor(Location bottomBlockLocation) {
        Block bottomBlock = bottomBlockLocation.getBlock();
        Block topBlock = bottomBlockLocation.clone().add(0, 1, 0).getBlock();

        // Set both blocks to OAK_DOOR before configuring their data
        bottomBlock.setType(Material.OAK_DOOR, false);
        topBlock.setType(Material.OAK_DOOR, false);

        // Configure the bottom part of the door
        BlockData bottomData = bottomBlock.getBlockData();
        if (bottomData instanceof org.bukkit.block.data.type.Door doorDataBottom) {
            doorDataBottom.setHalf(Bisected.Half.BOTTOM);
            doorDataBottom.setFacing(BlockFace.NORTH); // Adjust direction as needed
            doorDataBottom.setHinge(Door.Hinge.LEFT);
            doorDataBottom.setPowered(false); // Ensure door is not powered (optional)
            bottomBlock.setBlockData(doorDataBottom, false);
        }

        // Configure the top part of the door
        BlockData topData = topBlock.getBlockData();
        if (topData instanceof org.bukkit.block.data.type.Door doorDataTop) {
            doorDataTop.setHalf(Bisected.Half.TOP);
            doorDataTop.setFacing(BlockFace.NORTH);
            doorDataTop.setHinge(Door.Hinge.LEFT);
            doorDataTop.setPowered(false); // Ensure top isn't powered either
            topBlock.setBlockData(doorDataTop, false);
        }

        bottomBlock.setMetadata("custom_door", new FixedMetadataValue(Stalker.getInstance(), "special_door"));
        topBlock.setMetadata("custom_door", new FixedMetadataValue(Stalker.getInstance(), "special_door"));
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

    private void loadSchematic(Player player, com.sk89q.worldedit.world.World bWorld) throws IOException {

        ClipboardFormat format = ClipboardFormats.findByAlias("sponge");
        try (ClipboardReader reader = format.getReader(new FileInputStream(dungeon_stalker))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {
                BlockVector3 pasteLocation = BlockVector3.at(player.getLocation().getBlockX(),
                        player.getLocation().getBlockY() - 50,
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
