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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Dungeon {

    public FileConfiguration config = Stalker.getInstance().getConfig();
    private final File dungeon_stalker = new File(Stalker.getInstance().getDataFolder(), "structures/dungeon_stalker.schem");

    public Dungeon(Stalker plugin) {
    }


    public void spawnDungeon(Player player) throws IOException {

        com.sk89q.worldedit.world.World bWorld = new BukkitWorld(player.getWorld());
        loadSchematic(player, bWorld);

    }
    public void loadSchematic(Player player, com.sk89q.worldedit.world.World bWorld) throws IOException {

        ClipboardFormat format = ClipboardFormats.findByAlias("sponge");
        try (ClipboardReader reader = format.getReader(new FileInputStream(dungeon_stalker))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(bWorld, -1)) {
                BlockVector3 pasteLocation = BlockVector3.at(player.getLocation().getBlockX(),
                        player.getLocation().getBlockY() + config.getInt("structures.dungeon.depth"),
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
