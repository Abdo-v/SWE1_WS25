package client.view;

/**
 * Marker class to document legacy CLI components.
 *
 * <p>This project historically used {@link CLIHandler} for letter-based map output.
 * The current assignment requires emoji-based visualizations and plain text output.
 *
 * <p>{@link CLIHandler} remains in the codebase for compatibility with existing wiring,
 * but new features should prefer the dedicated views:
 * <ul>
 *   <li>{@link MapGenerationView} for map generation visualization</li>
 *   <li>{@link MapValidationInternalsView} for validation internals (System.err)</li>
 *   <li>{@link DynamicCLIGameView} for in-game visualization</li>
 * </ul>
 */
public final class LegacyCliHandlerNotice {
    private LegacyCliHandlerNotice() {
    }
}
