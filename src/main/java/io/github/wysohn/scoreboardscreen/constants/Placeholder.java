package io.github.wysohn.scoreboardscreen.constants;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.util.function.BiFunction;

public class Placeholder {
    private final ScriptContext scriptContext = new SimpleScriptContext();

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

    private boolean fail = false;

    public String parse(User sender, Context context, BiFunction<User, String, String> before) {
        if (result != null && animation != null && context.interval++ % animation.getTick() != 0) {
            return result;
        }
        if (context.interval < 0)
            context.interval = 0;

        result = before.apply(sender, value);

        if (!fail && animation != null) {
            try {
                String[] animations = animation.invoke(scriptContext, result, params);
                result = animations[context.phase++ % animations.length];
            } catch (NoSuchMethodException | ScriptException e) {
                fail = true;
                e.printStackTrace();
            }
        }

        if (context.phase < 0)
            context.phase = 0;

        return result;
    }

    public static class Context{
        private int interval = 0;
        private int phase = 0;
    }
}