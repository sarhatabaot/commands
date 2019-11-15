package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * @author sarhatabaot
 */
public class NukkitConditionContext extends ConditionContext<NukkitCommandIssuer> {
    public NukkitConditionContext(NukkitCommandIssuer issuer, String config) {
        super(issuer, config);
    }

    public CommandSender getSender() {
        return getIssuer().getIssuer();
    }

    public Player getPlayer() {
        return getIssuer().getPlayer();
    }
}
