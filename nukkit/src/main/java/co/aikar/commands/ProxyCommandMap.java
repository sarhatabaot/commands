package co.aikar.commands;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.SimpleCommandMap;

import java.util.List;
import java.util.Locale;

/**
 * @author sarhatabaot
 */
public class ProxyCommandMap extends SimpleCommandMap {
    private NukkitCommandManager manager;
    CommandMap proxied;

    public ProxyCommandMap(Server server, NukkitCommandManager manager, CommandMap proxied) {
        super(server);
        this.manager = manager;
        this.proxied = proxied;
    }

    @Override
    public void registerAll(String fallbackPrefix, List<? extends Command> commands) {
        proxied.registerAll(fallbackPrefix, commands);
    }

    @Override
    public boolean register(String fallbackPrefix, Command command, String label) {
        if (isOurCommand(command)) {
            return super.register(fallbackPrefix, command, label);
        } else {
            return proxied.register(fallbackPrefix, command, label);
        }
    }

    boolean isOurCommand(String cmdLine) {
        String[] args = ACFPatterns.SPACE.split(cmdLine);
        return args.length != 0 && isOurCommand(knownCommands.get(args[0].toLowerCase(Locale.ENGLISH)));

    }

    boolean isOurCommand(Command command) {
        return command instanceof RootCommand && ((RootCommand) command).getManager() == manager;
    }

    @Override
    public boolean register(String fallbackPrefix, Command command) {
        if (isOurCommand(command)) {
            return super.register(fallbackPrefix, command);
        } else {
            return proxied.register(fallbackPrefix, command);
        }
    }

    @Override
    public boolean dispatch(CommandSender sender, String cmdLine) {
        if (isOurCommand(cmdLine)) {
            return super.dispatch(sender, cmdLine);
        } else {
            return proxied.dispatch(sender, cmdLine);
        }
    }

    @Override
    public void clearCommands() {
        super.clearCommands();
        proxied.clearCommands();
    }

    @Override
    public Command getCommand(String name) {
        if (isOurCommand(name)) {
            return super.getCommand(name);
        } else {
            return proxied.getCommand(name);
        }
    }

}
