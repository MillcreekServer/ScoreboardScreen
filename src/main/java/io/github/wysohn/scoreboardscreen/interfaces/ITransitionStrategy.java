package io.github.wysohn.scoreboardscreen.interfaces;

import io.github.wysohn.scoreboardscreen.constants.transition.TimedTransitionStrategy;

public interface ITransitionStrategy {
    /**
     * Get the next board state.
     *
     * @param current current state
     * @return the next state (can be itself); null to fallback to default state
     */
    IBoardState nextState(IBoardState current);

    static ITransitionStrategy timed(long millisecs){
        return new TimedTransitionStrategy(millisecs);
    }
}
