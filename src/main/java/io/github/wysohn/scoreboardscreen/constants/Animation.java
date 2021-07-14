package io.github.wysohn.scoreboardscreen.constants;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Animation {
    private final ScriptEngine engine;
    private final int tick;
    private final CompiledScript compiled;

    public Animation(ScriptEngine engine, File script, int tick) throws FileNotFoundException, ScriptException {
        this.engine = engine;
        this.tick = tick;

        FileReader fr = new FileReader(script);
        compiled = ((Compilable) engine).compile(fr);
    }

    public Animation(ScriptEngine engine, File script) throws FileNotFoundException, ScriptException {
        this(engine, script, 5);
    }

    public String[] invoke(ScriptContext context, String str, String... params) throws NoSuchMethodException, ScriptException {
        compiled.eval(context);

        String combine = combineParams(params);
        return (String[]) engine.eval("Java.to(animate(" + "\"" + str + "\"" + combine + "), Java.type(\"java.lang.String[]\"));",
                context);
    }

    private String combineParams(Object[] params) {
        if (params.length == 0)
            return "";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length; i++)
            builder.append(",\"" + params[i] + "\"");

        return builder.toString();
    }

    public int getTick() {
        return tick;
    }
}
