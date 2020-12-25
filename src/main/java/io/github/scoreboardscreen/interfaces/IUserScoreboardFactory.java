package io.github.scoreboardscreen.interfaces;

import org.bukkit.entity.Player;

public interface IUserScoreboardFactory {
    IUserScoreboard create(Player player);
}
