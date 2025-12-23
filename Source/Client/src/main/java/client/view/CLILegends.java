package client.view;

/**
 * Builds consistent legend strings for CLI map visualizations.
 *
 * <p>Package-private to avoid Windows case-only rename problems while still
 * keeping the class name consistently spelled as "CLI".
 */
final class CLILegends {

    private CLILegends() {
    }

    static String halfMapLegend() {
        return "Legend: "
                + CLIIcons.GRASS + "=grass "
                + CLIIcons.MOUNTAIN + "=mountain "
                + CLIIcons.WATER + "=water "
                + CLIIcons.OWN_FORT + "=fort";
    }

    static String fullMapLegend() {
        return "Legend: "
                + CLIIcons.GRASS + "=grass "
                + CLIIcons.MOUNTAIN + "=mountain "
                + CLIIcons.WATER + "=water "
                + CLIIcons.OWN_FORT + "/" + CLIIcons.OPPONENT_FORT + "=fort "
                + CLIIcons.TREASURE + "=treasure "
                + CLIIcons.PLAYER + "/" + CLIIcons.PLAYER_WITH_TREASURE + "=you "
                + CLIIcons.OPPONENT + "/" + CLIIcons.OPPONENT_WITH_TREASURE + "=opponent "
                + CLIIcons.CLASH + "=clash";
    }
}
