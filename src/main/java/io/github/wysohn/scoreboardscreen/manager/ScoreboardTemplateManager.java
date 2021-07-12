package io.github.wysohn.scoreboardscreen.manager;

import io.github.wysohn.rapidframework3.bukkit.utils.Utf8YamlConfiguration;
import io.github.wysohn.rapidframework3.core.api.ManagerExternalAPI;
import io.github.wysohn.rapidframework3.core.inject.annotations.PluginDirectory;
import io.github.wysohn.rapidframework3.core.inject.annotations.PluginLogger;
import io.github.wysohn.rapidframework3.core.main.Manager;
import io.github.wysohn.rapidframework3.utils.JarUtil;
import io.github.wysohn.scoreboardscreen.constants.BoardTemplate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Singleton
public class ScoreboardTemplateManager extends Manager {
    private final File pluginDirectory;
    private final Logger logger;
    private final ManagerExternalAPI api;

    private final Map<String, BoardTemplate> templateMap = new ConcurrentHashMap<>();

    private File templateFolder;
    private File jsFolder;

    @Inject
    public ScoreboardTemplateManager(@PluginDirectory File pluginDirectory,
                                     @PluginLogger Logger logger,
                                     ManagerExternalAPI api) {
        this.pluginDirectory = pluginDirectory;
        this.logger = logger;
        this.api = api;
    }

    @Override
    public void enable() throws Exception {
        templateFolder = new File(pluginDirectory, "templates");
        JarUtil.copyFromJar(getClass(), "templates/*.yml", pluginDirectory, JarUtil.CopyOption.COPY_IF_NOT_EXIST);

        jsFolder = new File(pluginDirectory, "animation");
        JarUtil.copyFromJar(getClass(), "*.js", pluginDirectory, JarUtil.CopyOption.COPY_IF_NOT_EXIST);
    }

    @Override
    public void load() throws Exception {
        Map<String, BoardTemplate> loaded = new HashMap<>();
        Optional.of(templateFolder)
                .map(File::listFiles)
                .ifPresent(files -> Arrays.stream(files)
                        .filter(File::isFile)
                        .forEach(file -> {
                            YamlConfiguration config = new Utf8YamlConfiguration();
                            try {
                                config.load(file);
                            } catch (IOException | InvalidConfigurationException e) {
                                e.printStackTrace();
                                return;
                            }

                            try {
                                String name = file.getName();
                                loaded.put(name.substring(0, name.indexOf(".yml")), new BoardTemplate(config, jsFolder));
                            } catch (ScriptException | FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }));

        templateMap.clear();
        templateMap.putAll(loaded);
    }

    @Override
    public void disable() throws Exception {

    }

    /**
     *
     * @param templateName without .yml
     * @return template. Can be null
     */
    public BoardTemplate getTemplate(String templateName){
        return templateMap.get(templateName);
    }
}
