package io.github.wysohn.scoreboardscreen.manager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.wysohn.rapidframework3.bukkit.manager.user.AbstractUserManager;
import io.github.wysohn.rapidframework3.core.database.Databases;
import io.github.wysohn.rapidframework3.core.inject.annotations.PluginDirectory;
import io.github.wysohn.rapidframework3.core.inject.annotations.PluginLogger;
import io.github.wysohn.rapidframework3.core.main.ManagerConfig;
import io.github.wysohn.rapidframework3.interfaces.plugin.IShutdownHandle;
import io.github.wysohn.rapidframework3.interfaces.plugin.ITaskSupervisor;
import io.github.wysohn.rapidframework3.interfaces.serialize.ISerializer;
import io.github.wysohn.rapidframework3.interfaces.serialize.ITypeAsserter;
import io.github.wysohn.scoreboardscreen.constants.User;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboardFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.lang.ref.Reference;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
public class UserManager extends AbstractUserManager<User> implements Listener {
    private final long INTERVAL_MILLIS = 50L;
    private final ITaskSupervisor task;
    private final Logger logger;
    private final IUserScoreboardFactory scoreboardFactory;

    private Thread scoreboardUpdateThread;

    @Inject
    public UserManager(@Named("pluginName") String pluginName,
                       @PluginLogger Logger logger,
                       ManagerConfig config,
                       @PluginDirectory File pluginDir,
                       IShutdownHandle shutdownHandle,
                       ISerializer serializer,
                       ITypeAsserter asserter,
                       Injector injector,
                       ITaskSupervisor task,
                       IUserScoreboardFactory scoreboardFactory) {
        super(pluginName, logger, config, pluginDir, shutdownHandle, serializer, asserter, injector, User.class);
        this.task = task;
        this.logger = logger;
        this.scoreboardFactory = scoreboardFactory;
    }

    @Override
    protected Databases.DatabaseFactory createDatabaseFactory() {
        return getDatabaseFactory("User");
    }

    @Override
    protected User newInstance(UUID uuid) {
        return new User(uuid);
    }

    @Override
    public void load() throws Exception {
        super.load();

        if (scoreboardUpdateThread != null)
            scoreboardUpdateThread.interrupt();

        scoreboardUpdateThread = new ScoreboardUpdateThread(INTERVAL_MILLIS);
        scoreboardUpdateThread.start();
    }

    @Override
    public void disable() throws Exception {
        if (scoreboardUpdateThread != null)
            scoreboardUpdateThread.interrupt();

        super.disable();
    }

    @EventHandler
    @Override
    public void onJoin(PlayerJoinEvent event) {
        super.onJoin(event);

        Player player = event.getPlayer();
        get(player.getUniqueId())
                .map(Reference::get)
                .ifPresent(user -> user.setScoreboard(scoreboardFactory.create(player)));
    }

    private class ScoreboardUpdateThread extends Thread {
        private final long intervalMillis;

        private ScoreboardUpdateThread(long intervalMillis) {
            this.intervalMillis = intervalMillis;

            setPriority(NORM_PRIORITY - 1);
            setName("ScoreboardUpdateThread");
        }

        @Override
        public void run() {
            logger.info("Scoreboard update is started.");
            try {
                while (this.isAlive() && !this.isInterrupted()) {
                    keySet().forEach(uuid -> get(uuid)
                            .map(Reference::get)
                            .map(User::getScoreboard)
                            .ifPresent(IUserScoreboard::update));
                    Thread.sleep(intervalMillis);
                }
            } catch (InterruptedException ex) {
                logger.info("Scoreboard update is interrupted.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
