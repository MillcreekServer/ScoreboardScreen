package io.github.wysohn.scoreboardscreen.constants;


import io.github.wysohn.rapidframework3.bukkit.data.BukkitPlayer;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;

import java.util.UUID;

public class User extends BukkitPlayer {
    private boolean toggleState = true;

    private transient IUserScoreboard scoreboard;

    private User(){
        super(null);
    }

    public User(UUID key) {
        super(key);
    }

    public boolean isToggleState() {
        return toggleState;
    }

    public void setToggleState(boolean toggleState) {
        this.toggleState = toggleState;

        notifyObservers();
    }

    public IUserScoreboard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(IUserScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
}
