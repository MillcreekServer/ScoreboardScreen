package io.github.scoreboardscreen.manager;

import io.github.scoreboardscreen.constants.UserScoreboard;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import io.github.scoreboardscreen.ScoreboardMediator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager extends PluginMain.Manager implements Listener {
    private final long INTERVAL_MILLIS;

    private Map<UUID, UserScoreboard> users = new ConcurrentHashMap<UUID, UserScoreboard>();

    private Thread scoreboardUpdateThread;

    public UserManager(int loadPriority) {
        this(loadPriority, 50L);
    }

    public UserManager(int loadPriority, long INTERVAL_MILLIS) {
        super(loadPriority);
        this.INTERVAL_MILLIS = INTERVAL_MILLIS;
    }

    @Override
    public void enable() throws Exception {
        scoreboardUpdateThread = new ScoreboardUpdateThread(INTERVAL_MILLIS);
        scoreboardUpdateThread.start();
    }

    @Override
    public void load() throws Exception {
        if (scoreboardUpdateThread != null)
            scoreboardUpdateThread.interrupt();

        Bukkit.getOnlinePlayers().forEach(player -> main().getMediator(ScoreboardMediator.class)
                .ifPresent(scoreboardMediator -> {
                    scoreboardMediator.removeUser(player.getUniqueId());
                    scoreboardMediator.putUser(player);
                }));

        scoreboardUpdateThread = new ScoreboardUpdateThread(INTERVAL_MILLIS);
        scoreboardUpdateThread.start();
    }

    @Override
    public void disable() throws Exception {
        if (scoreboardUpdateThread != null)
            scoreboardUpdateThread.interrupt();
    }

    public Map<UUID, UserScoreboard> getUsers() {
        return users;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        main().getMediator(ScoreboardMediator.class).ifPresent(scoreboardMediator ->
                scoreboardMediator.removeUser(player.getUniqueId()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        main().getMediator(ScoreboardMediator.class).ifPresent(scoreboardMediator ->
                scoreboardMediator.putUser(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        main().getBridge().getTaskSupervisor().runAsync(() -> {
            Thread.sleep(50L);

            main().getBridge().getTaskSupervisor().runSync(() -> {
                main().getMediator(ScoreboardMediator.class).ifPresent(scoreboardMediator ->
                        scoreboardMediator.putUser(player));
                return null;
            }).get();
            return null;
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        main().getMediator(ScoreboardMediator.class).ifPresent(scoreboardMediator ->
                scoreboardMediator.removeUser(player.getUniqueId()));
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
                    for (Entry<UUID, UserScoreboard> entry : users.entrySet()) {
                        entry.getValue().run();
                    }

                    Thread.sleep(intervalMillis);
                }
            } catch (InterruptedException ex) {
                main().getLogger().info("Scoreboard update is interrupted.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
