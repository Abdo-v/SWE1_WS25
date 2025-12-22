package client.model.ai;
import client.exception.AIDecisionException;
import client.model.GameState;
import client.model.Direction;
import client.model.mapper.MapNode;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class WayFinder implements client.observer.util.Observer{
    // private static final Logger logger = LoggerFactory.getLogger(WayFinder.class);
    private GameState gameState;
    private WayHelper wayHelper;
    private StateHolder stateHolder;
    private MapNode currentMapNode;
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
     * game state intitilized to null, other fields initialized to empty collections.
     */
    public WayFinder(){
        this.gameState = null;
        this.wayHelper = new WayHelper();
        this.stateHolder = new StateHolder();
        this.currentMapNode = new MapNode();
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
        if (state == null) {
            return;
        }
        this.gameState = state;

        // Ensure sub-components exist (defensive for tests / partial construction)
        if (this.wayHelper == null) this.wayHelper = new WayHelper();
        if (this.stateHolder == null) this.stateHolder = new StateHolder();
        if (this.currentMapNode == null) this.currentMapNode = new MapNode();
        if (this.shortestPathFinder == null) this.shortestPathFinder = new ShortestPathFinder();
        if (this.treasureSeeker == null) this.treasureSeeker = new TreasureSeeker();
        if (this.fortSeeker == null) this.fortSeeker = new FortSeeker();
        if (this.strategyGuide == null) this.strategyGuide = new StrategyGuide();

        wayHelper.update(state);
        stateHolder.update(state);
        shortestPathFinder.update(state);
        treasureSeeker.update(state);
        fortSeeker.update(state);
        strategyGuide.update(state);
        
        // Add null safety checks for players list
        if (state.getPlayers() != null && !state.getPlayers().isEmpty()) {
            this.currentMapNode = state.getPlayers().get(0).getCurrentPosition();
            // logger.trace("Current player position updated to: {}", 
            //             currentMapNode != null ? currentMapNode.printCoordinates() : "null");
        } else {
            // logger.debug("Players list is null or empty, keeping current position: {}", 
            //             currentMapNode != null ? currentMapNode.printCoordinates() : "null");
            // Keep the existing currentMapNode if players are not available yet
        }
        
        if(gameState.getMap() != null && gameState.getMap().getContentSize() >= 50 && (wayHelper.getHalfMapVisitedGrassFields() == null || wayHelper.getHalfMapVisitedGrassFields().isEmpty())) {
            wayHelper.setHalfMapVisitedGrassFields(treasureSeeker.getTraverseWay());
            // logger.debug("Half map visited grass fields initialized with {} fields", 
            //             wayHelper.getHalfMapVisitedGrassFields() != null ? wayHelper.getHalfMapVisitedGrassFields().size() : 0);
        }
        if (gameState.getMap() != null && (gameState.getMap().getContentSize() == 100) && (wayHelper.getOppHalfMapVisitedGrassFields() == null || wayHelper.getOppHalfMapVisitedGrassFields().isEmpty())) {
            wayHelper.setOppHalfMapVisitedGrassFields(fortSeeker.getTraverseWay());
            // logger.debug("Opponent half map visited grass fields initialized with {} fields", 
            //             wayHelper.getOppHalfMapVisitedGrassFields() != null ? wayHelper.getOppHalfMapVisitedGrassFields().size() : 0);
        }
        if(movesMade == 8) {
            stateHolder.setEnemyFirstTruePosition(this.gameState.getEnemyCurrentPosition());
            wayHelper.setOppHalfMapVisitedGrassFields(fortSeeker.getFilteredTraverseWay(stateHolder.getEnemyFirstTruePosition()));
            // logger.debug("Enemy first turn position detected: {}, filtered traverse way updated", 
            //             stateHolder.getEnemyFirstTruePosition() != null ? stateHolder.getEnemyFirstTruePosition().printCoordinates() : "null");
        }
        if(gameState.getMap() != null && gameState.getMap().getContentSize() == 100 && (wayHelper.getAllMountainFieldsMap() == null || wayHelper.getAllMountainFieldsMap().isEmpty())) { // target M
            wayHelper.setAllMountainFields(strategyGuide.getAllMountainFields());
            // logger.debug("All mountain fields initialized with {} mountains", 
            //             wayHelper.getAllMountainFieldsMap() != null ? wayHelper.getAllMountainFieldsMap().size() : 0);
            //System.out.println("WayFinder: All mountain fields initialized: " + wayHelper.getAllMountainFieldsMap().toString());
        }
        // logger.trace("WayFinder update completed");
        //System.out.print(StaticColors.BLUE + "W" + StaticColors.RESET);
    }


    /**
     * Determines the strategy for the next move based on the game state.
     * @return The direction for the next move.
     * @throws AIDecisionException If the AI cannot determine a valid move.
     */
    public Direction findNext() throws AIDecisionException {
    	// logger.debug("Finding next move - treasure collected: {}, moves made: {}", gameState != null ? gameState.isTreasureCollected() : "unknown", movesMade);
        
        // Validate game state before making decisions
        if (gameState == null) {
            throw new AIDecisionException(
                "Cannot determine next move: game state is null",
                "WayFinder",
                "findNext",
                null
            );
        }
        
        if (gameState.getCurrentPlayerState() == null) {
            throw new AIDecisionException(
                "Cannot determine next move: current player state is null",
                "WayFinder",
                "findNext",
                null
            );
        }
        
        if (currentMapNode == null) {
            throw new AIDecisionException(
                "Cannot determine next move: current position is unknown",
                "WayFinder",
                "findNext",
                null
            );
        }
        
        try {
            boolean strategy = !gameState.isTreasureCollected();
            Direction nextDirection = logic().moveBasedOnStrategy(gameState, currentMapNode, strategy);
            
            if (nextDirection != null) {
                movesMade++;
             // logger.info("WayFinder selected direction: {} (move #{}, strategy: {})", nextDirection, movesMade, strategy ? "treasure hunting" : "fort seeking");
                return nextDirection;
            } else {
                throw new AIDecisionException(
                    "Strategy algorithm failed to determine a valid move",
                    "WayFinder",
                    "moveBasedOnStrategy",
                    currentMapNode
                );
            }
        } catch (AIDecisionException e) {
            throw e; // Re-throw AI exceptions
        } catch (Exception e) {
        	// logger.error("Unexpected error in AI decision making: {}", e.getMessage(), e);
            throw new AIDecisionException(
                "Unexpected error during move calculation: " + e.getMessage(),
                e,
                "WayFinder",
                "findNext",
                currentMapNode
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
        return logic().getCostToVisionRatio(gameState, currentMapNode, node, strategy);
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
        if(this.wayHelper != null) {
            this.gameState.addObserver(this.wayHelper);
            // logger.trace("WayHelper observer added");
        }
        if(this.stateHolder != null) {
            this.gameState.addObserver(this.stateHolder);
            // logger.trace("StateHolder observer added");
        }
        if(this.shortestPathFinder != null) {
            this.gameState.addObserver(this.shortestPathFinder);
            // logger.trace("ShortestPathFinder observer added");
        }
        if(this.treasureSeeker != null) {
            this.gameState.addObserver(this.treasureSeeker);
            // logger.trace("TreasureSeeker observer added");
        }
        if(this.fortSeeker != null) {
            this.gameState.addObserver(this.fortSeeker);
            // logger.trace("FortSeeker observer added");
        }
        if(this.strategyGuide != null) {
            this.gameState.addObserver(this.strategyGuide);
            // logger.trace("StrategyGuide observer added");
        }
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
        return gameState;
    }
    public MapNode getCurrentMapNode() {
        return currentMapNode;
    }
    public void setCurrentMapNode(MapNode currentMapNode) {
        this.currentMapNode = currentMapNode;
        // logger.debug("Current MapNode set to: {}", currentMapNode.printCoordinates());
    }
    public void setTreasureSeeker(TreasureSeeker treasureSeeker) {
        this.treasureSeeker = treasureSeeker;
        // logger.debug("TreasureSeeker set for WayFinder");
    }
    public void setFortSeeker(FortSeeker fortSeeker) {
        this.fortSeeker = fortSeeker;
        // logger.debug("FortSeeker set for WayFinder");
    }
    public void setShortestPathFinder(ShortestPathFinder shortestPathFinder) {
        this.shortestPathFinder = shortestPathFinder;
        // logger.debug("ShortestPathFinder set for WayFinder");
    }
    public void setStrategyGuide(StrategyGuide strategyGuide) {
        this.strategyGuide = strategyGuide;
        // logger.debug("StrategyGuide set for WayFinder");
    }

    /**
     * Sets the GameState for the WayFinder. only used for observer adding.
     * @param state
     * @return
     */
    public void setGameState(GameState state){
        this.gameState = state;
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