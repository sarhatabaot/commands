package co.aikar.commands;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sarhatabaot
 */
public class NukkitRootCommand extends Command implements RootCommand {
    private final NukkitCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    public NukkitRootCommand(NukkitCommandManager manager, String name) {
        super(name);
        this.manager = manager;
        this.name = name;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (commandLabel.contains(":")) commandLabel = ACFPatterns.COLON.split(commandLabel, 2)[1];
        execute(manager.getCommandIssuer(sender), commandLabel, args);
        return true;
    }

    @Override
    public String getDescription() {
        RegisteredCommand command = getDefaultRegisteredCommand();

        if (command != null && !command.getHelpText().isEmpty()) {
            return command.getHelpText();
        }
        if (command != null && command.scope.description != null) {
            return command.scope.description;
        }
        if (defCommand.description != null) {
            return defCommand.description;
        }
        return super.getDescription();
    }

    @Override
    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
        setPermission(getUniquePermission());
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public boolean testPermissionSilent(CommandSender target) {
        return hasAnyPermission(manager.getCommandIssuer(target));
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return children;
    }

    @Override
    public String getCommandName() {
        return name;
    }
}
