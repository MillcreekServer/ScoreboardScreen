package io.github.wysohn.scoreboardscreen.constants;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.rapidframework3.bukkit.manager.api.PlaceholderAPI;
import io.github.wysohn.rapidframework3.core.api.ManagerExternalAPI;
import io.github.wysohn.rapidframework3.interfaces.plugin.ITaskSupervisor;
import io.github.wysohn.scoreboardscreen.interfaces.IBoardState;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.wysohn.scoreboardscreen.manager.ScoreboardTemplateManager;
import io.github.wysohn.scoreboardscreen.manager.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.Reference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserScoreboard implements IUserScoreboard {
    private final ScriptContext context = new SimpleScriptContext();

    private final ITaskSupervisor task;
    private final UserManager userManager;
    private final Player player;

    private final Object stateChangeMonitor = new Object();
    private final SimpleBoardState defaultBoardState;
    private IBoardState currentBoardState;

    @Inject
    public UserScoreboard(ITaskSupervisor task,
                          ScoreboardTemplateManager scoreboardTemplateManager,
                          UserManager userManager,
                          ManagerExternalAPI api,
                          @Assisted Player player) {
        this.task = task;
        this.userManager = userManager;
        this.player = player;

        BoardTemplate defaultTemplate = Objects.requireNonNull(scoreboardTemplateManager.getTemplate("default"));
        this.defaultBoardState = new SimpleBoardState(defaultTemplate, (user, before) -> Optional.ofNullable(api)
                .flatMap(a -> a.getAPI(PlaceholderAPI.class))
                .map(papi -> papi.parse(user, before))
                .orElse(before));
        this.currentBoardState = defaultBoardState;
    }

    @Override
    public void changeState(IBoardState state){
        synchronized (stateChangeMonitor){
            currentBoardState = state;
        }
    }

    @Override
    public void update() {
        if (!player.isOnline() || player.isDead())
            return;

        userManager.get(player.getUniqueId())
                .map(Reference::get)
                .ifPresent(user -> {
                    if(!user.isToggleState()) {
                        task.sync(() -> player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
                        return;
                    }

                    try{
                        synchronized (stateChangeMonitor){
                            currentBoardState = Optional.ofNullable(currentBoardState)
                                    .map(state -> state.nextState(state))
                                    .orElse(defaultBoardState);
                            task.sync(() -> {
                                currentBoardState.registerBoard(player);
                                player.setHealth(player.getHealth());
                            });
                        }

                        currentBoardState.updateTitle(user);
                        currentBoardState.updateLines(user);
                        currentBoardState.updateTeams(user);
                    } catch (Exception ex){
                        ex.printStackTrace();

                        synchronized (stateChangeMonitor){
                            currentBoardState = defaultBoardState;
                        }
                    }
                });
    }

    //--------------------------------------------------------------------------------
    //ScriptContext delegates

    @Override
    public void setBindings(Bindings bindings, int scope) {
        context.setBindings(bindings, scope);
    }

    @Override
    public Bindings getBindings(int scope) {
        return context.getBindings(scope);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        context.setAttribute(name, value, scope);
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return context.getAttribute(name, scope);
    }

    @Override
    public Object removeAttribute(String name, int scope) {
        return context.removeAttribute(name, scope);
    }

    @Override
    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }

    @Override
    public int getAttributesScope(String name) {
        return context.getAttributesScope(name);
    }

    @Override
    public Writer getWriter() {
        return context.getWriter();
    }

    @Override
    public Writer getErrorWriter() {
        return context.getErrorWriter();
    }

    @Override
    public void setWriter(Writer writer) {
        context.setWriter(writer);
    }

    @Override
    public void setErrorWriter(Writer writer) {
        context.setErrorWriter(writer);
    }

    @Override
    public Reader getReader() {
        return context.getReader();
    }

    @Override
    public void setReader(Reader reader) {
        context.setReader(reader);
    }

    @Override
    public List<Integer> getScopes() {
        return context.getScopes();
    }
}
