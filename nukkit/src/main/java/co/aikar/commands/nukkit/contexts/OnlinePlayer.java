package co.aikar.commands.nukkit.contexts;

import cn.nukkit.Player;

import java.util.Objects;

/**
 * @author sarhatabaot
 */
public class OnlinePlayer {
    public final Player player;

    public OnlinePlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnlinePlayer that = (OnlinePlayer) o;
        return Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }

    @Override
    public String toString() {
        return "OnlinePlayer{player=" + player + '}';
    }
}
