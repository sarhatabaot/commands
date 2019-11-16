package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.scheduler.ServerScheduler;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sarhatabaot
 */
public class NukkitCommandManager extends CommandManager<
        CommandSender,
        NukkitCommandIssuer,
        TextFormat,
        NukkitMessageFormatter,
        NukkitCommandExecutionContext,
        NukkitConditionContext
        > {

    protected final Plugin plugin;
    private final SimpleCommandMap commandMap;
    private final TaskHandler localeTask;
    private final Logger logger;
    protected Map<String, Command> knownCommands = new HashMap<>();
    protected Map<String, NukkitRootCommand> registeredCommands = new HashMap<>();
    protected NukkitCommandContexts contexts;
    protected NukkitCommandCompletions completions;
    protected NukkitLocales locales;
    private boolean cantReadLocale = false;
    protected boolean autoDetectFromClient = true;

    public NukkitCommandManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = Logger.getLogger(plugin.getName());
        this.commandMap = hookCommandMap();
        defaultFormatter = new NukkitMessageFormatter(TextFormat.RED, TextFormat.YELLOW, TextFormat.RED);
        this.formatters.put(MessageType.ERROR, defaultFormatter);
        this.formatters.put(MessageType.SYNTAX, new NukkitMessageFormatter(TextFormat.YELLOW, TextFormat.GREEN, TextFormat.WHITE));
        this.formatters.put(MessageType.INFO, new NukkitMessageFormatter(TextFormat.BLUE, TextFormat.DARK_GREEN, TextFormat.GREEN));
        this.formatters.put(MessageType.HELP, new NukkitMessageFormatter(TextFormat.AQUA, TextFormat.GREEN, TextFormat.YELLOW));
        //no command api
        plugin.getServer().getPluginManager().registerEvents(new ACFNukkitListener(this, plugin), plugin);
        getLocales();
        this.localeTask = plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, () -> {
            if (this.cantReadLocale || !this.autoDetectFromClient) {
                return;
            }
            plugin.getServer().getOnlinePlayers().forEach(this::readPlayerLocale);
        }, 5, 5);

        registerDependency(plugin.getClass(), plugin);
        registerDependency(Logger.class, plugin.getLogger());
        registerDependency(Config.class, plugin.getConfig());
        registerDependency(Config.class, "Config", plugin.getConfig());
        registerDependency(Plugin.class, plugin);
        registerDependency(PluginBase.class, plugin);
        registerDependency(PluginManager.class, plugin.getServer().getPluginManager());
        registerDependency(Server.class, plugin.getServer());
        registerDependency(ServerScheduler.class, plugin.getServer().getScheduler());
        //registerDependency(ScoreboardManager.class,); not natively supported in nukkit
        //registerDependency(ItemFactory);
    }

    public Command getCommand(String name) {
        return this.commandMap.getCommands().get(name.toLowerCase(java.util.Locale.ENGLISH));
    }

    @NotNull
    public SimpleCommandMap hookCommandMap() {
        SimpleCommandMap map = plugin.getServer().getCommandMap();
        this.knownCommands = map.getCommands();
        return commandMap;
    }

    @Override
    public synchronized CommandContexts<NukkitCommandExecutionContext> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new NukkitCommandContexts(this);
        }
        return contexts;
    }

    @Override
    public synchronized CommandCompletions<NukkitCommandCompletionContext> getCommandCompletions() {
        if (this.completions == null) {
            this.completions = new NukkitCommandCompletions(this);
        }
        return completions;
    }

    @Override
    public void registerCommand(BaseCommand command) {
        registerCommand(command, false);
    }

    public void registerCommand(BaseCommand command, boolean force) {
        final String plugin = this.plugin.getName().toLowerCase();
        command.onRegister(this);
        for (Map.Entry<String, RootCommand> entry : command.registeredCommands.entrySet()) {
            String commandName = entry.getKey().toLowerCase();
            NukkitRootCommand nukkitCommand = (NukkitRootCommand) entry.getValue();
            if (!nukkitCommand.isRegistered) {
                this.plugin.getLogger().debug(commandName);
                Command oldCommand = getCommand(commandName);
                //Command oldCommand = commandMap.getCommand(commandName); //TODO: NPE Here
                if (oldCommand instanceof PluginIdentifiableCommand && ((PluginIdentifiableCommand) oldCommand).getPlugin() == this.plugin) {
                    knownCommands.remove(commandName);
                    oldCommand.unregister(commandMap);
                } else if (oldCommand != null && force) {
                    knownCommands.remove(commandName);
                    for (Map.Entry<String, Command> ce : knownCommands.entrySet()) {
                        String key = ce.getKey();
                        Command value = ce.getValue();
                        if (key.contains(":") && oldCommand.equals(value)) {
                            String[] split = ACFPatterns.COLON.split(key, 2);
                            if (split.length > 1) {
                                oldCommand.unregister(commandMap);
                                oldCommand.setLabel(split[0] + ":" + command.getName());
                                oldCommand.register(commandMap);
                            }
                        }
                    }
                }
                commandMap.register(plugin, nukkitCommand, commandName);
            }
            nukkitCommand.isRegistered = true;
            registeredCommands.put(commandName, nukkitCommand);
        }
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !registeredCommands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return CommandSender.class.isAssignableFrom(type);
    }

    @Override
    public NukkitCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof CommandSender)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        }
        return new NukkitCommandIssuer(this, (CommandSender) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new NukkitRootCommand(this, cmd);
    }

    @Override
    public Locales getLocales() {
        if (this.locales == null) {
            this.locales = new NukkitLocales(this);
            this.locales.loadLanguages();
        }
        return locales;
    }

    @Override
    public NukkitCommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new NukkitCommandExecutionContext(command, parameter, (NukkitCommandIssuer) sender, args, i, passedArgs);
    }

    @Override
    public NukkitCommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        return new NukkitCommandCompletionContext(command, (NukkitCommandIssuer) sender, input, config, args);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }

    public void unregisterCommands() {
        for (String key : new HashSet<>(registeredCommands.keySet())) {
            unregisterCommand(registeredCommands.get(key));
        }
    }

    public void unregisterCommand(BaseCommand command) {
        for (RootCommand rootcommand : command.registeredCommands.values()) {
            NukkitRootCommand nukkitCommand = (NukkitRootCommand) rootcommand;
            nukkitCommand.getSubCommands().values().removeAll(command.subCommands.values());
            if (nukkitCommand.isRegistered && nukkitCommand.getSubCommands().isEmpty()) {
                unregisterCommand(nukkitCommand);
                nukkitCommand.isRegistered = false;
            }
        }
    }

    @Deprecated
    public void unregisterCommand(NukkitRootCommand command) {
        final String plugin = this.plugin.getName().toLowerCase();
        command.unregister(commandMap);
        String key = command.getName();
        Command registered = knownCommands.get(key);
        if (command.equals(registered)) {
            knownCommands.remove(key);
        }
        knownCommands.remove(plugin + ":" + key);
        registeredCommands.remove(key);
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(registeredCommands.values());
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public Locale setPlayerLocale(Player player, Locale locale) {
        return this.setIssuerLocale(player, locale);
    }

    void readPlayerLocale(UUID uuid, Player player) {
        if (!player.isOnline() || cantReadLocale) {
            return;
        }
        Locale playerLocale = player.getLocale();
        String localeString = playerLocale.getLanguage();
        String[] split = ACFPatterns.UNDERSCORE.split((String) localeString);
        Locale locale = split.length > 1 ? new Locale(split[0], split[1]) : new Locale(split[0]);
        Locale prev = issuersLocale.put(player.getUniqueId(), locale);
        if (!Objects.equals(locale, prev)) {
            this.notifyLocaleChange(getCommandIssuer(player), prev, locale);
        }
    }
}
