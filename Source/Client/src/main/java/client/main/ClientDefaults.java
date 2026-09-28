package client.main;

/**
 * Default player identity used by {@link MainClient}.
 *
 * <p>This keeps the command line short for local runs; change these values if you need to
 * register a different player.
 */
final class ClientDefaults {

    private ClientDefaults() {
    }

    static final String PLAYER_FIRST_NAME = "Redacted_first_name";
    static final String PLAYER_LAST_NAME = "Redacted_last_name";
    static final String PLAYER_UACCOUNT = "Redacted_uaccount";
}
