package greefox.stalker.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class AddLoot extends JavaPlugin {
    public static void addLootToSpecificContainer(Location origin, int targetX, int targetY, int targetZ, LootItem[] common, LootItem[] uncommon, LootItem[] rare, LootItem[] epic) {
        Location barrelLocation = origin.clone().add(targetX, targetY, targetZ);
//        if (barrelLocation.getBlock().getType() == Material.BARREL) {
//            Barrel barrel = (Barrel) barrelLocation.getBlock().getState();
//            Inventory inventory = barrel.getInventory();
//
//            Random random = new Random();
//            for (int i = 0; i < inventory.getSize(); i++) {
//                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
//                    if (random.nextDouble() < 0.5) {
//                        inventory.setItem(i, generateRandomLoot(random, common, uncommon, rare, epic));
//                    }
//                }
//            }
//        }
//        if (barrelLocation.getBlock().getType() == Material.CHEST) {
//            Chest chest = (Chest) barrelLocation.getBlock().getState();
//            Inventory inventory = chest.getInventory();
//
//            Random random = new Random();
//            for (int i = 0; i < inventory.getSize(); i++) {
//                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
//                    if (random.nextDouble() < 0.5) {
//                        inventory.setItem(i, generateRandomLoot(random, common, uncommon, rare, epic));
//                    }
//                }
//            }
//        }
        Location containerLocation = origin.clone().add(targetX, targetY, targetZ);

        Material blockType = containerLocation.getBlock().getType();

        if (blockType == Material.BARREL || blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST) {
            Inventory inventory;
            if (blockType == Material.BARREL) {
                Barrel barrel = (Barrel) containerLocation.getBlock().getState();
                inventory = barrel.getInventory();
            } else {
                Chest chest = (Chest) containerLocation.getBlock().getState();
                inventory = chest.getInventory();
            }

            Random random = new Random();
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                    if (random.nextDouble() < 0.5) {
                        inventory.setItem(i, generateRandomLoot(random, common, uncommon, rare, epic));
                        //containerLocation.getBlock().setType(Material.GOLD_BLOCK);
                    }
                }
            }
        }

    }


    private static ItemStack generateRandomLoot(Random random, LootItem[] common, LootItem[] uncommon, LootItem[] rare, LootItem[] epic) {


        double roll = random.nextDouble();
        LootItem selectedLoot;

        if (roll < 0.85) {
            selectedLoot = common[random.nextInt(common.length)];
        } else if (roll < 0.96) {
            selectedLoot = uncommon[random.nextInt(uncommon.length)];
        } else if (roll < 0.99){
            selectedLoot = rare[random.nextInt(rare.length)];
        } else {
            selectedLoot = epic[random.nextInt(epic.length)];
        }

        return new ItemStack(selectedLoot.material, random.nextInt(selectedLoot.maxAmount) + 1);
    }
}
