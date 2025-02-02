package greefox.stalker;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AngryVillager implements Listener {

    public AngryVillager(Stalker plugin) {
    }

    @EventHandler
    public void onVillagerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager && event.getDamager() instanceof Player) {
            Villager hurtVillager = (Villager) event.getEntity();
            Player attacker = (Player) event.getDamager();
            transformVillager(hurtVillager.getLocation(), attacker);
        }
    }

    public void transformVillager(Location location, Player targetPlayer) {
        World world = location.getWorld();
        double radius = 10.0; // Radius to find nearby villagers

        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(location, radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Villager) {
                Villager villager = (Villager) entity;

                villager.getWorld().strikeLightning(villager.getLocation());

                // Repeatedly deal damage to the player to simulate "attacking"
//                new BukkitRunnable() {
//                    int attacks = 0;
//
//                    @Override
//                    public void run() {
//                        if (attacks >= 5 || targetPlayer.isDead()) { // Stop after 5 "attacks"
//                            villager.setGlowing(false); // Calm down after attacking
//                            cancel();
//                            return;
//                        }
//                        villager.setTarget(targetPlayer);
//
//                        // Simulate damage if the player is still nearby
//                        if (villager.getLocation().distance(targetPlayer.getLocation()) <= 2) {
//                            targetPlayer.damage(3.0); // Deal 1 heart of damage
//                        }
//                        attacks++;
//                    }
//                }.runTaskTimer(Stalker.getInstance(), 0L, 40L); // Attack every 2 seconds
            }
        }
    }
}
