package io.github.scoreboardscreen.constants;

import io.github.wysohn.rapidframework3.bukkit.manager.api.PlaceholderAPI;
import io.github.wysohn.rapidframework3.core.api.ManagerExternalAPI;
import org.bukkit.entity.Player;

import javax.script.ScriptException;

public class Placeholder {
    private final ManagerExternalAPI api;

    final String value;
    final Animation animation;

    final String[] params;

    public Placeholder(ManagerExternalAPI api, String value) {
        this(api, value, null);
    }

    public Placeholder(ManagerExternalAPI api, String value, Animation animation) {
        this(api, value, animation, new String[0]);
    }

    public Placeholder(ManagerExternalAPI api, String value, Animation animation, String... params) {
        this.api = api;
        this.value = value;
        this.animation = animation;
        this.params = params;
    }

    private String result;
    private int interval = 0;
    private int phase = 0;

    private boolean fail = false;

    public String parse(Player sender) {
        if (result != null && animation != null && interval++ % animation.getTick() != 0) {
            return result;
        }
        if (interval < 0)
            interval = 0;

        result = api.getAPI(PlaceholderAPI.class).map(placeholderAPI ->
                placeholderAPI.parse(new User(sender.getUniqueId()).setSender(sender), value))
                .orElse(value);

        if (!fail && animation != null) {
            try {
                String[] animations = animation.invoke(result, params);
                result = animations[phase++ % animations.length];
            } catch (NoSuchMethodException | ScriptException e) {
                fail = true;
                e.printStackTrace();
            }
        }

        if (phase < 0)
            phase = 0;

        return result;
    }

}