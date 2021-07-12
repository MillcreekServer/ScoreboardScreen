package io.github.wysohn.scoreboardscreen.constants;

import com.google.inject.assistedinject.Assisted;
import io.github.wysohn.rapidframework3.bukkit.manager.api.PlaceholderAPI;
import io.github.wysohn.rapidframework3.core.api.ManagerExternalAPI;
import io.github.wysohn.scoreboardscreen.interfaces.IBoardState;
import io.github.wysohn.scoreboardscreen.interfaces.IUserScoreboard;
import io.github.wysohn.scoreboardscreen.manager.ScoreboardTemplateManager;
import io.github.wysohn.scoreboardscreen.manager.UserManager;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.lang.ref.Reference;
import java.util.Objects;
import java.util.Optional;

public class UserScoreboard implements IUserScoreboard {
    private final UserManager userManager;
    private final Player player;

    private final Object stateChangeMonitor = new Object();
    private final SimpleBoardState defaultBoardState;
    private IBoardState currentBoardState;

    @Inject
    public UserScoreboard(ScoreboardTemplateManager scoreboardTemplateManager,
                          UserManager userManager,
                          ManagerExternalAPI api,
                          @Assisted Player player) {
        this.userManager = userManager;
        this.player = player;

        BoardTemplate defaultTemplate = Objects.requireNonNull(scoreboardTemplateManager.getTemplate("default"));
        this.defaultBoardState = new SimpleBoardState(defaultTemplate, (user, before) -> Optional.ofNullable(api)
                .flatMap(a -> a.getAPI(PlaceholderAPI.class))
                .map(papi -> papi.parse(user, before))
                .orElse(before));
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
                    if(!user.isToggleState())
                        return;

                    try{
                        synchronized (stateChangeMonitor){
                            IBoardState nextState = Optional.ofNullable(currentBoardState)
                                    .map(state -> state.nextState(state))
                                    .orElse(defaultBoardState);

                            if(currentBoardState != nextState){
                                nextState.registerBoard(player);
                                player.setHealth(player.getHealth());
                            }
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
}
