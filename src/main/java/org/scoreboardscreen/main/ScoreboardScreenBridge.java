package org.scoreboardscreen.main;

import io.github.wysohn.rapidframework2.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework2.bukkit.main.BukkitPluginBridge;
import io.github.wysohn.rapidframework2.core.interfaces.plugin.IPluginManager;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.command.SubCommand;
import org.bukkit.ChatColor;
import org.scoreboardscreen.ScoreboardMediator;
import org.scoreboardscreen.manager.ScoreboardManager;
import org.scoreboardscreen.manager.UserManager;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ScoreboardScreenBridge extends BukkitPluginBridge {
    public ScoreboardScreenBridge(AbstractBukkitPlugin bukkit) {
        super(bukkit);
    }

    public ScoreboardScreenBridge(String pluginName, String pluginDescription, String mainCommand, String adminPermission, Logger logger, File dataFolder, IPluginManager iPluginManager, AbstractBukkitPlugin bukkit) {
        super(pluginName, pluginDescription, mainCommand, adminPermission, logger, dataFolder, iPluginManager, bukkit);
    }

    @Override
    protected PluginMain init(PluginMain.Builder builder) {
        return builder
                .addLangs(ScoreboardScreenLanguage.values())
                .withManagers(new UserManager(PluginMain.Manager.NORM_PRIORITY))
                .withManagers(new ScoreboardManager(PluginMain.Manager.NORM_PRIORITY - 1))
                .withMediators(new ScoreboardMediator())
                .build();
    }

    @Override
    protected void registerCommands(List<SubCommand> commands) {
        commands.add(new SubCommand.Builder(getMain(), "toggle", 0)
                .withDescription(ScoreboardScreenLanguage.Command_Toggle_Description)
                .addUsage(ScoreboardScreenLanguage.Command_Toggle_Usage)
                .action(((sender, args) -> {
                    getMain().getMediator(ScoreboardMediator.class).ifPresent(scoreboardMediator -> {
                        Optional.ofNullable(scoreboardMediator.getUser(sender.getUuid()))
                                .ifPresent(board -> {
                                    if (board.toggleScoreboard()) {
                                        sender.sendMessageRaw(ChatColor.GREEN + "ON");
                                    } else {
                                        sender.sendMessageRaw(ChatColor.RED + "OFF");
                                    }
                                });
                    });

                    return true;
                }))
                .create());
    }
}
