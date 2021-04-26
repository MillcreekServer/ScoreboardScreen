package io.github.wysohn.scoreboardscreen.constants;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.wysohn.scoreboardscreen.manager.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserScoreboard implements IUserScoreboard {
    private static final int MAXIMUM_OBJECTIVE_NUM = 7;

    private final ScoreboardManager manager;
    private final Player player;

    private final String title;
    private final List<TeamLine> lines = new ArrayList<>();

    private final Scoreboard board;

    private final Objective objSide;
    private final Objective objBelow;

    private final Map<String, List<String>> teams = new TeamMap();
    private final Map<String, String> prefixes = new ConcurrentHashMap<String, String>();
    private final Map<String, String> suffixes = new ConcurrentHashMap<String, String>();

    @Inject
    public UserScoreboard(ScoreboardManager manager, @Assisted Player player) {
        this.manager = manager;
        this.player = player;

        title = manager.getTitle(player).parse(player);
        board = Bukkit.getScoreboardManager().getNewScoreboard();

        objSide = board.registerNewObjective(title, "dummy", title);
        objSide.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i > -15; i--) {
            Team team = board.registerNewTeam(String.valueOf(i));
            ChatColor color = ChatColor.values()[-i];
            team.addEntry(color.toString());
            lines.add(new TeamLine(color, team));
        }

        objBelow = board.registerNewObjective("showhealth", "health", "showhealth");
        objBelow.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objBelow.setDisplayName(ChatColor.DARK_RED + "/ 20");
        player.setHealth(player.getHealth());

        player.setScoreboard(board);
    }

    /**
     * @return true if board is on; false if board is off
     */
    @Override
    public boolean toggleScoreboard() {
        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
            return true;
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            return false;
        }
    }

    @Override
    public void setPrefix(String teamName, String prefix) {
        prefixes.put(teamName, prefix);
    }

    @Override
    public void setSuffix(String teamName, String suffix) {
        suffixes.put(teamName, suffix);
    }

    /**
     * @param teamName
     * @param teamMembers
     */
    @Override
    public void setTeamList(String teamName, List<String> teamMembers) {
        synchronized (teams) {
            if (teams.containsKey(teamName))
                return;

            teams.put(teamName, teamMembers);
        }
    }

    @Override
    public void removeTeamList(String teamName) {
        synchronized (teams) {
            teams.remove(teamName);
        }
    }

    @Override
    public boolean hasTeam(String teamName) {
        synchronized (teams) {
            return teams.containsKey(teamName);
        }
    }

    @Override
    public void clearAllTeamList() {
        synchronized (teams) {
            teams.clear();
        }
    }

    private void onTeamAdd(String name) {

    }

    private void onTeamRemove(String name) {
        Team team = board.getTeam(name);
        if (team != null)
            team.unregister();
    }

    @SuppressWarnings("serial")
    static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()) {{
        setTimeZone(TimeZone.getTimeZone("GMT+0"));
    }};
    static DecimalFormat df = new DecimalFormat("#,###,##0.00");

    @Override
    public void update() {
        if (!player.isOnline())
            return;

        String currentTitle = manager.getTitle(player).parse(player);

        // update title
        if(!currentTitle.equals(objSide.getDisplayName())){
            objSide.setDisplayName(currentTitle);
        }

        // update lines
        for (int index = 0; index < manager.getScoreboard(player).size(); index++) {
            if (index > lines.size() - 1)
                break;

            TeamLine valueLine = lines.get(index);
            objSide.getScore(String.valueOf(valueLine.color)).setScore(-index);

            ScoreboardManager.Line line = manager.getScoreboard(player).get(index);
            String value = line.value.parse(player);
            value = value.substring(0, Math.min(40, value.length()));
            setLine(valueLine.team, value);
        }

        // create or update teams
        for (Map.Entry<String, List<String>> entry : teams.entrySet()) {
            String teamName = entry.getKey();
            List<String> memberNames = entry.getValue();

            if (teamName.length() > 16)
                teamName = teamName.substring(0, 16);

            Team team = null;
            synchronized (board) {
                team = board.getTeam(teamName);
                if (team == null)
                    team = board.registerNewTeam(teamName);
            }

            Set<String> delete = new HashSet<>();
            // delete the unused team member
            for (String delMem : team.getEntries()) {
                if (!memberNames.contains(delMem))
                    delete.add(delMem);
            }
            for (String del : delete) {
                team.removeEntry(del);
            }

            Set<String> adding = new HashSet<>();
            // add new member if there is any
            for (String newMem : memberNames) {
                if (!team.hasEntry(newMem))
                    adding.add(newMem);
            }
            for (String add : adding) {
                team.addEntry(add);
            }

            String prefix = prefixes.get(teamName);
            String suffix = suffixes.get(teamName);

            if (prefix != null)
                team.setPrefix(prefix);
            if (suffix != null)
                team.setSuffix(suffix);
        }
    }

    private ChatColor getLastColor(String str) {
        ChatColor last = ChatColor.WHITE;
        if (str == null)
            return last;

        for (int i = str.length() - 2; i >= 0; i--) {
            if (str.charAt(i) == ChatColor.COLOR_CHAR) {
                last = ChatColor.getByChar(str.charAt(i + 1));
                break;
            }
        }

        return last;
    }

    private void setLine(Team teamLine, String value) {
        if (value == null) {
            if(!"".equals(teamLine.getPrefix()))
                teamLine.setPrefix("");
            if(!"".equals(teamLine.getSuffix()))
                teamLine.setSuffix("");
        } else if (value.length() < 16) {
            if(!Objects.equals(value, teamLine.getPrefix()))
                teamLine.setPrefix(value);
            if(!"".equals(teamLine.getSuffix()))
                teamLine.setSuffix("");
        } else {
            String pre;
            String suf;
            if (value.charAt(15) == ChatColor.COLOR_CHAR) {
                pre = value.substring(0, 15);
                suf = value.substring(15, Math.min(30, value.length()));
            } else {
                pre = value.substring(0, 16);
                suf = getLastColor(pre) + value.substring(16, Math.min(30, value.length()));
            }

            if(!Objects.equals(pre, teamLine.getPrefix()))
                teamLine.setPrefix(pre);
            if(!Objects.equals(suf, teamLine.getSuffix()))
                teamLine.setSuffix(suf);
        }
    }

    private String getColoredTitle(String title) {
        return ChatColor.DARK_GRAY
                + "[" + ChatColor.AQUA
                + title
                + ChatColor.DARK_GRAY + "]";
    }

    public interface TeamColorChooser {
        String getPrefixForTeam(String teamName);

        String getsuffixForTeam(String teamName);
    }

    private class TeamMap extends ConcurrentHashMap<String, List<String>> {

        @Override
        public List<String> put(String key, List<String> value) {
            List<String> result = super.put(key, value);
            onTeamAdd(key);
            if (result != null) {
                onTeamRemove(key);
            }
            return result;
        }

        @Override
        public List<String> remove(Object key) {
            List<String> result = super.remove(key);
            if (result != null && key instanceof String) {
                onTeamRemove((String) key);
            }
            return result;
        }

        @Override
        public void clear() {
            for (Entry<String, List<String>> entry : super.entrySet()) {
                onTeamRemove(entry.getKey());
            }
            super.clear();
        }
    }

    private static class TeamLine {
        final ChatColor color;
        final Team team;

        public TeamLine(ChatColor color, Team team) {
            super();
            this.color = color;
            this.team = team;
        }

    }
}
