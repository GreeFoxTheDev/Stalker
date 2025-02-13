package greefox.stalker.events;

import greefox.stalker.Stalker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Night {

    public FileConfiguration config = Stalker.getInstance().getConfig();
    private BukkitRunnable task;

    public void startEffects(Player player) {
        if (task != null && !task.isCancelled()) {
            return;
        }

        boolean nightEffectsEnabled = config.getBoolean("effects.night_effects.enable", true);
        boolean nightDarkness = config.getBoolean("effects.night_effects.darkness", true);
        boolean nightBlindness = config.getBoolean("effects.night_effects.blindness", true);

        boolean caveEffectsEnabled = config.getBoolean("effects.cave_effects.enable", true);
        boolean caveDarkness = config.getBoolean("effects.cave_effects.darkness", true);
        boolean caveBlindness = config.getBoolean("effects.cave_effects.blindness", true);
        int caveDepth = config.getInt("effects.cave_effects.depth", 40);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                long time = player.getWorld().getTime();
                boolean isNight = (time > 12300 && time < 23000);
                boolean isUnderground = (player.getLocation().getY() < caveDepth);

                if ((!isNight || !nightEffectsEnabled) && (!isUnderground || !caveEffectsEnabled)) {
                    this.cancel();
                    task = null;
                    return;
                }

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (isNight && nightEffectsEnabled) {
                        if (nightDarkness) {
                            onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 1, true, false));
                        }
                        if (nightBlindness) {
                            Bukkit.getScheduler().runTaskLater(Stalker.getInstance(), () ->
                                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false)), 40L
                            );
                        }
                    }

                    if (isUnderground && caveEffectsEnabled) {
                        if (caveDarkness) {
                            onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 1, true, false));
                        }
                        if (caveBlindness) {
                            Bukkit.getScheduler().runTaskLater(Stalker.getInstance(), () ->
                                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false)), 40L
                            );
                        }
                    }
                }
            }
        };

        task.runTaskTimer(Stalker.getInstance(), 0L, 20L);
    }

}
