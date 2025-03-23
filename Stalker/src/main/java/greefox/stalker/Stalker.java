package greefox.stalker;

import greefox.stalker.events.DynamicLighting;
import greefox.stalker.events.OpenDoor;
import greefox.stalker.events.SpawnStalker;
import greefox.stalker.structures.Cross;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class Stalker extends JavaPlugin implements Listener {

    private static Stalker instance;
    public FileConfiguration config = this.getConfig();
    public File cross_top = new File(getDataFolder(), "structures/cross_top.schem");
    public File cross_middle = new File(getDataFolder(), "structures/cross_middle.schem");
    public File cross_bottom = new File(getDataFolder(), "structures/cross_bottom.schem");
    public File cross_tnt = new File(getDataFolder(), "structures/cross_tnt.schem");
    private File dungeon_stalker = new File(getDataFolder(), "structures/dungeon_stalker.schem");
    public File refuge_top = new File(getDataFolder(), "structures/refuge_top.schem");
    public File refuge_basement = new File(getDataFolder(), "structures/refuge_basement.schem");



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
    private void create_refuge_top() {
        refuge_top = new File(getDataFolder(), "structures/refuge_top.schem");
        if (!refuge_top.exists()) {
            refuge_top.getParentFile().mkdirs();
            saveResource("structures/refuge_top.schem", false);
        }
    }
    private void create_refuge_basement() {
        refuge_basement = new File(getDataFolder(), "structures/refuge_basement.schem");
        if (!refuge_basement.exists()) {
            refuge_basement.getParentFile().mkdirs();
            saveResource("structures/refuge_basement.schem", false);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        config = getConfig();
        reloadConfig();
        saveDefaultConfig();

        create_dungeon_stalker();
        create_cross_top();
        create_cross_middle();
        create_cross_bottom();
        create_cross_tnt();
        create_refuge_top();
        create_refuge_basement();


        getServer().getPluginManager().registerEvents(new Cross(this), this);
        getServer().getPluginManager().registerEvents(new SpawnStalker(this), this);
        getServer().getPluginManager().registerEvents(new DynamicLighting(this), this);
        //getServer().getPluginManager().registerEvents(new AngryAnimals(this), this);
        getServer().getPluginManager().registerEvents(new OpenDoor(this), this);


        Objects.requireNonNull(this.getCommand("place")).setExecutor(new PlaceStructure(this));
        Objects.requireNonNull(this.getCommand("place")).setTabCompleter(new PlaceStructure(this));


    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
