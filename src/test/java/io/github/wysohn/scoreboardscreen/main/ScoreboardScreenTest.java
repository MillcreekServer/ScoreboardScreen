package io.github.wysohn.scoreboardscreen.main;

import io.github.wysohn.rapidframework3.bukkit.testutils.SimpleBukkitPluginMainTest;
import junit.framework.TestCase;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScoreboardScreenTest extends TestCase {
    @Test
    public void testPlugin(){
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(uuid);

        List<Player> collection = new LinkedList<>();
        collection.add(player);

        Server server = new SimpleBukkitPluginMainTest<ScoreboardScreen>(){
            @Override
            public ScoreboardScreen instantiate(Server server) {
                try {
                    Field field = Bukkit.class.getDeclaredField("server");
                    field.setAccessible(true);
                    field.set(null, server);

                    when(server.getOnlinePlayers()).then(invocation -> collection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ScoreboardScreen(server);
            }
        }.enable();
    }
}