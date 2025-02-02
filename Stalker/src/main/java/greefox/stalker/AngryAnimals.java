package greefox.stalker;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AngryAnimals implements Listener {

    public AngryAnimals(Stalker plugin) {
    }

    @EventHandler
    public void onAnimalHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Cow || event.getEntity() instanceof Sheep || event.getEntity() instanceof Pig && event.getDamager() instanceof Player) {
            Entity hurtEntity = event.getEntity();
            Player attacker = (Player) event.getDamager();
            agroMobs(hurtEntity.getLocation(), attacker);
        }
    }

    public void agroMobs(Location location, Player targetPlayer) {
        World world = location.getWorld();
        double radius = 10.0; // Radius to find nearby villagers

        List<Entity> nearbyEntities = (List<Entity>) world.getNearbyEntities(location, radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Cow || entity instanceof Sheep || entity instanceof Pig) {



                new BukkitRunnable() {
                    int attacks = 0;

                    @Override
                    public void run() {
                        if (attacks >= 5 || targetPlayer.isDead()) { // Stop after 5 "attacks"
                            entity.setGlowing(false); // Calm down after attacking
                            cancel();
                            return;
                        }
                        entity.setGlowing(true);
                        ((Animals) entity).setTarget(targetPlayer);
                        ((Animals) entity).playHurtAnimation(10);


                        // Simulate damage if the player is still nearby
                        if (entity.getLocation().distance(targetPlayer.getLocation()) <= 3) {
                            targetPlayer.damage(3.0); // Deal 1 heart of damage
                        }
                        attacks++;
                    }
                }.runTaskTimer(Stalker.getInstance(), 0L, 40L); // Attack every 2 seconds
            }
        }
    }
}
