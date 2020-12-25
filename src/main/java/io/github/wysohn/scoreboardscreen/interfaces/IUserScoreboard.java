package io.github.wysohn.scoreboardscreen.interfaces;

import java.util.List;

public interface IUserScoreboard {
    boolean toggleScoreboard();

    void setPrefix(String teamName, String prefix);

    void setSuffix(String teamName, String suffix);

    void setTeamList(String teamName, List<String> teamMembers);

    void removeTeamList(String teamName);

    boolean hasTeam(String teamName);

    void clearAllTeamList();

    void update();
}
