package io.github.wysohn.scoreboardscreen.constants;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Animation {
    private final ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("nashorn");
    private final int tick;

    public Animation(File script, int tick) throws FileNotFoundException, ScriptException {
        assert engine != null;
        this.tick = tick;

        FileReader fr = new FileReader(script);
        engine.eval(fr);
    }

    public Animation(File script) throws FileNotFoundException, ScriptException {
        this(script, 5);
    }

    public String[] invoke(String str, String... params) throws NoSuchMethodException, ScriptException {
        String combine = combineParams(params);
        return (String[]) engine.eval("Java.to(animate(" + "\"" + str + "\"" + combine + "), Java.type(\"java.lang.String[]\"));");
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
