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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Refuge implements Listener {
    public static Location surface;
    public File refuge_top = new File(Stalker.getInstance().getDataFolder(), "structures/refuge_top.schem");
    public File refuge_basement = new File(Stalker.getInstance().getDataFolder(), "structures/refuge_basement.schem");
    LootItem[] common = {new LootItem(Material.BREAD, 1, 2), new LootItem(Material.TORCH, 1, 2), new LootItem(Material.COAL, 1, 3), new LootItem(Material.WHEAT, 1, 2), new LootItem(Material.WHEAT_SEEDS, 1, 3), new LootItem(Material.COBWEB, 1, 2)};
    LootItem[] uncommon = {new LootItem(Material.RAW_IRON, 1, 2), new LootItem(Material.AMETHYST_SHARD, 1, 2), new LootItem(Material.RAW_COPPER, 1, 1), new LootItem(Material.BREAD, 1, 3)};
    LootItem[] rare = {new LootItem(Material.GOLDEN_APPLE, 1, 1), new LootItem(Material.CRYING_OBSIDIAN, 1, 1), new LootItem(Material.WOODEN_HOE, 1, 1), new LootItem(Material.COBWEB, 1, 1)};
    LootItem[] epic = {new LootItem(Material.EXPERIENCE_BOTTLE, 1, 1), new LootItem(Material.CHAINMAIL_LEGGINGS, 2, 4), new LootItem(Material.CHAINMAIL_BOOTS, 1, 4), new LootItem(Material.IRON_CHESTPLATE, 1, 1)};

    public Refuge(Stalker plugin) {
    }

    public void spawnRefuge(Player player) throws IOException {

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
        surface = FindLocation.findSurfaceLocation(player.getLocation(), 20, "refuge");

        try (ClipboardReader reader = format.getReader(new FileInputStream(refuge_top))) {
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
        try (ClipboardReader reader = format.getReader(new FileInputStream(refuge_basement))) {
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
    }

    private void addLoot() {
        AddLoot.addLootToSpecificContainer(surface, 4, -5, 3, common, uncommon, rare, epic);
    }
}
