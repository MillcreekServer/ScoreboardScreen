package io.github.wysohn.scoreboardscreen.constants;

import org.bukkit.configuration.ConfigurationSection;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BoardTemplate {
    private static ScriptEngineManager SEM;

    public final Placeholder title;
    public final List<Line> scoreboard = new ArrayList<>();

    public BoardTemplate(ConfigurationSection config, File jsFolder) throws ScriptException, FileNotFoundException {
        ConfigurationSection titleSection = config.getConfigurationSection("Title");
        title = extractPlaceHolder(Objects.requireNonNull(titleSection), jsFolder);

        ConfigurationSection objectives = config.getConfigurationSection("Objectives");
        if (objectives != null) {
            scoreboard.clear();
            for (Map.Entry<String, Object> entry : objectives.getValues(false).entrySet()) {
                String key = entry.getKey();
                ConfigurationSection section = (ConfigurationSection) entry.getValue();

                Placeholder ph = extractPlaceHolder(section, jsFolder);
                scoreboard.add(new Line(key, ph));
            }
        }

        if(SEM == null){
            SEM = new ScriptEngineManager();
        }
    }

    private Placeholder extractPlaceHolder(ConfigurationSection section, File jsFolder)
            throws FileNotFoundException, ScriptException {
        String value = section.getString("Value");
        String animation = section.getString("Animation.Value");
        int tick = section.getInt("Animation.Tick", 5);

        Placeholder ph = null;
        if (animation == null) {
            ph = new Placeholder(value);
        } else {
            String[] splits = animation.split(" ");
            String jsNames = splits[0];
            List<String> args = new ArrayList<String>();
            if (splits.length > 1) {
                for (int i = 1; i < splits.length; i++) {
                    args.add(splits[i]);
                }
            }

            ScriptEngine engine = SEM.getEngineByName("graal.js");
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowAllAccess", true);
            Animation anim = new Animation(engine, new File(jsFolder, jsNames), tick);

            if (args.isEmpty())
                ph = new Placeholder(value, anim);
            else
                ph = new Placeholder(value, anim, args.toArray(new String[0]));
        }
        return ph;
    }

    public int numLines() {
        return scoreboard.size();
    }

    public static class Line {
        public final String name;
        public final Placeholder value;

        public Line(String name, Placeholder value) {
            super();
            this.name = name;
            this.value = value;
        }

    }
}
