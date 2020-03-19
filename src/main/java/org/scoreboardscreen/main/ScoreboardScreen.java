package org.scoreboardscreen.main;

import io.github.wysohn.rapidframework2.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework2.bukkit.main.BukkitPluginBridge;
import io.github.wysohn.rapidframework2.bukkit.main.objects.BukkitPlayer;
import io.github.wysohn.rapidframework2.core.interfaces.plugin.IPluginManager;
import io.github.wysohn.rapidframework2.core.manager.player.AbstractPlayerWrapper;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class ScoreboardScreen extends AbstractBukkitPlugin {
    @Override
    protected BukkitPluginBridge createCore() {
        return new ScoreboardScreenBridge(this);
    }

    @Override
    protected BukkitPluginBridge createCore(String pluginName, String pluginDescription, String mainCommand,
                                            String adminPermission, Logger logger, File dataFolder, IPluginManager iPluginManager) {
        return new ScoreboardScreenBridge(pluginName, pluginDescription, mainCommand, adminPermission, logger,
                dataFolder, iPluginManager, this);
    }

    @Override
    protected Optional<? extends AbstractPlayerWrapper> getPlayerWrapper(UUID uuid) {
        return Optional.ofNullable(Bukkit.getPlayer(uuid))
                .map(player -> new BukkitPlayer(player.getUniqueId()).setSender(player));
    }
}
