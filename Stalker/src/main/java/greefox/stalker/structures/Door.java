package greefox.stalker.structures;

import greefox.stalker.Stalker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.metadata.FixedMetadataValue;

public class Door {

    public void placeOakDoor(Location bottomBlockLocation) {
        Block bottomBlock = bottomBlockLocation.getBlock();
        Block topBlock = bottomBlockLocation.clone().add(0, 1, 0).getBlock();

        bottomBlock.setType(Material.OAK_DOOR, false);
        topBlock.setType(Material.OAK_DOOR, false);

        BlockData bottomData = bottomBlock.getBlockData();
        if (bottomData instanceof org.bukkit.block.data.type.Door doorDataBottom) {
            doorDataBottom.setHalf(Bisected.Half.BOTTOM);
            doorDataBottom.setFacing(BlockFace.NORTH);
            doorDataBottom.setHinge(org.bukkit.block.data.type.Door.Hinge.LEFT);
            doorDataBottom.setPowered(false);
            bottomBlock.setBlockData(doorDataBottom, false);
        }

        BlockData topData = topBlock.getBlockData();
        if (topData instanceof org.bukkit.block.data.type.Door doorDataTop) {
            doorDataTop.setHalf(Bisected.Half.TOP);
            doorDataTop.setFacing(BlockFace.NORTH);
            doorDataTop.setHinge(org.bukkit.block.data.type.Door.Hinge.LEFT);
            doorDataTop.setPowered(false);
            topBlock.setBlockData(doorDataTop, false);
        }

        bottomBlock.setMetadata("custom_door", new FixedMetadataValue(Stalker.getInstance(), "special_door"));
        topBlock.setMetadata("custom_door", new FixedMetadataValue(Stalker.getInstance(), "special_door"));
    }
}
