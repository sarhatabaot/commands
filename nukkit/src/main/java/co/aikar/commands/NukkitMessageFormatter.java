package co.aikar.commands;

import cn.nukkit.utils.TextFormat;

/**
 * @author sarhatabaot
 */
public class NukkitMessageFormatter extends MessageFormatter<TextFormat> {
    public NukkitMessageFormatter(TextFormat... colors) {
        super(colors);
    }

    @Override
    String format(TextFormat color, String message) {
        return null;
    }
}
