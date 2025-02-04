package greefox.stalker.structures;

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
import greefox.stalker.Stalker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Cross implements Listener {

    public File cross_top = new File(Stalker.getInstance().getDataFolder(), "structures/cross_top.schem");
    public File cross_middle = new File(Stalker.getInstance().getDataFolder(), "structures/cross_middle.schem");
    public File cross_bottom = new File(Stalker.getInstance().getDataFolder(), "structures/cross_bottom.schem");
    public File cross_tnt = new File(Stalker.getInstance().getDataFolder(), "structures/cross_tnt.schem");
    public Cross(Stalker plugin) {
    }

    public void spawnCross(Player player) throws IOException {

        com.sk89q.worldedit.world.World bWorld = new BukkitWorld(player.getWorld());
        loadSchematic(player, bWorld);

    }

    private void loadSchematic(Player player, com.sk89q.worldedit.world.World bWorld) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByAlias("sponge.3");
        if (format == null) {
            player.sendMessage("Error: Unsupported schematic format!");
            return;
        }
        Location surface = findSurfaceLocation(player.getLocation(), 20);

        try (ClipboardReader reader = format.getReader(new FileInputStream(cross_top))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance()
                    .getEditSessionFactory()
                    .getEditSession(bWorld, -1)) {

                if (surface != null) {

                    BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                    ClipboardHolder holder = new ClipboardHolder(clipboard);

                    Operation operation = holder.createPaste(editSession)
                            .to(surfaceVector)
                            .ignoreAirBlocks(true)
                            .copyEntities(true)
                            .build();

                    Operations.complete(operation);
                }
            } catch (WorldEditException e) {
                player.sendMessage("Error during schematic pasting: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            player.sendMessage("Error loading schematic: " + e.getMessage());
            throw e;
        }

        //load middle part

        try (ClipboardReader reader = format.getReader(new FileInputStream(cross_middle))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance()
                    .getEditSessionFactory()
                    .getEditSession(bWorld, -1)) {

                if (surface != null) {

                    BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                    ClipboardHolder holder = new ClipboardHolder(clipboard);

                    Operation operation = holder.createPaste(editSession)
                            .to(surfaceVector)
                            .ignoreAirBlocks(false)
                            .copyEntities(true)
                            .build();

                    Operations.complete(operation);
                }
            } catch (WorldEditException e) {
                player.sendMessage("Error during schematic pasting: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            player.sendMessage("Error loading schematic: " + e.getMessage());
            throw e;
        }

        //load the down part
        try (ClipboardReader reader = format.getReader(new FileInputStream(cross_bottom))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance()
                    .getEditSessionFactory()
                    .getEditSession(bWorld, -1)) {

                if (surface != null) {
                    BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                    ClipboardHolder holder = new ClipboardHolder(clipboard);

                    Operation operation = holder.createPaste(editSession)
                            .to(surfaceVector)
                            .ignoreAirBlocks(false)
                            .copyEntities(true)
                            .build();

                    Operations.complete(operation);
                }
            } catch (WorldEditException e) {
                player.sendMessage("Error during schematic pasting: " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            player.sendMessage("Error loading schematic: " + e.getMessage());
            throw e;
        }


        double chance = 50.0;

        double randomValue = ThreadLocalRandom.current().nextDouble(100);

        if (randomValue < chance) {

            //load tnt
            try (ClipboardReader reader = format.getReader(new FileInputStream(cross_tnt))) {
                Clipboard clipboard = reader.read();

                try (EditSession editSession = WorldEdit.getInstance()
                        .getEditSessionFactory()
                        .getEditSession(bWorld, -1)) {

                    if (surface != null) {

                        BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                        ClipboardHolder holder = new ClipboardHolder(clipboard);

                        Operation operation = holder.createPaste(editSession)
                                .to(surfaceVector)
                                .ignoreAirBlocks(true)
                                .copyEntities(false)
                                .build();

                        Operations.complete(operation);
                    }
                } catch (WorldEditException e) {
                    player.sendMessage("Error during schematic pasting: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                player.sendMessage("Error loading schematic: " + e.getMessage());
                throw e;
            }


        }
    }

    public Location findSurfaceLocation(Location startLocation, int searchRadius) {
        int startX = startLocation.getBlockX();
        int startZ = startLocation.getBlockZ();
        World world = startLocation.getWorld();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                // Get the highest block at the current X and Z coordinates
                Location topLocation = world.getHighestBlockAt(startX + x, startZ + z).getLocation();

                Location blockBelow = topLocation.clone().subtract(0, 1, 0);
                Location blockAt = topLocation.clone();
                Location blockX = topLocation.clone().add(1, -1, 0);
                Location blockY = topLocation.clone().add(0, -1, 1);
                Location blockmX = topLocation.clone().add(-1, -1, 0);
                Location blockmY = topLocation.clone().add(0, -1, -1);

                if (isValidSurfaceLocation(blockBelow, blockAt, blockX, blockY, blockmX, blockmY)) {
                    return blockAt.clone().add(0, 1, 0);
                }
            }
        }

        return null;
    }

    private boolean isValidSurfaceLocation(Location blockBelow, Location blockAt, Location blockX, Location blockY, Location blockmX, Location blockmY) {
        return blockBelow.getBlock().getType().isSolid()
                && blockAt.getBlock().getType().isSolid()
                && !blockAt.getBlock().getType().isBurnable()
                && blockX.getBlock().getType().isSolid()
                && blockY.getBlock().getType().isSolid()
                && blockmX.getBlock().getType().isSolid()
                && blockmY.getBlock().getType().isSolid();
    }

    private void addLootToSpecificBarrel(Location origin, int targetX, int targetY, int targetZ) {
        Location barrelLocation = origin.clone().add(targetX, targetY, targetZ);
        if (barrelLocation.getBlock().getType() == Material.BARREL) {
            Barrel barrel = (Barrel) barrelLocation.getBlock().getState();
            Inventory inventory = barrel.getInventory();

            // Generate loot for the barrel
            Random random = new Random();
            for (int i = 0; i < inventory.getSize(); i++) {
                if (random.nextDouble() < 0.5) { // 50% chance to add an item
                    inventory.setItem(i, generateRandomLoot(random));
                }
            }
        }
    }

    private ItemStack generateRandomLoot(Random random) {
        Material[] lootItems = {Material.DIAMOND, Material.GOLD_INGOT, Material.IRON_INGOT, Material.EMERALD, Material.ENCHANTED_BOOK, Material.APPLE, Material.GOLDEN_APPLE, Material.BREAD};

        Material material = lootItems[random.nextInt(lootItems.length)];
        int amount = material.getMaxStackSize() > 1 ? random.nextInt(material.getMaxStackSize()) + 1 : 1;
        return new ItemStack(material, amount);
    }
}
