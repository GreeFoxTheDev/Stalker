package greefox.stalker;

public class AngryVillagerBuilder {
    private Stalker plugin;

    public AngryVillagerBuilder setPlugin(Stalker plugin) {
        this.plugin = plugin;
        return this;
    }

    public AngryVillager createAngryVillager() {
        return new AngryVillager(plugin);
    }
}