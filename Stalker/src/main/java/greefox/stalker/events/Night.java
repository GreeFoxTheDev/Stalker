package greefox.stalker.events;

import greefox.stalker.Stalker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Night {


    public void startEffects(Player player) {
        long time = player.getWorld().getTime();
        player.sendMessage("1");
        if (12300 < time && 23850 > time) {
            player.sendMessage("10");

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {

                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, true, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0, true, false));
                    }
                }
            };

            task.runTaskTimer(Stalker.getInstance(), 0L, 20L);
        }
    }
}
