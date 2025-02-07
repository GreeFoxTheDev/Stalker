package greefox.stalker.events;

import greefox.stalker.Stalker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Night {
    private BukkitRunnable task;

    public void startEffects(Player player) {
        if (task != null && !task.isCancelled()) {
            return; // Prevent duplicate tasks
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                long time = player.getWorld().getTime();
                boolean isNight = (time > 12300 && time < 23850);
                boolean isUnderground = (player.getLocation().getY() < 40);

                // If it's neither night nor underground, stop the effect
                if (!isNight && !isUnderground) {
                    this.cancel();
                    task = null;
                    return;
                }

                // Apply darkness immediately
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 1, true, false));

                    // Schedule blindness 10 ticks later
                    Bukkit.getScheduler().runTaskLater(Stalker.getInstance(), () -> onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false)), 40L);
                }
            }
        };

        task.runTaskTimer(Stalker.getInstance(), 0L, 20L); // Runs every second
    }
}
