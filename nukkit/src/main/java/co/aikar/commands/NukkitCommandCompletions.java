package co.aikar.commands;

import cn.nukkit.utils.DyeColor;

/**
 * @author sarhatabaot
 */
//TODO
public class NukkitCommandCompletions extends CommandCompletions<NukkitCommandCompletionContext> {
    public NukkitCommandCompletions(CommandManager manager) {
        super(manager);
        registerAsyncCompletion("mobs", c -> {

        });
        registerAsyncCompletion("dyecolors", c -> ACFUtil.enumNames(DyeColor.values()))
        registerCompletion("levels",  c -> {

        });
        registerCompletion("players", c -> {

        });
    }
}
