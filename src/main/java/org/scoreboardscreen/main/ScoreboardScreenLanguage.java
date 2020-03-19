package org.scoreboardscreen.main;

import io.github.wysohn.rapidframework2.core.manager.lang.Lang;

public enum ScoreboardScreenLanguage implements Lang {
    Command_Toggle_Description("Toggle scoreboard"),
    Command_Toggle_Usage("&6/scoreboardscreen &8- &7toggle on/off scoreboard",
            "&6/ss &8- &7t on/off scoreboard"),
    ;

    private String[] strs;

    ScoreboardScreenLanguage(String... strs) {
        this.strs = strs;
    }

    @Override
    public String[] getEngDefault() {
        return strs;
    }

}
