package io.github.wysohn.scoreboardscreen.main;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.github.wysohn.rapidframework3.bukkit.main.AbstractBukkitPlugin;
import io.github.wysohn.rapidframework3.bukkit.manager.api.PlaceholderAPI;
import io.github.wysohn.rapidframework3.core.command.SubCommand;
import io.github.wysohn.rapidframework3.core.inject.module.GsonSerializerModule;
import io.github.wysohn.rapidframework3.core.inject.module.LanguagesModule;
import io.github.wysohn.rapidframework3.core.inject.module.ManagerModule;
import io.github.wysohn.rapidframework3.core.inject.module.MediatorModule;
import io.github.wysohn.rapidframework3.core.main.PluginMainBuilder;
import io.github.wysohn.rapidframework3.core.player.AbstractPlayerWrapper;
import io.github.wysohn.scoreboardscreen.constants.BoardTemplate;
import io.github.wysohn.scoreboardscreen.constants.SimpleBoardState;
import io.github.wysohn.scoreboardscreen.constants.User;
import io.github.wysohn.scoreboardscreen.constants.UserScoreboard;
import io.github.wysohn.scoreboardscreen.interfaces.IBoardState;
import io.github.wysohn.scoreboardscreen.interfaces.ITransitionStrategy;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboardFactory;
import io.github.wysohn.scoreboardscreen.manager.ScoreboardTemplateManager;
import io.github.wysohn.scoreboardscreen.manager.UserManager;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.lang.ref.Reference;
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
                ScoreboardTemplateManager.class
        ));
        pluginMainBuilder.addModule(new MediatorModule(

        ));
        pluginMainBuilder.addModule(new GsonSerializerModule());
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
                    getMain().getManager(UserManager.class)
                            .flatMap(userManager -> userManager.get(sender.getUuid()))
                            .map(Reference::get)
                            .ifPresent(user -> {
                                user.setToggleState(!user.isToggleState());
                                if (user.isToggleState()) {
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
        return getUser(uuid);
    }

    public Optional<User> getUser(UUID uuid){
        return getMain().getManager(UserManager.class)
                .flatMap(userManager -> userManager.get(uuid))
                .map(Reference::get);
    }

    public SimpleBoardState createSimpleBoardState(BoardTemplate template, ITransitionStrategy transitionStrategy){
        return new SimpleBoardState(template, (user, before) -> Optional.ofNullable(getMain().api())
                .flatMap(a -> a.getAPI(PlaceholderAPI.class))
                .map(papi -> papi.parse(user, before))
                .orElse(before), transitionStrategy);
    }

    public void changeBoardState(Player player, IBoardState newState){
        getUser(player.getUniqueId())
                .map(User::getScoreboard)
                .ifPresent(board -> board.changeState(newState));
    }

    public BoardTemplate getTemplate(String templateName){
        return getMain().getManager(ScoreboardTemplateManager.class)
                .map(manager -> manager.getTemplate(templateName))
                .orElse(null);
    }
}
