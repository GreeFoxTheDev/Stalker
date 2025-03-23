package greefox.stalker.utils;

import org.bukkit.Material;

public class LootItem {
    public Material material;
    public int minAmount;
    public int maxAmount;

    public LootItem(Material material, int minAmount, int maxAmount) {
        this.material = material;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }
}