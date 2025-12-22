package client.model.mapper;

import client.model.common.Notification;

import java.util.List;

final class HalfMapTerrainValidator {
    private HalfMapTerrainValidator() {
    }

    static void validateTerrainAndFort(
            PlayerHalfMap halfMap,
            Notification notification,
            int totalNodes,
            double minMountainPercentage,
            double minGrassPercentage,
            double minWaterPercentage,
            double fortPercentage
    ) {
        List<MapNode> nodes = halfMap.getMapNodes();
        int mountainCount = 0;
        int grassCount = 0;
        int waterCount = 0;
        int fortCount = 0;
        MapNode fortNode = null;

        for (MapNode node : nodes) {
            if (node.getTerrain() == null) {
                notification.addError("Node at X=" + node.getX() + ", Y=" + node.getY() + " has null terrain.");
                continue;
            }
            switch (node.getTerrain()) {
                case MOUNTAIN:
                    mountainCount++;
                    break;
                case GRASS:
                    grassCount++;
                    break;
                case WATER:
                    waterCount++;
                    break;
                default:
                    notification.addError("Node at X=" + node.getX() + ", Y=" + node.getY() + " has an unknown terrain type.");
                    break;
            }

            if (node.isFortPresent()) {
                fortCount++;
                fortNode = node;
            }
        }

        if (notification.hasErrors()) {
            return;
        }

        int minMountains = (int) Math.round(minMountainPercentage * totalNodes);
        int minGrass = (int) Math.round(minGrassPercentage * totalNodes);
        int minWater = (int) Math.round(minWaterPercentage * totalNodes);
        int expectedForts = (int) (fortPercentage * totalNodes);

        if (mountainCount < minMountains) {
            notification.addError(String.format("Insufficient mountains. Required: >=%d (%.0f%%), Found: %d",
                    minMountains, minMountainPercentage * 100, mountainCount));
        }
        if (grassCount < minGrass) {
            notification.addError(String.format("Insufficient grass. Required: >=%d (%.0f%%), Found: %d",
                    minGrass, minGrassPercentage * 100, grassCount));
        }
        if (waterCount < minWater) {
            notification.addError(String.format("Insufficient water. Required: >=%d (%.0f%%), Found: %d",
                    minWater, minWaterPercentage * 100, waterCount));
        }

        if (mountainCount + grassCount + waterCount != totalNodes) {
            notification.addError("Sum of terrain types (" + (mountainCount + grassCount + waterCount)
                    + ") does not match total nodes (" + totalNodes + "). Check for unassigned or miscounted terrains.");
        }

        if (fortCount != expectedForts) {
            notification.addError("Incorrect number of forts. Required: " + expectedForts + ", Found: " + fortCount);
        } else if (fortNode != null) {
            if (fortNode.getTerrain() != Terrain.GRASS) {
                notification.addError("Fort must be placed on a GRASS field. Found on: " + fortNode.getTerrain()
                        + " at X=" + fortNode.getX() + ", Y=" + fortNode.getY());
            }
        } else if (expectedForts > 0) {
            notification.addError("Expected " + expectedForts + " fort(s) but none found.");
        }
    }
}
