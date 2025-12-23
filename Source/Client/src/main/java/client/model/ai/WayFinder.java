package client.model.ai;
import client.exception.AIDecisionException;
import client.model.GameState;
import client.model.Direction;
import client.model.mapper.MapNode;
import client.model.mapper.HalfMapDimensions;
import java.util.Objects;
import java.util.Optional;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class WayFinder implements client.observer.util.Observer{
    // private static final Logger logger = LoggerFactory.getLogger(WayFinder.class);

    private static final int HALF_MAP_TOTAL_NODES = HalfMapDimensions.TOTAL_NODES;
    private static final int FULL_MAP_TOTAL_NODES = HALF_MAP_TOTAL_NODES * 2;
    private static final int MOVES_UNTIL_ENEMY_TRUE_POSITION = 8;

    private Optional<GameState> gameState;
    private WayHelper wayHelper;
    private StateHolder stateHolder;
    private Optional<MapNode> currentMapNode;
    private ShortestPathFinder shortestPathFinder;
    private TreasureSeeker treasureSeeker;
    private FortSeeker fortSeeker;
    private StrategyGuide strategyGuide;
    int movesMade = 0;
    
    /**
     * Constructs a WayFinder with a given GameState.
     * uses update() to update/initialize the other fields with the provided GameState.
     * @param state The initial GameState.
     */
    public WayFinder(GameState state) {
        this();
        update(state);
    }

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
        this.treasureSeeker = new TreasureSeeker();
        this.fortSeeker = new FortSeeker();
        this.strategyGuide = new StrategyGuide();

    }

    @Override
    /**
     * Updates the WayFinder with the current GameState.
     * called by notifier when the GameState changes.
     * @param state The current GameState.
     */
    public void update(GameState state) {
        // logger.debug("WayFinder received GameState update");
        updateFromState(Objects.requireNonNull(state, "state is required"));
    }

    private void updateFromState(GameState state) {
        this.gameState = Optional.of(Objects.requireNonNull(state, "state is required"));

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
            stateHolder.getEnemyFirstTruePosition().ifPresent(enemyPos ->
                    wayHelper.setOppHalfMapVisitedGrassFields(fortSeeker.getFilteredTraverseWay(enemyPos))
            );
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
    public Direction findNext() throws AIDecisionException {
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

    /**
     * Calculates the cost to reach a node divided by the number of unvisited grass nodes in the path to that node.
     * @param node The target MapNode to evaluate.
     * @param strategy True if the strategy is to seek treasure, false if seeking enemy fort.
     * @return The cost to vision ratio, or -1 if invalid.
     */
    public float getCostToVisionRatio(MapNode node, boolean strategy){
        return getCostToVisionRatio(node, strategy ? Objective.TREASURE : Objective.FORT);
    }

    public float getCostToVisionRatio(MapNode node, Objective objective) {
        GameState state = gameState.orElseThrow(() -> new IllegalStateException("GameState must be initialized before scoring"));
        MapNode current = currentMapNode.orElseThrow(() -> new IllegalStateException("Current position must be initialized before scoring"));
        return logic().getCostToVisionRatio(state, current, node, objective);
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

    // getters and setters for testing, TDD

    public TreasureSeeker getTreasureSeeker() {
        return treasureSeeker;
    }
    public FortSeeker getFortSeeker() {
        return fortSeeker;
    }
    public ShortestPathFinder getShortestPathFinder() {
        return shortestPathFinder;
    }
    public StrategyGuide getStrategyGuide() {
        return strategyGuide;
    }
    public GameState getGameState() {
        return gameState.orElseThrow(() -> new IllegalStateException("GameState is not initialized"));
    }
    public MapNode getCurrentMapNode() {
        return currentMapNode.orElseThrow(() -> new IllegalStateException("Current position is not initialized"));
    }
    public void setCurrentMapNode(MapNode currentMapNode) {
        this.currentMapNode = Optional.of(Objects.requireNonNull(currentMapNode, "currentMapNode must not be null"));
        // logger.debug("Current MapNode set to: {}", currentMapNode.printCoordinates());
    }
    public void setTreasureSeeker(TreasureSeeker treasureSeeker) {
        this.treasureSeeker = Objects.requireNonNull(treasureSeeker, "treasureSeeker must not be null");
        // logger.debug("TreasureSeeker set for WayFinder");
    }
    public void setFortSeeker(FortSeeker fortSeeker) {
        this.fortSeeker = Objects.requireNonNull(fortSeeker, "fortSeeker must not be null");
        // logger.debug("FortSeeker set for WayFinder");
    }
    public void setShortestPathFinder(ShortestPathFinder shortestPathFinder) {
        this.shortestPathFinder = Objects.requireNonNull(shortestPathFinder, "shortestPathFinder must not be null");
        // logger.debug("ShortestPathFinder set for WayFinder");
    }
    public void setStrategyGuide(StrategyGuide strategyGuide) {
        this.strategyGuide = Objects.requireNonNull(strategyGuide, "strategyGuide must not be null");
        // logger.debug("StrategyGuide set for WayFinder");
    }

    /**
     * Sets the GameState for the WayFinder. only used for observer adding.
     * @param state
     * @return
     */
    public void setGameState(GameState state){
        this.gameState = Optional.of(Objects.requireNonNull(state, "state must not be null"));
        // logger.debug("GameState set for WayFinder");
    }
    
    public WayHelper getWayHelper() {
        return wayHelper;
    }
    
    public void setWayHelper(WayHelper wayHelper) {
        this.wayHelper = wayHelper;
    }
    
    public StateHolder getStateHolder() {
        return stateHolder;
    }
    
    public void setStateHolder(StateHolder stateHolder) {
        this.stateHolder = stateHolder;
    }

}