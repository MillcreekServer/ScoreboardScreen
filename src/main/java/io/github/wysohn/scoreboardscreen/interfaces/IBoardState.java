package io.github.wysohn.scoreboardscreen.interfaces;

import io.github.wysohn.scoreboardscreen.constants.User;
import org.bukkit.entity.Player;

/**
 * Represent an abstract board state.
 */
public interface IBoardState extends ITransitionStrategy {
    void registerBoard(Player player);

    void updateTitle(User player);

    void updateLines(User player);

    void updateTeams(User player);
}
