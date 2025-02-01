package greefox.stalker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Stalker extends JavaPlugin implements Listener {

    private static Stalker instance;
    public File cross_top = new File(getDataFolder(), "structures/cross_top.schem");
    public File cross_middle = new File(getDataFolder(), "structures/cross_middle.schem");
    public File cross_bottom = new File(getDataFolder(), "structures/cross_bottom.schem");
    public File cross_tnt = new File(getDataFolder(), "structures/cross_tnt.schem");
    private File dungeon_stalker = new File(getDataFolder(), "structures/dungeon_stalker.schem");

    public static Stalker getInstance() {
        return instance;
    }

    private void create_dungeon_stalker() {
        dungeon_stalker = new File(getDataFolder(), "structures/dungeon_stalker.schem");
        if (!dungeon_stalker.exists()) {
            dungeon_stalker.getParentFile().mkdirs();
            saveResource("structures/dungeon_stalker.schem", false);
        }
    }

    private void create_cross_top() {
        cross_top = new File(getDataFolder(), "structures/cross_top.schem");
        if (!cross_top.exists()) {
            cross_top.getParentFile().mkdirs();
            saveResource("structures/cross_top.schem", false);
        }
    }

    private void create_cross_middle() {
        cross_middle = new File(getDataFolder(), "structures/cross_middle.schem");
        if (!cross_middle.exists()) {
            cross_middle.getParentFile().mkdirs();
            saveResource("structures/cross_middle.schem", false);
        }
    }

    private void create_cross_bottom() {
        cross_bottom = new File(getDataFolder(), "structures/cross_bottom.schem");
        if (!cross_bottom.exists()) {
            cross_bottom.getParentFile().mkdirs();
            saveResource("structures/cross_bottom.schem", false);
        }
    }

    private void create_cross_tnt() {
        cross_tnt = new File(getDataFolder(), "structures/cross_tnt.schem");
        if (!cross_tnt.exists()) {
            cross_tnt.getParentFile().mkdirs();
            saveResource("structures/cross_tnt.schem", false);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        create_dungeon_stalker();
        create_cross_top();
        create_cross_middle();
        create_cross_bottom();
        create_cross_tnt();


        getServer().getPluginManager().registerEvents(new Cross(this), this);
        getServer().getPluginManager().registerEvents(new SpawnStalker(this), this);
        getServer().getPluginManager().registerEvents(new DynamicLighting(this), this);


        for (Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage("1");
        }

    }

    @Override
    public void onDisable() {
        instance = null;
    }

}
