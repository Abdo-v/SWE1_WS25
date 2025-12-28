package client.model.ai;
import client.exception.AIDecisionException;
import client.exception.NoValidMoveAvailableException;
import client.model.GameState;
import client.model.Direction;
import client.model.mapper.MapNode;
import client.model.mapper.HalfMapDimensions;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class WayFinder implements client.observer.util.Observer{
    // private static final Logger logger = LoggerFactory.getLogger(WayFinder.class);

    private static final int HALF_MAP_TOTAL_NODES = HalfMapDimensions.TOTAL_NODES;
    private static final int FULL_MAP_TOTAL_NODES = HALF_MAP_TOTAL_NODES * 2;
    private static final int MOVES_UNTIL_ENEMY_TRUE_POSITION = 8;
    private static final String STATE_REQUIRED_MESSAGE = MessageConfig.STATE_REQUIRED_MESSAGE;

    private Optional<GameState> gameState;
    private final WayHelper wayHelper;
    private final StateHolder stateHolder;
    private Optional<MapNode> currentMapNode;
    private final ShortestPathFinder shortestPathFinder;
    private final TreasureSeeker treasureSeeker;
    private final FortSeeker fortSeeker;
    private final StrategyGuide strategyGuide;
    private int movesMade = 0;

    /**
     * Constructs a WayFinder without an initial GameState.
     * game state intitilized to empty, other fields initialized to empty collections.
     */
    public WayFinder(){
        this.gameState = Optional.empty();
        this.wayHelper = new WayHelper();
        this.stateHolder = new StateHolder();
        this.currentMapNode = Optional.empty();
        this.shortestPathFinder = new ShortestPathFinder();
        this.treasureSeeker = new TreasureSeeker(wayHelper);
        this.fortSeeker = new FortSeeker(wayHelper);
        this.strategyGuide = new StrategyGuide(wayHelper);

    }

    @Override
    /**
     * Updates the WayFinder with the current GameState.
     * called by notifier when the GameState changes.
     * @param state The current GameState.
     */
    public void update(GameState state) {
        // logger.debug("WayFinder received GameState update");
        updateFromState(Objects.requireNonNull(state, STATE_REQUIRED_MESSAGE));
    }

    private void updateFromState(GameState state) {
        this.gameState = Optional.of(Objects.requireNonNull(state, STATE_REQUIRED_MESSAGE));

        wayHelper.update(state);
        stateHolder.update(state);
        shortestPathFinder.update(state);
        treasureSeeker.update(state);
        fortSeeker.update(state);
        strategyGuide.update(state);

        this.currentMapNode = state.getCurrentPlayerState()
                .flatMap(player -> player.getCurrentPosition());

        state.getMap()
                .filter(map -> map.getContentSize() >= HALF_MAP_TOTAL_NODES)
                .filter(map -> wayHelper.getHalfMapVisitedGrassFields().isEmpty())
                .ifPresent(map -> {
            wayHelper.setHalfMapVisitedGrassFields(treasureSeeker.getTraverseWay());
        });

        state.getMap()
                .filter(map -> map.getContentSize() == FULL_MAP_TOTAL_NODES)
                .filter(map -> wayHelper.getOppHalfMapVisitedGrassFields().isEmpty())
                .ifPresent(map -> {
            wayHelper.setOppHalfMapVisitedGrassFields(fortSeeker.getTraverseWay());
        });
        if(movesMade == MOVES_UNTIL_ENEMY_TRUE_POSITION) {
            stateHolder.setEnemyFirstTruePosition(state.getEnemyCurrentPosition());
            stateHolder.getEnemyFirstTruePosition().ifPresent(enemyPos -> {
                LinkedHashMap<MapNode, Boolean> previous = new LinkedHashMap<>(wayHelper.getOppHalfMapVisitedGrassFields());
                LinkedHashMap<MapNode, Boolean> filtered = fortSeeker.getFilteredTraverseWay(enemyPos);

                // Preserve already-visited flags for nodes that remain in the filtered search space.
                for (MapNode node : filtered.keySet()) {
                    if (Boolean.TRUE.equals(previous.get(node))) {
                        filtered.put(node, true);
                    }
                }

                // If we are currently standing on a relevant enemy-half grass node, mark it visited immediately.
                currentMapNode
                        .filter(n -> filtered.containsKey(n))
                        .ifPresent(n -> filtered.put(n, true));

                wayHelper.setOppHalfMapVisitedGrassFields(filtered);

                // Force the fort-seeking exploration logic to re-evaluate with the refined search-space.
                stateHolder.clearLockedExplorationTarget();
            });
        }
        state.getMap()
            .filter(map -> map.getContentSize() == FULL_MAP_TOTAL_NODES)
            .filter(map -> wayHelper.getAllMountainFieldsMap().isEmpty())
            .ifPresent(map -> {
            wayHelper.setAllMountainFields(strategyGuide.getAllMountainFields());
            //System.out.println("WayFinder: All mountain fields initialized: " + wayHelper.getAllMountainFieldsMap().toString());
        });
        // logger.trace("WayFinder update completed");
        //System.out.print(StaticColors.BLUE + "W" + StaticColors.RESET);
    }


    /**
     * Determines the strategy for the next move based on the game state.
     * @return The direction for the next move.
     * @throws AIDecisionException If the AI cannot determine a valid move.
     */
    public Direction findNext() throws AIDecisionException, NoValidMoveAvailableException {
        // logger.debug("Finding next move - treasure collected: {}, moves made: {}", gameState.isPresent() ? gameState.get().isTreasureCollected() : "unknown", movesMade);
        
        // Validate game state before making decisions
        GameState state = gameState.orElseThrow(() -> new AIDecisionException(
                "Cannot determine next move: game state not initialized",
                "WayFinder",
                "findNext"
        ));

        if (state.getCurrentPlayerState().isEmpty()) {
            throw new AIDecisionException(
                    "Cannot determine next move: current player state is missing",
                    "WayFinder",
                    "findNext",
                    state
            );
        }

        MapNode current = currentMapNode.orElseThrow(() -> new AIDecisionException(
                "Cannot determine next move: current position is unknown",
                "WayFinder",
                "findNext",
                state
        ));
        
        try {
            Objective objective = state.isTreasureCollected() ? Objective.FORT : Objective.TREASURE;
            Direction nextDirection = logic().moveBasedOnStrategy(state, current, objective);
            movesMade++;
            return nextDirection;
        } catch (NoValidMoveAvailableException e) {
            throw e;
        } catch (AIDecisionException e) {
            throw e; // Re-throw AI exceptions
        } catch (Exception e) {
        	// logger.error("Unexpected error in AI decision making: {}", e.getMessage(), e);
            throw new AIDecisionException(
                "Unexpected error during move calculation: " + e.getMessage(),
                e,
                "WayFinder",
                "findNext",
                current
            );
        }
    }

    private WayFinderLogic logic() {
        return new WayFinderLogic(wayHelper, stateHolder, shortestPathFinder, treasureSeeker, fortSeeker, strategyGuide);
    }

    /**
     * Adds the sub-observers to the GameState.
     * This method is used to register the observers for the WayFinder.
     */
    public void addSubObservers(){
        // logger.debug("Adding sub-observers to GameState");
        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be set before adding sub-observers"));
        state.addObserver(this.wayHelper);
        state.addObserver(this.stateHolder);
        state.addObserver(this.shortestPathFinder);
        state.addObserver(this.treasureSeeker);
        state.addObserver(this.fortSeeker);
        state.addObserver(this.strategyGuide);
        // logger.debug("All sub-observers added successfully");
    }

    public void setGameState(GameState state){
        this.gameState = Optional.of(Objects.requireNonNull(state, STATE_REQUIRED_MESSAGE));
        // logger.debug("GameState set for WayFinder");
    }

}