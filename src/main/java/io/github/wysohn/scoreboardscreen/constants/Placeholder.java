package io.github.wysohn.scoreboardscreen.constants;

import javax.script.ScriptException;
import java.util.function.BiFunction;

public class Placeholder {
    final String value;
    final Animation animation;

    final String[] params;

    public Placeholder(String value) {
        this(value, null);
    }

    public Placeholder(String value, Animation animation) {
        this(value, animation, new String[0]);
    }

    public Placeholder(String value, Animation animation, String... params) {
        this.value = value;
        this.animation = animation;
        this.params = params;
    }

    private String result;
    private int interval = 0;
    private int phase = 0;

    private boolean fail = false;

    public String parse(User sender, BiFunction<User, String, String> before) {
        if (result != null && animation != null && interval++ % animation.getTick() != 0) {
            return result;
        }
        if (interval < 0)
            interval = 0;

        result = before.apply(sender, result);

        if (!fail && animation != null) {
            try {
                String[] animations = animation.invoke(result, params);
                result = animations[phase++ % animations.length];
            } catch (NoSuchMethodException | ScriptException e) {
                fail = true;
                e.printStackTrace();
            }
        }

        if (phase < 0)
            phase = 0;

        return result;
    }

}