package greefox.stalker.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.enginehub.linbus.stream.token.LinToken;

import java.util.HashMap;
import java.util.Map;

public class FindLocation {
    public static Location findSurfaceLocation(Location startLocation, int searchRadius, String structureType) {
        int startX = startLocation.getBlockX();
        int startZ = startLocation.getBlockZ();
        World world = startLocation.getWorld();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {

                Location topLocation = world.getHighestBlockAt(startX + x, startZ + z).getLocation();

                Map<Integer, Location> base = new HashMap<>();
                base.put(1, topLocation.clone().add(0, -1, 0)); //below
                base.put(2, topLocation.clone().add(1, -1, 0)); //x
                base.put(3, topLocation.clone().add(0, -1, 1)); //y
                base.put(4, topLocation.clone().add(-1, -1, 0)); //mx
                base.put(5, topLocation.clone().add(0, -1, -1)); //mY

                Map<Integer, Location> cross = new HashMap<>(base);
                cross.put(6, topLocation.clone().add(6, -7, 3));
                cross.put(7, topLocation.clone().add(6, -7, -3));


                Map<Integer, Location> refuge = new HashMap<>(base);
                refuge.put(6, topLocation.clone().add(5, -1, 4));
                refuge.put(7, topLocation.clone().add(-1, -1, 4));
                refuge.put(8, topLocation.clone().add(5, -1, -2));
                refuge.put(9, topLocation.clone().add(-1, -1, -3));

                Map<Integer, Location> refuge_air = new HashMap<>();
                refuge.put(1, topLocation.clone().add(0, 1, 1));
                refuge.put(2, topLocation.clone().add(0, 2, 1));


                if (structureType.equalsIgnoreCase("cross")) {
                    if (isValidSurfaceLocation(cross)){
                        return topLocation.clone().add(0, 1, 0);
                    }
                } else if (structureType.equalsIgnoreCase("refuge")){
                    if (isValidSurfaceLocation(refuge)){
                        return topLocation.clone();
                    }
                }
            }
        }

        return null;
    }

    private static boolean isValidSurfaceLocation(Map<Integer, Location> blocks) {
        for (Location loc : blocks.values()) {
            if (loc.getBlock().getType().isSolid()) {
                if (!loc.getBlock().getType().isBurnable()) {
                    return true;

                }
            }
        }
        return false;
    }
}
