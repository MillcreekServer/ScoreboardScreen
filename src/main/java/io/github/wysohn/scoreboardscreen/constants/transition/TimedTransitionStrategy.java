package io.github.wysohn.scoreboardscreen.constants.transition;

import io.github.wysohn.rapidframework3.utils.Validation;
import io.github.wysohn.scoreboardscreen.interfaces.IBoardState;
import io.github.wysohn.scoreboardscreen.interfaces.ITransitionStrategy;

public class TimedTransitionStrategy implements ITransitionStrategy {
    private long duration;

    private long lastStateChange = -1;

    public TimedTransitionStrategy(long duration) {
        this.duration = duration;
        Validation.validate(duration, v -> v >= 0L, "must be at least 0L");
    }

    @Override
    public IBoardState nextState(IBoardState current) {
        // first transition
        if(lastStateChange < 0L) {
            lastStateChange = System.currentTimeMillis();
            return current;
        }

        // keep the current state until duration is passed
        if(lastStateChange + duration > System.currentTimeMillis()){
            return current;
        }

        // back to default state
        return null;
    }
}
