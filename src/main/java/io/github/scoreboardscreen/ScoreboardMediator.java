package io.github.scoreboardscreen;

import io.github.scoreboardscreen.constants.UserScoreboard;
import io.github.scoreboardscreen.manager.ScoreboardManager;
import io.github.scoreboardscreen.manager.UserManager;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScoreboardMediator extends PluginMain.Mediator {
    private ScoreboardManager scoreboardManager;
    private UserManager userManager;

    @Override
    public void enable() throws Exception {
        scoreboardManager = main().getManager(ScoreboardManager.class).get();
        userManager = main().getManager(UserManager.class).get();
    }

    @Override
    public void load() throws Exception {

    }

    @Override
    public void disable() throws Exception {

    }

    public void putUser(Player player) {
        if (userManager.getUsers().containsKey(player.getUniqueId()))
            return;

        UserScoreboard board = new UserScoreboard(scoreboardManager, player);

        userManager.getUsers().put(player.getUniqueId(), board);
    }

    public UserScoreboard getUser(UUID playerUuid) {
        return userManager.getUsers().get(playerUuid);
    }

    public void removeUser(UUID playerUuid) {
        UserScoreboard board = userManager.getUsers().remove(playerUuid);
        if (board != null) {
            board.interrupt();
        }
    }
}
