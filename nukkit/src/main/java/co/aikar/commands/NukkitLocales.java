package co.aikar.commands;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import co.aikar.locales.MessageKey;

import java.util.Locale;

/**
 * @author sarhatabaot
 */
public class NukkitLocales extends Locales {
    private final NukkitCommandManager manager;

    public NukkitLocales(NukkitCommandManager manager) {
        super(manager);
        this.manager = manager;
        this.addBundleClassLoader(this.manager.getPlugin().getClass().getClassLoader());
    }

    @Override
    public void loadLanguages() {
        super.loadLanguages();
        String pluginName = "acf-" + manager.plugin.getDescription().getName();
        addMessageBundles("acf-minecraft", pluginName, pluginName.toLowerCase());
    }

    /**
     * Loads a file out of the plugins data folder by the given name
     *
     * @param file
     * @param locale
     * @return If any language keys were added
     */
    public boolean loadYamlLanguageFile(String file, Locale locale) {
        Config config = new Config(file);
        return loadLanguage(config, locale);
    }

    /**
     * Loads every message from the Configuration object. Any nested values will be treated as namespace
     * so acf-core:\n\tfoo: bar will be acf-core.foo = bar
     *
     * @param config
     * @param locale
     * @return If any language keys were added
     */
    public boolean loadLanguage(Config config, Locale locale) {
        boolean loaded = false;
        for (String parentKey : config.getKeys()) {
            ConfigSection inner = config.getSection(parentKey);
            if (inner == null) continue;
            for (String key : inner.getKeys()) {
                String value = inner.getString(key);
                if (value != null && !value.isEmpty()) {
                    addMessage(locale, MessageKey.of(parentKey + "." + key), value);
                    loaded = true;
                }
            }
        }
        return loaded;
    }
}
