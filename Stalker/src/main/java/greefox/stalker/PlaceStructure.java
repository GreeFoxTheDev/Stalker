package greefox.stalker;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import greefox.stalker.structures.Cross;
import greefox.stalker.structures.Dungeon;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaceStructure implements CommandExecutor, TabCompleter {
    private static final String[] ARGS = {"cross", "dungeon"};

    Stalker plugin;

    public PlaceStructure(Stalker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("place")) {
            if (args.length == 1) {
                if (sender instanceof Player player) {
                    if (player.isOp()) {
                        switch (args[0]) {
                            case "cross":
                                try {
                                    new Cross(Stalker.getInstance()).spawnCross(player);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                String loc = Cross.surface.getBlockX() + "," + Cross.surface.getBlockY() + "," + Cross.surface.getBlockZ() + "lol";
                                player.sendMessage("Cross has been spawned at " + loc);

                            case "dungeon":
                                try {
                                    new Dungeon(Stalker.getInstance()).spawnDungeon(player);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                String loc2 = Dungeon.location.toString();
                                player.sendMessage("Dungeon has been spawned at " + loc2);

                        }
                    }
                }
            }

        }
        return true;

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        final List<String> structures = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], Arrays.asList(ARGS), structures);
        Collections.sort(structures);
        if (args.length == 1) {
            return structures;
        }
        return new ArrayList<>();
    }
}
