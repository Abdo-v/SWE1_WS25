package client.view;

import client.model.PlayerState;

import java.util.Objects;

/**
 * Formats user-facing player summary text for CLI output.
 *
 * <p>Intentionally excludes technical/internal details from {@link PlayerState#toString()}.
 */
final class PlayerSummaryFormatter {

    String format(PlayerState playerState, int movesMade) {
        Objects.requireNonNull(playerState, "playerState is required");

        String id = displayOrUnknown(playerState.getPlayerID());
        String firstName = displayOrUnknown(playerState.getFirstName());
        String lastName = displayOrUnknown(playerState.getLastName());
        String uAccount = displayOrUnknown(playerState.getUAccount());

        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(firstName).append(' ').append(lastName).append('\n');
        sb.append("uAccount: ").append(uAccount).append('\n');
        sb.append("ID: ").append(id).append('\n');
        sb.append("Moves made: ").append(Math.max(0, movesMade)).append('\n');
        return sb.toString();
    }

    private static String displayOrUnknown(String value) {
        String trimmed = Objects.requireNonNullElse(value, "").trim();
        return trimmed.isEmpty() ? "(unknown)" : trimmed;
    }
}
