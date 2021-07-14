package io.github.wysohn.scoreboardscreen.interfaces;

public interface IUserScoreboard {
    /**
     * Change current board state.
     *
     * Thread-safe operation
     *
     * @param state
     */
    void changeState(IBoardState state);

    void update();
}
