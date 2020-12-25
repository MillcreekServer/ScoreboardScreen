package io.github.wysohn.scoreboardscreen.main;

import io.github.wysohn.rapidframework3.interfaces.language.ILang;

public enum scoreboardscreenLangs implements ILang {

    ;

    private final String[] def;

    scoreboardscreenLangs(String... def) {
        this.def = def;
    }

    @Override
    public String[] getEngDefault() {
        return def;
    }
}
