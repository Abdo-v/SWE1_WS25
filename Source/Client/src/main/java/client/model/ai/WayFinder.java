package client.model.ai;
import client.exception.AIDecisionException;
import client.model.GameState;
import client.model.Direction;
import client.model.mapper.MapNode;
import client.model.mapper.Terrain;
import client.view.CLIHandler;
import client.model.StaticColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WayFinder implements client.observer.util.Observer{
    private static final Logger logger = LoggerFactory.getLogger(WayFinder.class);
    private GameState gameState;
    private LinkedHashMap<MapNode, Boolean> halfMapVisitedGrassFields;
    private LinkedHashMap<MapNode, Boolean> oppHalfMapVisitedGrassFields;
    private LinkedHashMap<MapNode, Boolean> allMountainFields; //target M
    private MapNode currentMapNode;
    private ShortestPathFinder shortestPathFinder;
    private TreasureSeeker treasureSeeker;
    private FortSeeker fortSeeker;
    private StrategyGuide strategyGuide;
    int movesMade = 0;
    boolean treasureAlreadyFound = false;
    boolean treasureAlreadyCollected = false;
    boolean fortAlreadyFound = false;
    MapNode enemyFirstTurePosition = null;
    
    /**
     * Constructs a WayFinder with a given GameState.
     * uses update() to update/initialize the other fields with the provided GameState.
     * @param state The initial GameState.
     */
    public WayFinder(GameState state) {
        update(state);
    }

    /**
     * Constructs a WayFinder without an initial GameState.
     * game state intitilized to null, other fields initialized to empty collections.
     */
    public WayFinder(){
        this.gameState = null;
        this.halfMapVisitedGrassFields = new LinkedHashMap<>();
        this.oppHalfMapVisitedGrassFields = new LinkedHashMap<>();
        this.allMountainFields = new LinkedHashMap<>();// target M
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
        logger.debug("WayFinder received GameState update");
        this.gameState = state;
        
        // Add null safety checks for players list
        if (state.getPlayers() != null && !state.getPlayers().isEmpty()) {
            this.currentMapNode = state.getPlayers().get(0).getCurrentPosition();
            logger.trace("Current player position updated to: {}", 
                        currentMapNode != null ? currentMapNode.printCoordinates() : "null");
        } else {
            logger.debug("Players list is null or empty, keeping current position: {}", 
                        currentMapNode != null ? currentMapNode.printCoordinates() : "null");
            // Keep the existing currentMapNode if players are not available yet
        }
        
        if(gameState.getMap() != null && gameState.getMap().getContentSize() >= 50 && (halfMapVisitedGrassFields == null || halfMapVisitedGrassFields.isEmpty())) {
            halfMapVisitedGrassFields = treasureSeeker.getTraverseWay();
            logger.debug("Half map visited grass fields initialized with {} fields", 
                        halfMapVisitedGrassFields != null ? halfMapVisitedGrassFields.size() : 0);
        }
        if (gameState.getMap() != null && (gameState.getMap().getContentSize() == 100) && (oppHalfMapVisitedGrassFields == null || oppHalfMapVisitedGrassFields.isEmpty())) {
            oppHalfMapVisitedGrassFields = fortSeeker.getTraverseWay();
            logger.debug("Opponent half map visited grass fields initialized with {} fields", 
                        oppHalfMapVisitedGrassFields != null ? oppHalfMapVisitedGrassFields.size() : 0);
        }
        if(movesMade == 8) {
            enemyFirstTurePosition = this.gameState.getEnemyCurrentPosition();
            oppHalfMapVisitedGrassFields = fortSeeker.getFilteredTraverseWay(enemyFirstTurePosition);
            logger.debug("Enemy first turn position detected: {}, filtered traverse way updated", 
                        enemyFirstTurePosition != null ? enemyFirstTurePosition.printCoordinates() : "null");
        }
        if(gameState.getMap() != null && gameState.getMap().getContentSize() == 100 && (allMountainFields == null || allMountainFields.isEmpty())) { // target M
            allMountainFields = strategyGuide.getAllMountainFields();
            logger.debug("All mountain fields initialized with {} mountains", 
                        allMountainFields != null ? allMountainFields.size() : 0);
            //System.out.println("WayFinder: All mountain fields initialized: " + allMountainFields.toString());
        }
        logger.trace("WayFinder update completed");
        //System.out.print(StaticColors.BLUE + "W" + StaticColors.RESET);
    }


    /**
     * Determines the strategy for the next move based on the game state.
     * @return The direction for the next move.
     * @throws AIDecisionException If the AI cannot determine a valid move.
     */
    public Direction findNext() throws AIDecisionException {
        logger.debug("Finding next move - treasure collected: {}, moves made: {}", 
                    gameState != null ? gameState.isTreasureCollected() : "unknown", movesMade);
        
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
            Direction nextDirection = moveBasedOnStrategy(strategy);
            
            if (nextDirection != null) {
                movesMade++;
                logger.info("WayFinder selected direction: {} (move #{}, strategy: {})", 
                           nextDirection, movesMade, strategy ? "treasure hunting" : "fort seeking");
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
            logger.error("Unexpected error in AI decision making: {}", e.getMessage(), e);
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
     * Moves based on the current strategy.
     * @param strategy The strategy indicating whether the companion has collected treasure.
     * @return The direction for the companion to move in.
     * @throws AIDecisionException If the strategy fails to determine a valid move.
     */
    private Direction moveBasedOnStrategy(boolean strategy) throws AIDecisionException {
        logger.trace("Moving based on strategy: {}", strategy ? "treasure hunting" : "fort seeking");
        
        try {
            // Validate current position
            if (currentMapNode == null) {
                throw new AIDecisionException(
                    "Cannot execute strategy: current map node is null",
                    "WayFinder",
                    "moveBasedOnStrategy",
                    null
                );
            }
            
            // Mark current position as visited
            markNodeAsVisited(currentMapNode, gameState.isPlayerInOwnHalfMap());
            
            // If current is mountain, mark all neighbors (incl. diagonal) as visited
            if (currentMapNode.getTerrain() == Terrain.MOUNTAIN) {
                ArrayList<MapNode> extendedVisionNodes = strategyGuide.getGrassNodesFromExtendedVision(currentMapNode);
                for (MapNode node : extendedVisionNodes) {
                    markNodeAsVisited(node, gameState.isPlayerInOwnHalfMap());
                }
            }
            
            // If any node on game map contains treasure, target it
            Direction treasureDirection = targetTreasureIfFound(strategy);
            if (treasureDirection != null) {
                return treasureDirection;
            }
            
            // If any node on game map contains enemy fort, target it
            Direction fortDirection = targetFortIfFOund(strategy);
            if (fortDirection != null) {
                return fortDirection;
            }
            
            // Default search strategy
            MapNode bestNode = traverseHalfMap(strategy);
            if (bestNode == null) {
                throw new AIDecisionException(
                    "No valid target node found for traversal",
                    "WayFinder",
                    "traverseHalfMap",
                    currentMapNode
                );
            }
            
            MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, shortestPathFinder.findNextValidNodeToTarget(bestNode));
            if (nextNode == null) {
                throw new AIDecisionException(
                    "Pathfinding failed: no valid next node found to target: " + bestNode.printCoordinates(),
                    "WayFinder",
                    "shortestPathFinder.getNodeInDirection",
                    currentMapNode
                );
            }
            
            Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode);
            if (direction == null) {
                throw new AIDecisionException(
                    "Direction calculation failed: cannot determine direction to neighbor: " + nextNode.printCoordinates(),
                    "WayFinder",
                    "shortestPathFinder.getDirectionToNeighbor",
                    currentMapNode
                );
            }
            
            return direction;
            
        } catch (AIDecisionException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            logger.error("Unexpected error in move strategy: {}", e.getMessage(), e);
            throw new AIDecisionException(
                "Strategy execution failed: " + e.getMessage(),
                e,
                "WayFinder",
                "moveBasedOnStrategy",
                currentMapNode
            );
        }
    }
    
    /**
     * Traverses the half map to find the best node to move towards based on the strategy.
     * @param strategy True if the strategy is to seek treasure, false if seeking enemy fort.
     * @return The best MapNode to move towards based on the strategy.
     * @throws AIDecisionException If no valid nodes are available for traversal.
     */
    private MapNode traverseHalfMap(boolean strategy) throws AIDecisionException {
        try {
            ArrayList<MapNode> visitedHalfMapNodes = new ArrayList<>();
            
            // Validate required data structures
            if (strategy && (halfMapVisitedGrassFields == null || allMountainFields == null)) {
                throw new AIDecisionException(
                    "Cannot traverse half map: required data structures not initialized for treasure hunting",
                    "WayFinder",
                    "traverseHalfMap",
                    currentMapNode
                );
            }
            
            if (!strategy && (oppHalfMapVisitedGrassFields == null || allMountainFields == null)) {
                throw new AIDecisionException(
                    "Cannot traverse half map: required data structures not initialized for fort seeking",
                    "WayFinder",
                    "traverseHalfMap",
                    currentMapNode
                );
            }
            
            // Build visited nodes list
            if (strategy) {
                for (MapNode grassNode : halfMapVisitedGrassFields.keySet()) {
                    if (halfMapVisitedGrassFields.get(grassNode)) {
                        visitedHalfMapNodes.add(grassNode);
                    }
                }
                for (MapNode mountainNode : allMountainFields.keySet()) {
                    if (allMountainFields.get(mountainNode)) {
                        visitedHalfMapNodes.add(mountainNode);
                    }
                }
            } else {
                for (MapNode grassNode : oppHalfMapVisitedGrassFields.keySet()) {
                    if (oppHalfMapVisitedGrassFields.get(grassNode)) {
                        visitedHalfMapNodes.add(grassNode);
                    }
                }
                for (MapNode mountainNode : allMountainFields.keySet()) {
                    if (allMountainFields.get(mountainNode)) {
                        visitedHalfMapNodes.add(mountainNode);
                    }
                }
            }
            
            float bestValue = 0;
            MapNode bestNode = null;
            int candidatesEvaluated = 0;
            
            if (strategy) {
                if (treasureSeeker == null) {
                    throw new AIDecisionException(
                        "Cannot execute treasure hunting strategy: TreasureSeeker is null",
                        "WayFinder",
                        "traverseHalfMap",
                        currentMapNode
                    );
                }
                
                for (MapNode tile : treasureSeeker.getArrangedOwnHalfMap(currentMapNode).getMapNodes()) {
                    if (tile.getTerrain() == Terrain.WATER) continue;
                    if (!visitedHalfMapNodes.contains(tile)) {
                        candidatesEvaluated++;
                        float ratio = getCostToVisionRatio(tile, strategy);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = tile;
                        }
                    }
                }
            } else {
                for (MapNode tile : oppHalfMapVisitedGrassFields.keySet()) {
                    if (tile.getTerrain() == Terrain.WATER) continue;
                    if (!visitedHalfMapNodes.contains(tile)) {
                        candidatesEvaluated++;
                        float ratio = getCostToVisionRatio(tile, strategy);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = tile;
                        }
                    }
                }
                for (MapNode tile : this.allMountainFields.keySet()) {
                    if (!visitedHalfMapNodes.contains(tile) && !gameState.getMap().isNodeInOwnHalf(tile)) {
                        candidatesEvaluated++;
                        float ratio = getCostToVisionRatio(tile, strategy);
                        if (ratio > bestValue) {
                            bestValue = ratio;
                            bestNode = tile;
                        }
                    }
                }
            }
            
            if (bestNode == null) {
                String strategyName = strategy ? "treasure hunting" : "fort seeking";
                throw new AIDecisionException(
                    String.format("No valid target found during %s (evaluated %d candidates, best value: %.2f)", 
                                strategyName, candidatesEvaluated, bestValue),
                    "WayFinder",
                    "traverseHalfMap",
                    currentMapNode
                );
            }
            
            logger.debug("Found best node: {} with value: {} (evaluated {} candidates)", 
                        bestNode.printCoordinates(), bestValue, candidatesEvaluated);
            return bestNode;
            
        } catch (AIDecisionException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            logger.error("Unexpected error in half map traversal: {}", e.getMessage(), e);
            throw new AIDecisionException(
                "Half map traversal failed: " + e.getMessage(),
                e,
                "WayFinder",
                "traverseHalfMap",
                currentMapNode
            );
        }
    }
    
    /**
     * Targets the enemy fort if found, moving towards it.
     * @param strategy True if the strategy is to seek treasure, false if seeking enemy fort.
     * @return The direction to move towards the enemy fort, or null if not applicable.
     * @throws AIDecisionException If fort targeting fails when it should succeed.
     */
    private Direction targetFortIfFOund(boolean strategy) throws AIDecisionException {
        if (!strategy) {
            try {
                if (fortSeeker == null) {
                    throw new AIDecisionException(
                        "Cannot target fort: FortSeeker is null",
                        "WayFinder",
                        "targetFortIfFOund",
                        currentMapNode
                    );
                }
                
                MapNode fortNode = fortSeeker.getEnemyFortNodeIfFound();
                if (fortNode != null) {
                    if (!fortAlreadyFound) {
                        if (CLIHandler.isGameModeReduced()) {
                            System.out.println(StaticColors.PURPLE + "enemy fort found at: (" + fortNode.getX() + "," + fortNode.getY() + "), moving towards it." + StaticColors.RESET);
                        }
                        fortAlreadyFound = true;
                    }
                    
                    MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, 
                                                                           shortestPathFinder.findNextValidNodeToTarget(fortNode));
                    if (nextNode == null) {
                        throw new AIDecisionException(
                            "Cannot find path to enemy fort at: " + fortNode.printCoordinates(),
                            "WayFinder",
                            "targetFortIfFOund",
                            currentMapNode
                        );
                    }
                    
                    Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode);
                    if (direction == null) {
                        throw new AIDecisionException(
                            "Cannot determine direction to enemy fort, nextNode: " + nextNode.printCoordinates(),
                            "WayFinder",
                            "targetFortIfFOund",
                            currentMapNode
                        );
                    }
                    
                    return direction;
                }
            } catch (AIDecisionException e) {
                throw e;
            } catch (Exception e) {
                throw new AIDecisionException(
                    "Fort targeting failed: " + e.getMessage(),
                    e,
                    "WayFinder",
                    "targetFortIfFOund",
                    currentMapNode
                );
            }
        }
        return null;
    }
    
    /**
     * Targets the treasure if found, moving towards it.
     * @param strategy True if the strategy is to seek treasure, false if seeking enemy fort.
     * @return The direction to move towards the treasure, or null if not applicable.
     * @throws AIDecisionException If treasure targeting fails when it should succeed.
     */
    private Direction targetTreasureIfFound(boolean strategy) throws AIDecisionException {
        if (strategy) {
            try {
                if (treasureSeeker == null) {
                    throw new AIDecisionException(
                        "Cannot target treasure: TreasureSeeker is null",
                        "WayFinder",
                        "targetTreasureIfFound",
                        currentMapNode
                    );
                }
                
                MapNode treasureNode = treasureSeeker.getTreasureNodeIfFound();
                if (treasureNode != null) {
                    if (!treasureAlreadyFound) {
                        if (CLIHandler.isGameModeReduced()) {
                            System.out.println(StaticColors.ORANGE + "treasure found at: (" + treasureNode.getX() + "," + treasureNode.getY() + "), moving towards it." + StaticColors.RESET);
                        }
                        treasureAlreadyFound = true;
                    }
                    
                    MapNode nextNode = shortestPathFinder.getNodeInDirection(currentMapNode, 
                                                                           shortestPathFinder.findNextValidNodeToTarget(treasureNode));
                    markNodeAsVisited(currentMapNode, gameState.isPlayerInOwnHalfMap());
                    
                    if (nextNode == null) {
                        throw new AIDecisionException(
                            "Cannot find path to treasure at: " + treasureNode.printCoordinates(),
                            "WayFinder",
                            "targetTreasureIfFound",
                            currentMapNode
                        );
                    }
                    
                    Direction direction = shortestPathFinder.getDirectionToNeighbor(nextNode);
                    if (direction == null) {
                        throw new AIDecisionException(
                            "Cannot determine direction to treasure, nextNode: " + nextNode.printCoordinates(),
                            "WayFinder",
                            "targetTreasureIfFound",
                            currentMapNode
                        );
                    }
                    
                    return direction;
                }
            } catch (AIDecisionException e) {
                throw e;
            } catch (Exception e) {
                throw new AIDecisionException(
                    "Treasure targeting failed: " + e.getMessage(),
                    e,
                    "WayFinder",
                    "targetTreasureIfFound",
                    currentMapNode
                );
            }
        }
        return null;
    }

    /**
     * Marks the current node as visited in the appropriate half map.
     * @param ownHalf true if marking in own half map, false for opponent's half map.
     */
    private void markNodeAsVisited(MapNode node, boolean ownHalf) {
        if (node == null) {
            System.err.println("WayFinder: Cannot mark a null node as visited.");
            return; // Avoid NullPointerException
        }
        if (node != null && node.getTerrain() == Terrain.GRASS) {
            if (ownHalf) {
                // mark as visited in own half map
                this.halfMapVisitedGrassFields.put(node, true);
            } else {
                // mark as visited in opponent half map
                this.oppHalfMapVisitedGrassFields.put(node, true);
            }
        } else if (node != null && (node.getTerrain() == Terrain.MOUNTAIN)) {
            this.allMountainFields.put(node, true); // target M
        }
    }

    /**
     * Calculates the cost to reach a node divided by the number of unvisited grass nodes in the path to that node.
     * @param node The target MapNode to evaluate.
     * @param strategy True if the strategy is to seek treasure, false if seeking enemy fort.
     * @return The cost to vision ratio, or -1 if invalid.
     */
    public float getCostToVisionRatio(MapNode node, boolean strategy){
        if (node == null || gameState == null || gameState.getMap() == null) {
            System.err.println("WayFinder.getCostToVisionRatio: Node, gameState, or map is null.");
            return -1;
        }
        //get a list of visited grass nodes
        ArrayList<MapNode> visitedGrassNodes = new ArrayList<>();
        if (strategy) {
            for (MapNode grassNode : halfMapVisitedGrassFields.keySet()) {
                if (halfMapVisitedGrassFields.get(grassNode)) {
                    visitedGrassNodes.add(grassNode);
                }
            }
        } else {
            for (MapNode grassNode : oppHalfMapVisitedGrassFields.keySet()) {
                if (oppHalfMapVisitedGrassFields.get(grassNode)) {
                    visitedGrassNodes.add(grassNode);
                }
            }
        }
        MapNode previousTile = currentMapNode;
        float cost = 0;
        float grassVision = 0;
        ArrayList<MapNode> path = shortestPathFinder.findShortestPath(currentMapNode, node);
        for(MapNode tile : path) {
            if (tile.getTerrain() == Terrain.GRASS && !visitedGrassNodes.contains(tile)) {
                grassVision += 1; // count grass nodes in path
            }
            cost += shortestPathFinder.getCostToReachNode(previousTile, tile);
            previousTile = tile; 
        }
        if (node.getTerrain() == Terrain.MOUNTAIN){
            ArrayList<MapNode> grassFromVision = strategyGuide.getGrassNodesFromExtendedVision(node);
            for (MapNode grassNode : grassFromVision) {
                if (!visitedGrassNodes.contains(grassNode)) {
                    grassVision += 1; // count grass nodes in extended vision
                }
            }
        }
        
        return grassVision > 0 ? grassVision / cost : -1;
    }

    /**
     * Adds the sub-observers to the GameState.
     * This method is used to register the observers for the WayFinder.
     */
    public void addSubObservers(){
        logger.debug("Adding sub-observers to GameState");
        if(this.shortestPathFinder != null) {
            this.gameState.addObserver(this.shortestPathFinder);
            logger.trace("ShortestPathFinder observer added");
        }
        if(this.treasureSeeker != null) {
            this.gameState.addObserver(this.treasureSeeker);
            logger.trace("TreasureSeeker observer added");
        }
        if(this.fortSeeker != null) {
            this.gameState.addObserver(this.fortSeeker);
            logger.trace("FortSeeker observer added");
        }
        if(this.strategyGuide != null) {
            this.gameState.addObserver(this.strategyGuide);
            logger.trace("StrategyGuide observer added");
        }
        logger.debug("All sub-observers added successfully");
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
        logger.debug("Current MapNode set to: {}", currentMapNode.printCoordinates());
    }
    public void setTreasureSeeker(TreasureSeeker treasureSeeker) {
        this.treasureSeeker = treasureSeeker;
        logger.debug("TreasureSeeker set for WayFinder");
    }
    public void setFortSeeker(FortSeeker fortSeeker) {
        this.fortSeeker = fortSeeker;
        logger.debug("FortSeeker set for WayFinder");
    }
    public void setShortestPathFinder(ShortestPathFinder shortestPathFinder) {
        this.shortestPathFinder = shortestPathFinder;
        logger.debug("ShortestPathFinder set for WayFinder");
    }
    public void setStrategyGuide(StrategyGuide strategyGuide) {
        this.strategyGuide = strategyGuide;
        logger.debug("StrategyGuide set for WayFinder");
    }

    /**
     * Sets the GameState for the WayFinder. only used for observer adding.
     * @param state
     * @return
     */
    public void setGameState(GameState state){
        this.gameState = state;
        logger.debug("GameState set for WayFinder");
    }

}