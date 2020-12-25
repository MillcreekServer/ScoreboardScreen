package io.github.wysohn.scoreboardscreen.manager;

import io.github.wysohn.rapidframework3.core.inject.annotations.PluginLogger;
import io.github.wysohn.rapidframework3.core.main.Manager;
import io.github.wysohn.rapidframework3.interfaces.plugin.ITaskSupervisor;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboardFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Singleton
public class UserManager extends Manager implements Listener {
    private final long INTERVAL_MILLIS = 50L;
    private final ITaskSupervisor task;
    private final Logger logger;
    private final IUserScoreboardFactory scoreboardFactory;

    private final Map<UUID, IUserScoreboard> users = new ConcurrentHashMap<>();

    private Thread scoreboardUpdateThread;

    @Inject
    public UserManager(ITaskSupervisor task,
                       @PluginLogger Logger logger,
                       IUserScoreboardFactory scoreboardFactory) {
        this.task = task;
        this.logger = logger;
        this.scoreboardFactory = scoreboardFactory;
    }

    @Override
    public void enable() throws Exception {

    }

    @Override
    public void load() throws Exception {
        if (scoreboardUpdateThread != null)
            scoreboardUpdateThread.interrupt();

        Bukkit.getOnlinePlayers().forEach(player -> {
            users.remove(player.getUniqueId());
            users.put(player.getUniqueId(), scoreboardFactory.create(player));
        });

        scoreboardUpdateThread = new ScoreboardUpdateThread(INTERVAL_MILLIS);
        scoreboardUpdateThread.start();
    }

    @Override
    public void disable() throws Exception {
        if (scoreboardUpdateThread != null)
            scoreboardUpdateThread.interrupt();
    }

    public IUserScoreboard getBoard(UUID playerUuid){
        return users.get(playerUuid);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        users.remove(player.getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        lazyUpdate(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        lazyUpdate(player);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        lazyUpdate(player);
    }

    private void lazyUpdate(Player player) {
        task.async(() -> {
            Thread.sleep(50L);

            task.sync(() -> {
                if(users.containsKey(player.getUniqueId()))
                    return null;

                users.put(player.getUniqueId(), scoreboardFactory.create(player));
                return null;
            }).get();
            return null;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        users.remove(player.getUniqueId());
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
            try {
                while (this.isAlive() && !this.isInterrupted()) {
                    for (Entry<UUID, IUserScoreboard> entry : new HashMap<>(users).entrySet()) {
                        try {
                            entry.getValue().update();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            logger.warning(entry.getKey().toString());
                        }
                    }

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
