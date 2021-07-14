package io.github.wysohn.scoreboardscreen.constants;

import io.github.wysohn.scoreboardscreen.interfaces.IBoardState;
import io.github.wysohn.scoreboardscreen.interfaces.ITeamInfo;
import io.github.wysohn.scoreboardscreen.interfaces.ITransitionStrategy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SimpleBoardState implements IBoardState {
    private final BoardTemplate template;
    private final BiFunction<User, String, String> beforeParse;
    private final ITransitionStrategy transitionStrategy;

    private final Map<String, SimpleTeam> teams = new HashMap<>();

    private final Scoreboard board;
    private final Objective objSide;
    private final List<TeamLine> lines = new ArrayList<>();
    private final Objective objBelow;

    private final Placeholder.Context titleContext;
    private final List<Placeholder.Context> lineContexts = new ArrayList<>();

    public SimpleBoardState(BoardTemplate template,
                            BiFunction<User, String, String> beforeParse,
                            ITransitionStrategy transitionStrategy) {
        this.template = template;
        this.beforeParse = beforeParse;
        this.transitionStrategy = transitionStrategy;

        board = Bukkit.getScoreboardManager().getNewScoreboard();

        objSide = board.registerNewObjective("title", "dummy", "title");
        objSide.setDisplaySlot(DisplaySlot.SIDEBAR);
        titleContext = new Placeholder.Context();

        for (int i = 0; i > -15; i--) {
            Team team = board.registerNewTeam(String.valueOf(i));
            ChatColor color = ChatColor.values()[-i];
            team.addEntry(color.toString());
            lines.add(new TeamLine(color, team));
            lineContexts.add(new Placeholder.Context());
        }

        objBelow = board.registerNewObjective("showhealth", "health", "showhealth");
        objBelow.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objBelow.setDisplayName(ChatColor.DARK_RED + "/ 20");
    }

    public SimpleBoardState(BoardTemplate template,
                            BiFunction<User, String, String> beforeParse) {
        this(template, beforeParse, (current) -> null);
    }

    private SimpleTeam getOrNewTeam(String teamName){
        synchronized (teams){
            return teams.computeIfAbsent(teamName, (key) -> new SimpleTeam(teamName));
        }
    }

    public void addMember(String teamName, String memberName){
        SimpleTeam team = getOrNewTeam(teamName);
        team.addMember(memberName);
    }

    public void removeMember(String teamName, String memberName){
        SimpleTeam team = getOrNewTeam(teamName);
        team.removeMember(memberName);
    }

    public boolean deleteTeam(String teamName){
        synchronized (teams){
            return teams.remove(teamName) != null;
        }
    }

    public void setPrefix(String teamName, String prefix){
        SimpleTeam team = getOrNewTeam(teamName);
        team.setPrefix(prefix);
    }

    public void setSuffix(String teamName, String suffix){
        SimpleTeam team = getOrNewTeam(teamName);
        team.setSuffix(suffix);
    }

    @Override
    public void registerBoard(Player player, Consumer<Runnable> runSynchronous) {
        runSynchronous.accept(() -> {
            if(player.getScoreboard() == board)
                return;

            player.setHealth(player.getHealth());
            player.setScoreboard(board);
        });
    }

    @Override
    public IBoardState nextState(IBoardState current) {
        return transitionStrategy.nextState(current);
    }

    @Override
    public void updateTitle(User player, Consumer<Runnable> runSynchronous) {
        String currentTitle = Optional.of(template)
                .map(t -> t.title)
                .map(placeholder -> placeholder.parse(player, titleContext, beforeParse))
                .map(str -> str.substring(0, Math.min(36, str.length())))
                .orElse("");

        runSynchronous.accept(() -> {
            // update title
            if(!objSide.getDisplayName().equals(currentTitle)){
                objSide.setDisplayName(currentTitle);
            }
        });
    }

    @Override
    public void updateLines(User player, Consumer<Runnable> runSynchronous) {
        // update lines
        for (int index = 0; index < template.numLines(); index++) {
            if (index > lines.size() - 1)
                break;

            TeamLine valueLine = lines.get(index);
            objSide.getScore(String.valueOf(valueLine.color)).setScore(-index);

            int i = index;
            String value = Optional.of(template)
                    .map(t -> t.scoreboard)
                    .filter(lines -> i < lines.size())
                    .map(lines -> lines.get(i))
                    .map(line -> line.value.parse(player, lineContexts.get(i), beforeParse))
                    .orElse("Error");
            value = value.substring(0, Math.min(40, value.length()));
            setLine(valueLine.team, value);
        }
    }

    @Override
    public void updateTeams(User player, Consumer<Runnable> runSynchronous) {
        List<ITeamInfo> copy;
        synchronized (teams){
            copy = new LinkedList<>(teams.values());
        }

        // create or update teams
        for (ITeamInfo teamInfo : copy) {
            String teamName = teamInfo.teamName();
            List<String> memberNames = new LinkedList<>(teamInfo.teamMemberNames());

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

            String prefix = teamInfo.prefix();
            String suffix = teamInfo.suffix();

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

    private static final class SimpleTeam implements ITeamInfo{
        private final String teamName;
        private final Set<String> memberNames = new HashSet<>();
        private String prefix;
        private String suffix;

        public SimpleTeam(String teamName) {
            this.teamName = teamName;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        public boolean addMember(String s) {
            synchronized (memberNames){
                return memberNames.add(s);
            }
        }

        public boolean removeMember(String o) {
            synchronized(memberNames){
                return memberNames.remove(o);
            }
        }

        @Override
        public String teamName() {
            return teamName;
        }

        @Override
        public List<String> teamMemberNames() {
            synchronized (memberNames){
                return new LinkedList<>(memberNames);
            }
        }

        @Override
        public String prefix() {
            return prefix;
        }

        @Override
        public String suffix() {
            return suffix;
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
