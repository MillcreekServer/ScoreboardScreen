package io.github.wysohn.scoreboardscreen.interfaces;

import java.util.List;

public interface ITeamInfo {
    String teamName();

    List<String> teamMemberNames();

    String prefix();

    String suffix();
}
