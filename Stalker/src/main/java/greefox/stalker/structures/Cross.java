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
import greefox.stalker.utils.AddLoot;
import greefox.stalker.utils.FindLocation;
import greefox.stalker.utils.LootItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;


public class Cross implements Listener {
    public static Location surface;
    public FileConfiguration config = Stalker.getInstance().getConfig();
    public File cross_top = new File(Stalker.getInstance().getDataFolder(), "structures/cross_top.schem");
    public File cross_middle = new File(Stalker.getInstance().getDataFolder(), "structures/cross_middle.schem");
    public File cross_bottom = new File(Stalker.getInstance().getDataFolder(), "structures/cross_bottom.schem");
    public File cross_tnt = new File(Stalker.getInstance().getDataFolder(), "structures/cross_tnt.schem");
    LootItem[] common = {new LootItem(Material.DRIED_KELP, 1, 2), new LootItem(Material.RED_CANDLE, 1, 2), new LootItem(Material.STICK, 1, 2), new LootItem(Material.GLASS_BOTTLE, 1, 2), new LootItem(Material.REDSTONE, 1, 3), new LootItem(Material.COBWEB, 1, 2)};
    LootItem[] uncommon = {new LootItem(Material.IRON_INGOT, 1, 2), new LootItem(Material.GOLD_INGOT, 1, 2), new LootItem(Material.GOLDEN_APPLE, 1, 1), new LootItem(Material.BREAD, 1, 3)};
    LootItem[] rare = {new LootItem(Material.GOLDEN_APPLE, 1, 1), new LootItem(Material.EXPERIENCE_BOTTLE, 1, 1), new LootItem(Material.IRON_PICKAXE, 1, 1), new LootItem(Material.ENDER_PEARL, 1, 1)};
    LootItem[] epic = {new LootItem(Material.ENCHANTED_GOLDEN_APPLE, 1, 1), new LootItem(Material.ECHO_SHARD, 2, 4), new LootItem(Material.AMETHYST_SHARD, 1, 4), new LootItem(Material.CLOCK, 1, 1)};

    public Cross(Stalker plugin) {
    }

    public void spawnCross(Player player) throws IOException {

        com.sk89q.worldedit.world.World bWorld = new BukkitWorld(player.getWorld());
        loadSchematic(player, bWorld);
        addLoot();

    }

    private void loadSchematic(Player player, com.sk89q.worldedit.world.World bWorld) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByAlias("sponge.3");
        if (format == null) {
            player.sendMessage("Error: Unsupported schematic format!");
            return;
        }
        surface = FindLocation.findSurfaceLocation(player.getLocation(), 20, "cross");

        try (ClipboardReader reader = format.getReader(new FileInputStream(cross_top))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {

                if (surface != null) {

                    BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                    ClipboardHolder holder = new ClipboardHolder(clipboard);

                    Operation operation = holder.createPaste(editSession).to(surfaceVector).ignoreAirBlocks(true).copyEntities(true).build();

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

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {

                if (surface != null) {

                    BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                    ClipboardHolder holder = new ClipboardHolder(clipboard);

                    Operation operation = holder.createPaste(editSession).to(surfaceVector).ignoreAirBlocks(false).copyEntities(true).build();

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

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {

                if (surface != null) {
                    BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                    ClipboardHolder holder = new ClipboardHolder(clipboard);

                    Operation operation = holder.createPaste(editSession).to(surfaceVector).ignoreAirBlocks(false).copyEntities(true).build();

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


        boolean tnt = config.getBoolean("structures.cross.enable_tnt", true);
        int chance = config.getInt("structures.cross.tnt_chance", 50);
        if (tnt) {

            double randomValue = ThreadLocalRandom.current().nextDouble(100);

            if (randomValue < chance) {

                //load tnt
                try (ClipboardReader reader = format.getReader(new FileInputStream(cross_tnt))) {
                    Clipboard clipboard = reader.read();

                    try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {

                        if (surface != null) {

                            BlockVector3 surfaceVector = BlockVector3.at(surface.getBlockX(), surface.getBlockY(), surface.getBlockZ());

                            ClipboardHolder holder = new ClipboardHolder(clipboard);

                            Operation operation = holder.createPaste(editSession).to(surfaceVector).ignoreAirBlocks(true).copyEntities(false).build();

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
    }

    private void addLoot() {

        AddLoot.addLootToSpecificContainer(surface, 4, -10, -2, common, uncommon, rare, epic);
        AddLoot.addLootToSpecificContainer(surface, 5, -9, -2, common, uncommon, rare, epic);
        AddLoot.addLootToSpecificContainer(surface, 5, -10, -1, common, uncommon, rare, epic);

    }
}
