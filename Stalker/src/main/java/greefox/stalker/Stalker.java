package greefox.stalker;

import greefox.stalker.commands.PlaceStructure;
import greefox.stalker.commands.StalkerCommand;
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
    public File dungeon_stalker = new File(getDataFolder(), "structures/dungeon_stalker.schem");
    public File refuge_top = new File(getDataFolder(), "structures/refuge_top.schem");
    public File refuge_basement = new File(getDataFolder(), "structures/refuge_basement.schem");
    public File resort_top = new File(getDataFolder(), "structures/resort_top.schem");
    public File resort_bottom_1 = new File(getDataFolder(), "structures/resort_bottom_1.schem");
    public File resort_bottom_2 = new File(getDataFolder(), "structures/resort_bottom_2.schem");



    public static Stalker getInstance() {
        return instance;
    }

    private void create_dungeon_stalker() {
        dungeon_stalker = new File(getDataFolder(), "structures/dungeon_stalker.schem");
        if (!dungeon_stalker.exists()) {
            saveResource("structures/dungeon_stalker.schem", false);
        }
    }
    private void create_cross(){
        cross_top = new File(getDataFolder(), "structures/cross_top.schem");
        cross_middle = new File(getDataFolder(), "structures/cross_middle.schem");
        cross_bottom = new File(getDataFolder(), "structures/cross_bottom.schem");
        cross_tnt = new File(getDataFolder(), "structures/cross_tnt.schem");
        if (!cross_top.exists()) {
            saveResource("structures/cross_top.schem", false);
        }
        if (!cross_middle.exists()) {
            saveResource("structures/cross_middle.schem", false);
        }
        if (!cross_bottom.exists()) {
            saveResource("structures/cross_bottom.schem", false);
        }
        if (!cross_tnt.exists()) {
            saveResource("structures/cross_tnt.schem", false);
        }
    }

    private void create_refuge() {
        refuge_top = new File(getDataFolder(), "structures/refuge_top.schem");
        if (!refuge_top.exists()) {
            saveResource("structures/refuge_top.schem", false);
        }
        refuge_basement = new File(getDataFolder(), "structures/refuge_basement.schem");
        if (!refuge_basement.exists()) {
            saveResource("structures/refuge_basement.schem", false);
        }
    }
    private void create_resort(){
        resort_top = new File(getDataFolder(), "structures/resort_top.schem");
        resort_bottom_1 = new File(getDataFolder(), "structures/resort_bottom_1.schem");
        resort_bottom_2 = new File(getDataFolder(), "structures/resort_bottom_2.schem");
        if (!resort_top.exists()){
            saveResource("structures/resort_top.schem", true);
        }
        if (!resort_bottom_1.exists()){
            saveResource("structures/resort_bottom_1.schem", true);
        }
        if (!resort_bottom_2.exists()){
            saveResource("structures/resort_bottom_2.schem", true);
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        config = getConfig();
        reloadConfig();
        saveDefaultConfig();

        create_dungeon_stalker();
        create_cross();
        create_refuge();
        create_resort();


        getServer().getPluginManager().registerEvents(new Cross(this), this);
        getServer().getPluginManager().registerEvents(new SpawnStalker(this), this);
        getServer().getPluginManager().registerEvents(new DynamicLighting(this), this);
        //getServer().getPluginManager().registerEvents(new AngryAnimals(this), this);
        getServer().getPluginManager().registerEvents(new OpenDoor(this), this);


        Objects.requireNonNull(this.getCommand("place")).setExecutor(new PlaceStructure(this));
        Objects.requireNonNull(this.getCommand("place")).setTabCompleter(new PlaceStructure(this));

        Objects.requireNonNull(this.getCommand("stalker")).setExecutor(new StalkerCommand(this));
        Objects.requireNonNull(this.getCommand("stalker")).setTabCompleter(new StalkerCommand(this));


    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
