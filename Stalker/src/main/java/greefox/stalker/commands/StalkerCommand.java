package greefox.stalker.commands;

import greefox.stalker.Stalker;
import greefox.stalker.events.SpawnStalker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class StalkerCommand implements TabCompleter, CommandExecutor {
    private static final String[] ARGS = {"kill", "spawn", "show"};

    Stalker plugin;

    public StalkerCommand(Stalker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("stalker")) {
            if (args.length == 1 || args.length == 2) {

                if (sender instanceof Player player) {
                    if (sender.isOp()) {
                        if (args.length == 2) {
                            player = Bukkit.getPlayer(args[1]);
                        }
                        switch (args[0]) {
                            case "kill":
                                for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
                                    String name = entity.getCustomName();
                                    if (name != null && name.equalsIgnoreCase("stalker")) {
                                        entity.remove();
                                        sender.sendMessage("Stalker has been removed.");
                                    }
                                }
                                break;
                            case "spawn":
                                new SpawnStalker(Stalker.getInstance()).spawnStalker(player);
                                sender.sendMessage("Stalker has been spawned");
                                break;
                            case "show":
                                for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
                                    if (entity instanceof LivingEntity && entity.getCustomName() != null && entity.getCustomName().equalsIgnoreCase("stalker")) {
                                        ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 0, true));
                                        sender.sendMessage("Showing all stalkers to " + player.getName());
                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {


        final List<String> commands = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], Arrays.asList(ARGS), commands);
        Collections.sort(commands);
        if (args.length == 1) {
            return commands;
        }
        final ArrayList<String> finalNames = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        names.add("@a");

        String[] ARGS1 = new String[names.size()];
        for (Player p : Bukkit.getOnlinePlayers()) {
            String name = p.getName();
            names.add(name);
        }
        ARGS1 = names.toArray(ARGS1);
        StringUtil.copyPartialMatches(args[1], Arrays.asList(ARGS1), finalNames);
        if (args.length == 2) {
            return finalNames;
        }
        return new ArrayList<>();
    }

}
