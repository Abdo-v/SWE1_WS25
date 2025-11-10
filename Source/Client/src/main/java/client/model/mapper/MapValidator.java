package client.model.mapper;

import client.model.common.Notification;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class MapValidator {

    private static final int HALF_MAP_TOTAL_NODES = 50;
    private static final double MIN_MOUNTAIN_PERCENTAGE = 0.10;
    private static final double MIN_GRASS_PERCENTAGE = 0.48;
    private static final double MIN_WATER_PERCENTAGE = 0.14;
    private static final double CASTLE_PERCENTAGE = 0.02; // 1 castle
    private static final double MIN_EDGE_WALKABLE_PERCENTAGE = 0.51;

    /**
     * Validates a PlayerHalfMap according to game rules and structural requirements.
     * Performs comprehensive validation including:
     * - Structural integrity (null checks, node count, coordinates)
     * - Map dimensions (10x5 or 5x10)
     * - Terrain distribution (minimum percentages for each terrain type)
     * - Castle placement and count
     * - Reachability of all walkable nodes
     * - Edge walkability requirements
     * 
     * @param halfMap The PlayerHalfMap to validate
     * @return A Notification object containing any validation errors found
     */
    public Notification validate(PlayerHalfMap halfMap) {
        Notification notification = new Notification();
        if (halfMap == null) {
            notification.addError("PlayerHalfMap cannot be null.");
            return notification;
        }

        List<MapNode> nodes = halfMap.getMapNodes();
        if (nodes == null) {
            notification.addError("MapNode list in PlayerHalfMap cannot be null.");
            return notification;
        }

        // Rule: Correct number of nodes
        if (nodes.size() != HALF_MAP_TOTAL_NODES) {
            notification.addError("Map must contain exactly " + HALF_MAP_TOTAL_NODES + " nodes. Found: " + nodes.size());
            return notification; // Critical error, further validation might be unreliable
        }

        // Check for null nodes and duplicate coordinates, determine bounds
        int maxX = -1, maxY = -1;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        Set<String> uniqueCoords = new HashSet<>();
        for (MapNode node : nodes) {
            if (node == null) {
                notification.addError("Map contains a null MapNode.");
                return notification; // Critical error
            }
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            if (!uniqueCoords.add(node.getX() + "," + node.getY())) {
                notification.addError("Duplicate coordinates found at X=" + node.getX() + ", Y=" + node.getY());
            }
        }

        if (notification.hasErrors()) return notification; // Stop if critical structural errors found

        if (minX != 0 || minY != 0) {
            notification.addError("Map coordinates should start from (0,0). Found minX=" + minX + ", minY=" + minY);
        }

        // Check if the map forms a complete rectangle from (0,0) to (maxX, maxY)
        // and if all nodes within this rectangle are present.
        // Valid half-map dimensions are 10x5 (maxX=9, maxY=4) or 5x10 (maxX=4, maxY=9).
        boolean dimensionsValid = ((maxX + 1 == 10 && maxY + 1 == 5) || (maxX + 1 == 5 && maxY + 1 == 10));
        if (!dimensionsValid) {
             notification.addError(String.format("Invalid map dimensions. Expected 10x5 or 5x10, but got %dx%d (maxX=%d, maxY=%d).",
                                                maxX + 1, maxY + 1, maxX, maxY));
        } else {
            // Check for missing nodes within the inferred rectangle
            for (int x = 0; x <= maxX; x++) {
                for (int y = 0; y <= maxY; y++) {
                    if (halfMap.getMapNode(x, y) == null) {
                        notification.addError("Missing map node at coordinates X=" + x + ", Y=" + y + " within the " + (maxX+1) + "x" + (maxY+1) + " grid.");
                    }
                }
            }
        }
        
        if (notification.hasErrors()) return notification;


        validateTerrainAndCastle(halfMap, notification);
        
        // Only proceed with complex validations if basic structure and terrain are okay
        if (!notification.hasErrors()) {
            validateReachability(halfMap, notification, maxX, maxY);
            validateEdgeWalkability(halfMap, notification, maxX, maxY);
        }

        return notification;
    }

    /**
     * Validates terrain distribution, castle placement, and terrain-related rules.
     * Checks that the map contains the required minimum percentages of each terrain type
     * and that exactly one castle is placed on a grass field.
     * 
     * @param halfMap The PlayerHalfMap to validate
     * @param notification The Notification object to add errors to
     */
    private void validateTerrainAndCastle(PlayerHalfMap halfMap, Notification notification) {
        List<MapNode> nodes = halfMap.getMapNodes();
        int mountainCount = 0;
        int grassCount = 0;
        int waterCount = 0;
        int castleCount = 0;
        MapNode castleNode = null;

        for (MapNode node : nodes) {
            if (node.getTerrain() == null) {
                 notification.addError("Node at X=" + node.getX() + ", Y=" + node.getY() + " has null terrain.");
                 continue; // Skip this node for terrain counting to avoid NullPointerException
            }
            switch (node.getTerrain()) {
                case MOUNTAIN: mountainCount++; break;
                case GRASS: grassCount++; break;
                case WATER: waterCount++; break;
                default: // Should not happen with an enum
                    notification.addError("Node at X=" + node.getX() + ", Y=" + node.getY() + " has an unknown terrain type.");
                    break;
            }

            if (node.isFortPresent()) {
                castleCount++;
                castleNode = node;
            }
        }
        if (notification.hasErrors()) return; // Stop if null terrains found

        int minMountains = (int) Math.round(MIN_MOUNTAIN_PERCENTAGE * HALF_MAP_TOTAL_NODES);
        int minGrass = (int) Math.round(MIN_GRASS_PERCENTAGE * HALF_MAP_TOTAL_NODES);
        int minWater = (int) Math.round(MIN_WATER_PERCENTAGE * HALF_MAP_TOTAL_NODES);
        int expectedCastles = (int) (CASTLE_PERCENTAGE * HALF_MAP_TOTAL_NODES); // This will be 6

        if (mountainCount < minMountains) {
            notification.addError(String.format("Insufficient mountains. Required: >=%d (%.0f%%), Found: %d", minMountains, MIN_MOUNTAIN_PERCENTAGE * 100, mountainCount));
        }
        if (grassCount < minGrass) {
            notification.addError(String.format("Insufficient grass. Required: >=%d (%.0f%%), Found: %d", minGrass, MIN_GRASS_PERCENTAGE * 100, grassCount));
        }
        if (waterCount < minWater) {
            notification.addError(String.format("Insufficient water. Required: >=%d (%.0f%%), Found: %d", minWater, MIN_WATER_PERCENTAGE * 100, waterCount));
        }
        
        if (mountainCount + grassCount + waterCount != HALF_MAP_TOTAL_NODES) {
             notification.addError("Sum of terrain types (" + (mountainCount + grassCount + waterCount) +
                                   ") does not match total nodes (" + HALF_MAP_TOTAL_NODES + "). Check for unassigned or miscounted terrains.");
        }

        if (castleCount != expectedCastles) {
            notification.addError("Incorrect number of castles. Required: " + expectedCastles + ", Found: " + castleCount);
        } else if (castleNode != null) { // castleCount == 1
            if (castleNode.getTerrain() != Terrain.GRASS) {
                notification.addError("Castle must be placed on a GRASS field. Found on: " + castleNode.getTerrain() + " at X=" + castleNode.getX() + ", Y=" + castleNode.getY());
            }
        } else if (expectedCastles > 0) { // castleCount is 0 but expected > 0
             notification.addError("Expected " + expectedCastles + " castle(s) but none found.");
        }
    }

    /**
     * Validates that all walkable nodes (grass and mountain) are reachable from each other.
     * Uses breadth-first search (BFS) starting from the fort position (or first walkable node)
     * to ensure no walkable areas are isolated by water or map boundaries.
     * 
     * @param halfMap The PlayerHalfMap to validate
     * @param notification The Notification object to add errors to
     * @param maxX The maximum X coordinate of the map
     * @param maxY The maximum Y coordinate of the map
     */
    private void validateReachability(PlayerHalfMap halfMap, Notification notification, int maxX, int maxY) {
        List<MapNode> walkableNodes = halfMap.getMapNodes().stream()
                                             .filter(node -> node.isWalkable())
                                             .collect(Collectors.toList());

        if (walkableNodes.isEmpty()) {
            // Check if map rules *require* walkable nodes (grass/mountain minimums > 0)
            if ((MIN_GRASS_PERCENTAGE + MIN_MOUNTAIN_PERCENTAGE) > 0) {
                 notification.addError("No walkable nodes found on the map, but map rules require grass or mountain fields.");
            }
            return; // No walkable nodes, so reachability is trivially met or irrelevant.
        }

        Set<MapNode> visited = new HashSet<>();
        Queue<MapNode> queue = new LinkedList<>();
        
        MapNode startNode = halfMap.getFortNode();
        if (startNode == null || !startNode.isWalkable()) {
            // If no fort, or fort is not walkable, pick the first available walkable node
            startNode = walkableNodes.get(0); 
        }
        
        queue.add(startNode);
        visited.add(startNode);

        int[] dX = {0, 0, 1, -1}; // For N, S, E, W neighbors
        int[] dY = {1, -1, 0, 0};

        while (!queue.isEmpty()) {
            MapNode current = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nextX = current.getX() + dX[i];
                int nextY = current.getY() + dY[i];

                if (nextX >= 0 && nextX <= maxX && nextY >= 0 && nextY <= maxY) {
                    MapNode neighbor = halfMap.getMapNode(nextX, nextY);
                    if (neighbor != null && neighbor.isWalkable() && !visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (visited.size() != walkableNodes.size()) {
            notification.addError("Not all walkable fields are reachable from each other. Visited: " + visited.size() + ", Total Walkable: " + walkableNodes.size());
        }
    }
    
    /**
     * Validates walkability requirements for a single edge of the map.
     * Checks that at least 51% of nodes on the specified edge are walkable (grass or mountain).
     * 
     * @param halfMap The PlayerHalfMap being validated
     * @param notification The Notification object to add errors to
     * @param fixedCoordVal The fixed coordinate value (X for vertical edges, Y for horizontal edges)
     * @param isXFixed True if validating a vertical edge (X is fixed), false for horizontal edge (Y is fixed)
     * @param startVarCoord The starting value of the variable coordinate
     * @param endVarCoord The ending value of the variable coordinate
     * @param edgeName A descriptive name for the edge being validated (for error messages)
     */
    private void checkSingleEdge(PlayerHalfMap halfMap, Notification notification,
                                 int fixedCoordVal, boolean isXFixed,
                                 int startVarCoord, int endVarCoord,
                                 String edgeName) {
        int totalEdgeNodes = 0;
        int walkableEdgeNodes = 0;

        for (int varCoord = startVarCoord; varCoord <= endVarCoord; varCoord++) {
            int x, y;
            if (isXFixed) { // Edge is vertical, X is fixed
                x = fixedCoordVal;
                y = varCoord;
            } else { // Edge is horizontal, Y is fixed
                x = varCoord;
                y = fixedCoordVal;
            }

            MapNode node = halfMap.getMapNode(x, y);
            // Node existence should be guaranteed by prior checks if this point is reached without errors
            if (node != null) { 
                totalEdgeNodes++;
                if (node.isWalkable()) {
                    walkableEdgeNodes++;
                }
            } else {
                // This should ideally be caught earlier by map completeness checks.
                notification.addError("Critical: Missing node on " + edgeName + " at X=" + x + ", Y=" + y + " during edge walkability check. Aborting this check.");
                return; 
            }
        }
        
        if (totalEdgeNodes == 0 && (endVarCoord >= startVarCoord) ) {
             notification.addError(edgeName + " has an effective length of 0 or no nodes were found, which is unexpected for a valid map structure.");
            return;
        }
        if (totalEdgeNodes == 0) return; // Edge has no length (e.g. startVarCoord > endVarCoord)

        int requiredWalkableNodes = (int) Math.ceil(MIN_EDGE_WALKABLE_PERCENTAGE * totalEdgeNodes);

        if (walkableEdgeNodes < requiredWalkableNodes) {
            String requiredPercentageStr = String.format("%.0f%%", MIN_EDGE_WALKABLE_PERCENTAGE * 100);
            notification.addError(String.format("%s walkability below threshold. Required: >=%s (%d nodes), Found: %d/%d nodes.",
                                  edgeName, requiredPercentageStr, requiredWalkableNodes, walkableEdgeNodes, totalEdgeNodes));
        }
    }

    /**
     * Validates that all four edges of the map meet the minimum walkability requirement.
     * Each edge (top, bottom, left, right) must have at least 51% walkable nodes.
     * This ensures the map can be properly connected to adjacent maps in the full game.
     * 
     * @param halfMap The PlayerHalfMap to validate
     * @param notification The Notification object to add errors to
     * @param maxX The maximum X coordinate of the map
     * @param maxY The maximum Y coordinate of the map
     */
    private void validateEdgeWalkability(PlayerHalfMap halfMap, Notification notification, int maxX, int maxY) {
        // Assuming map coordinates start at (0,0) as per earlier checks (minX=0, minY=0)
        int minX = 0; 
        int minY = 0;

        // Top edge: Y is fixed at minY, X varies from minX to maxX
        checkSingleEdge(halfMap, notification, minY, false, minX, maxX, "Top Edge (Y=" + minY + ")");
        // Bottom edge: Y is fixed at maxY, X varies from minX to maxX
        checkSingleEdge(halfMap, notification, maxY, false, minX, maxX, "Bottom Edge (Y=" + maxY + ")");
        // Left edge: X is fixed at minX, Y varies from minY to maxY
        checkSingleEdge(halfMap, notification, minX, true, minY, maxY, "Left Edge (X=" + minX + ")");
        // Right edge: X is fixed at maxX, Y varies from minY to maxY
        checkSingleEdge(halfMap, notification, maxX, true, minY, maxY, "Right Edge (X=" + maxX + ")");
    }
}