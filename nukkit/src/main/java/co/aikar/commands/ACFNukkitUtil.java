package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.utils.TextFormat;
//import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sarhatabaot
 */
public class ACFNukkitUtil {
    public static String color(String message) {
        return TextFormat.colorize(message);
    }

    static boolean isValidItem(Item item) {
        return item != null && item.getId() != BlockID.AIR && item.getCount() > 0;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

    private static void findMatches(String search, CommandSender requester, List<Player> matches, List<Player> confirmList) {
        // Remove vanished players from smart matching.
        Iterator<Player> iter = matches.iterator();
        //noinspection Duplicates
        while (iter.hasNext()) {
            Player player = iter.next();
            if (requester instanceof Player && !((Player) requester).canSee(player)) {
                if (requester.hasPermission("acf.seevanish")) {
                    if (!search.endsWith(":confirm")) {
                        confirmList.add(player);
                        iter.remove();
                    }
                } else {
                    iter.remove();
                }
            }
        }
    }

    public static Player findPlayerSmart(CommandIssuer issuer, String search, Server server) {
        CommandSender requester = issuer.getIssuer();
        if (search == null) {
            return null;
        }
        String name = ACFUtil.replace(search, ":confirm", "");

        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<Player> matches = Arrays.asList(server.matchPlayer(name));
        List<Player> confirmList = new ArrayList<>();
        findMatches(search, requester, matches, confirmList);


        if (matches.size() > 1 || confirmList.size() > 1) {
            String allMatches = matches.stream().map(Player::getName).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        //noinspection Duplicates
        if (matches.isEmpty()) {
            Player player = ACFUtil.getFirstElement(confirmList);
            if (player == null) {
                issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", name);
                return null;
            } else {
                issuer.sendInfo(MinecraftMessageKeys.PLAYER_IS_VANISHED_CONFIRM, "{vanished}", player.getName());
                return null;
            }
        }

        return matches.get(0);
    }

    /**
     * This method uses a region to check case-insensitive equality. This
     * means the internal array does not need to be copied like a
     * toLowerCase() call would.
     *
     * @param string String to check
     * @param prefix Prefix of string to compare
     * @return true if provided string starts with, ignoring case, the prefix
     * provided
     * @throws NullPointerException     if prefix is null
     * @throws IllegalArgumentException if string is null
     */
    public static boolean startsWithIgnoreCase(@NotNull final String string, final String prefix) throws IllegalArgumentException, NullPointerException {
        //Validate.notNull(string, "Cannot check a null string for a match");
        if (string.length() < prefix.length()) {
            return false;
        }
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
