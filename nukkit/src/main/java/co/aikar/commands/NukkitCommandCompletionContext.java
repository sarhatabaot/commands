package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

/**
 * @author sarhatabaot
 */
public class NukkitCommandCompletionContext extends CommandCompletionContext<NukkitCommandIssuer> {
    public NukkitCommandCompletionContext(RegisteredCommand command, NukkitCommandIssuer issuer, String input, String config, String[] args) {
        super(command, issuer, input, config, args);
    }

    public CommandSender getSender() {
        return this.getIssuer().getIssuer();
    }

    /**
     * Returns the Player object if this Issuer is a Player
     *
     * @return
     */
    public Player getPlayer() {
        return this.issuer.getPlayer();
    }
}
