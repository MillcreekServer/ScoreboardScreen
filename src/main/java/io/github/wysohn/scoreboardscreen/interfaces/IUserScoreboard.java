package io.github.wysohn.scoreboardscreen.interfaces;

import javax.script.ScriptContext;

public interface IUserScoreboard extends ScriptContext {
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
