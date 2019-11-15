package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * @author sarhatabaot
 */
public class NukkitCommandExecutionContext extends CommandExecutionContext<NukkitCommandExecutionContext, NukkitCommandIssuer> {
    public NukkitCommandExecutionContext(RegisteredCommand cmd, CommandParameter param, NukkitCommandIssuer sender, List<String> args,
                                         int index, Map<String, Object> passedArgs) {
        super(cmd, param, sender, args, index, passedArgs);
    }

    public CommandSender getSender() {
        return this.issuer.getIssuer();
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
