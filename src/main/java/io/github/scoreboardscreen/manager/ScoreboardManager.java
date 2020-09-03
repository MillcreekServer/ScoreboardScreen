package io.github.scoreboardscreen.manager;

import io.github.scoreboardscreen.constants.Animation;
import io.github.scoreboardscreen.constants.Placeholder;
import io.github.wysohn.rapidframework2.bukkit.utils.Utf8YamlConfiguration;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.tools.JarUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager extends PluginMain.Manager {
    private final Map<UUID, Board> boards = new ConcurrentHashMap<>();

    private File configFile;
    private YamlConfiguration config;

    private File jsFolder;

    public ScoreboardManager(int loadPriority) {
        super(loadPriority);
    }

    @Override
    public void enable() throws Exception {
        configFile = new File(main().getPluginDirectory(), "scoreboard.yml");
        JarUtil.copyFromJar(getClass(), "scoreboard.yml", main().getPluginDirectory(), JarUtil.CopyOption.COPY_IF_NOT_EXIST);

        jsFolder = new File(main().getPluginDirectory(), "animation");
        JarUtil.copyFromJar(getClass(), "*.js", main().getPluginDirectory(), JarUtil.CopyOption.COPY_IF_NOT_EXIST);
    }

    @Override
    public void load() throws Exception {
        config = new Utf8YamlConfiguration();
        config.load(configFile);

        for (Entry<UUID, Board> entry : boards.entrySet()) {
            init(entry.getValue());
        }
    }

    @Override
    public void disable() throws Exception {

    }

    public Placeholder getTitle(Player player) {
        Board board = boards.get(player.getUniqueId());
        if (board == null) {
            board = new Board();
            init(board);
            boards.put(player.getUniqueId(), board);
        }

        return board.title;
    }

    public List<Line> getScoreboard(Player player) {
        Board board = boards.get(player.getUniqueId());
        if (board == null) {
            board = new Board();
            init(board);
            boards.put(player.getUniqueId(), board);
        }

        return board.scoreboard;
    }

    private void init(Board board) {
        ConfigurationSection titleSection = config.getConfigurationSection("Title");
        if (titleSection != null) {
            try {
                board.title = extractPlaceHolder(titleSection);
            } catch (FileNotFoundException | ScriptException e1) {
                main().getLogger().warning("While reading Title:");
                main().getLogger().warning(e1.getMessage());
            }
        }

        ConfigurationSection objectives = config.getConfigurationSection("Objectives");
        if (objectives != null) {
            board.scoreboard.clear();
            for (Entry<String, Object> entry : objectives.getValues(false).entrySet()) {
                String key = entry.getKey();
                ConfigurationSection section = (ConfigurationSection) entry.getValue();

                try {
                    Placeholder ph = extractPlaceHolder(section);
                    board.scoreboard.add(new Line(key, ph));
                } catch (FileNotFoundException | ScriptException e) {
                    main().getLogger().warning("While reading element [" + key + "]:");
                    main().getLogger().warning(e.getMessage());
                }
            }
        }
    }

    private Placeholder extractPlaceHolder(ConfigurationSection section) throws FileNotFoundException, ScriptException {
        String value = section.getString("Value");
        String animation = section.getString("Animation.Value");
        int tick = section.getInt("Animation.Tick", 5);

        Placeholder ph = null;
        if (animation == null) {
            ph = new Placeholder(main(), value);
        } else {
            String[] splits = animation.split(" ");
            String jsNames = splits[0];
            List<String> args = new ArrayList<String>();
            if (splits.length > 1) {
                for (int i = 1; i < splits.length; i++) {
                    args.add(splits[i]);
                }
            }

            Animation anim = new Animation(new File(jsFolder, jsNames), tick);

            if (args.isEmpty())
                ph = new Placeholder(main(), value, anim);
            else
                ph = new Placeholder(main(), value, anim, args.toArray(new String[0]));
        }
        return ph;
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

    public static class Board {
        private Placeholder title;
        private final List<Line> scoreboard = new ArrayList<>();
    }
}
