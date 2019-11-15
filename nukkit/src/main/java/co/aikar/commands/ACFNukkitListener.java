package co.aikar.commands;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.plugin.PluginDisableEvent;
import cn.nukkit.plugin.Plugin;

/**
 * @author sarhatabaot
 */
public class ACFNukkitListener implements Listener {
    private NukkitCommandManager manager;
    private final Plugin plugin;

    public ACFNukkitListener(NukkitCommandManager manager, Plugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (!(plugin.getName().equalsIgnoreCase(event.getPlugin().getName()))) {
            return;
        }
        manager.unregisterCommands();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.manager.autoDetectFromClient) {
            this.manager.readPlayerLocale(player);
            this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, () -> manager.readPlayerLocale(player), 20);
        } else {
            this.manager.setIssuerLocale(player, this.manager.getLocales().getDefaultLocale());
            this.manager.notifyLocaleChange(this.manager.getCommandIssuer(player), null, this.manager.getLocales().getDefaultLocale());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.issuersLocale.remove(event.getPlayer().getUniqueId());
    }
}
