package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * @author sarhatabaot
 */
public class NukkitCommandIssuer implements CommandIssuer {
    private final NukkitCommandManager manager;
    private final CommandSender sender;

    public NukkitCommandIssuer(NukkitCommandManager manager, CommandSender sender) {
        this.manager = manager;
        this.sender = sender;
    }

    @Override
    public CommandSender getIssuer() {
        return sender;
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        if (isPlayer())
            return ((Player) sender).getUniqueId();
        return UUID.nameUUIDFromBytes(sender.getName().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public void sendMessageInternal(String message) {
        sender.sendMessage(ACFNukkitUtil.color(message));
    }

    public Player getPlayer() {
        return isPlayer() ? (Player) sender : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NukkitCommandIssuer that = (NukkitCommandIssuer) o;
        return Objects.equals(sender, that.sender);
    }
}
