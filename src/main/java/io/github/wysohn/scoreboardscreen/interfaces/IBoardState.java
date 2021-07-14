package io.github.wysohn.scoreboardscreen.interfaces;

import io.github.wysohn.scoreboardscreen.constants.User;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

/**
 * Represent an abstract board state.
 */
public interface IBoardState extends ITransitionStrategy {
    void registerBoard(Player player, Consumer<Runnable> runSynchronous);

    void updateTitle(User player, Consumer<Runnable> runSynchronous);

    void updateLines(User player, Consumer<Runnable> runSynchronous);

    void updateTeams(User player, Consumer<Runnable> runSynchronous);
}
