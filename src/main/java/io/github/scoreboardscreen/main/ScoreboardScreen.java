package io.github.scoreboardscreen.main;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.assistedinject.FactoryProvider;
import io.github.scoreboardscreen.constants.User;
import io.github.scoreboardscreen.constants.UserScoreboard;
import io.github.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.scoreboardscreen.interfaces.IUserScoreboardFactory;
import io.github.scoreboardscreen.manager.ScoreboardManager;
import io.github.scoreboardscreen.manager.UserManager;
import io.github.wysohn.rapidframework3.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework3.core.command.SubCommand;
import io.github.wysohn.rapidframework3.core.inject.module.LanguagesModule;
import io.github.wysohn.rapidframework3.core.inject.module.ManagerModule;
import io.github.wysohn.rapidframework3.core.inject.module.MediatorModule;
import io.github.wysohn.rapidframework3.core.main.PluginMainBuilder;
import io.github.wysohn.rapidframework3.core.player.AbstractPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ScoreboardScreen extends AbstractBukkitPlugin {
    public ScoreboardScreen() {
    }

    ScoreboardScreen(Server mockServer) {
        super(mockServer);
    }

    @Override
    protected void init(PluginMainBuilder pluginMainBuilder) {
        pluginMainBuilder.addModule(new LanguagesModule(ScoreboardScreenLanguage.values()));
        pluginMainBuilder.addModule(new ManagerModule(
                UserManager.class,
                ScoreboardManager.class
        ));
        pluginMainBuilder.addModule(new MediatorModule(

        ));
        pluginMainBuilder.addModule(new AbstractModule() {
            @Override
            protected void configure() {
                install(new FactoryModuleBuilder()
                        .implement(IUserScoreboard.class, UserScoreboard.class)
                        .build(IUserScoreboardFactory.class));
            }
        });
    }

    @Override
    protected void registerCommands(List<SubCommand.Builder> list) {
        list.add(new SubCommand.Builder("toggle", 0)
                .withDescription(ScoreboardScreenLanguage.Command_Toggle_Description)
                .addUsage(ScoreboardScreenLanguage.Command_Toggle_Usage)
                .action(((sender, args) -> {
                    getMain().getManager(UserManager.class).flatMap(userManager ->
                            Optional.ofNullable(userManager.getBoard(sender.getUuid()))).ifPresent(board -> {
                        if (board.toggleScoreboard()) {
                            sender.sendMessageRaw(ChatColor.GREEN + "ON");
                        } else {
                            sender.sendMessageRaw(ChatColor.RED + "OFF");
                        }
                    });

                    return true;
                })));
    }

    @Override
    protected Optional<? extends AbstractPlayerWrapper> getPlayerWrapper(UUID uuid) {
        return Optional.of(uuid)
                .map(Bukkit::getPlayer)
                .map(player -> new User(uuid).setSender(player));
    }
}
