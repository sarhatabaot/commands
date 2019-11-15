package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.DyeColor;
//import org.apache.commons.lang3.Validate;

import java.util.ArrayList;

/**
 * @author sarhatabaot
 */
public class NukkitCommandCompletions extends CommandCompletions<NukkitCommandCompletionContext> {
    public NukkitCommandCompletions(NukkitCommandManager manager) {
        super(manager);
        registerAsyncCompletion("dyecolors", c -> ACFUtil.enumNames(DyeColor.values()));
        registerCompletion("players", c -> {
            CommandSender sender = c.getSender();
            //Validate.notNull(sender, "Sender cannot be null");
            Player senderPlayer = sender instanceof Player ? (Player) sender : null;

            ArrayList<String> matchedPlayers = new ArrayList<>();
            for (Player player : manager.plugin.getServer().getOnlinePlayers().values()) {
                String name = player.getName();
                if ((senderPlayer == null || senderPlayer.canSee(player)) && ACFNukkitUtil.startsWithIgnoreCase(name, c.getInput())) {
                    matchedPlayers.add(name);
                }
            }


            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            return matchedPlayers;
        });
    }
}
