package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.level.Level;
import co.aikar.commands.nukkit.contexts.OnlinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author sarhatabaot
 *///TODO
public class NukkitCommandContexts extends CommandContexts<NukkitCommandExecutionContext> {
    public NukkitCommandContexts(CommandManager manager) {
        super(manager);

        registerContext(OnlinePlayer.class, c -> getOnlinePlayer(c.getIssuer(), c.popFirstArg(), c.isOptional()));
        registerContext(OnlinePlayer[].class, (c) -> {
            NukkitCommandIssuer issuer = c.getIssuer();
            final String search = c.popFirstArg();
            boolean allowMissing = c.hasFlag("allowmissing");
            Set<OnlinePlayer> players = new HashSet<>();
            Pattern split = ACFPatterns.COMMA;
            String splitter = c.getFlagValue("splitter", (String) null);
            if (splitter != null) {
                split = Pattern.compile(Pattern.quote(splitter));
            }
            for (String lookup : split.split(search)) {
                OnlinePlayer player = getOnlinePlayer(issuer, lookup, allowMissing);
                if (player != null) {
                    players.add(player);
                }
            }
            if (players.isEmpty() && !c.hasFlag("allowempty")) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", search);
                throw new InvalidCommandArgument(false);
            }
            return players.toArray(new OnlinePlayer[players.size()]);
        });
        registerIssuerAwareContext(CommandSender.class, NukkitCommandExecutionContext::getSender);
        registerIssuerAwareContext(Player.class, c -> {
            boolean isOptional = c.isOptional();
            CommandSender sender = c.getSender();
            boolean isPlayerSender = sender instanceof Player;
            if (!c.hasFlag("other")) {
                Player player = isPlayerSender ? (Player) sender : null;
                if (player == null && !isOptional) {
                    throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
                }
                PlayerInventory inventory = player != null ? player.getInventory() : null;
                if (inventory != null && c.hasFlag("itemheld") && !ACFNukkitUtil.isValidItem(inventory.getItem(inventory.getHeldItemSlot()))) {
                    throw new InvalidCommandArgument(MinecraftMessageKeys.YOU_MUST_BE_HOLDING_ITEM, false);
                }
                return player;
            } else {
                String arg = c.popFirstArg();
                if (arg == null && isOptional) {
                    if (c.hasFlag("defaultself")) {
                        if (isPlayerSender) {
                            return (Player) sender;
                        } else {
                            throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
                        }
                    } else {
                        return null;
                    }
                } else if (arg == null) {
                    throw new InvalidCommandArgument();
                }

                OnlinePlayer onlinePlayer = getOnlinePlayer(c.getIssuer(), arg, isOptional);
                return onlinePlayer != null ? onlinePlayer.getPlayer() : null;
            }
        });

    }

    @Nullable
    OnlinePlayer getOnlinePlayer(NukkitCommandIssuer issuer, String lookup, boolean allowMissing) throws InvalidCommandArgument {
        Player player = ACFNukkitUtil.findPlayerSmart(issuer, lookup);
        //noinspection Duplicates
        if (player == null) {
            if (allowMissing) {
                return null;
            }
            throw new InvalidCommandArgument(false);
        }
        return new OnlinePlayer(player);
    }
}
