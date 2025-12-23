package client.view;

import client.model.common.Notification;

import java.util.Objects;

/**
 * CLI visualization for technical internals during map generation.
 *
 * <p>Requirement: print validation problems to {@link System#err} with:
 * <ul>
 *   <li>coarse error kind</li>
 *   <li>helpful explanation</li>
 *   <li>reference to relevant validation logic (class + method) based on stack trace</li>
 * </ul>
 */
public class MapValidationInternalsView {

    public void report(Notification notification) {
        Objects.requireNonNull(notification, "notification must not be null");

        if (!notification.hasErrors()) {
            return;
        }

        System.err.println("\n🧪 Map Validation Internals");
        System.err.println("Type: Half-map validation failure");
        System.err.println("Details:");

        int index = 1;
        for (Notification.Error error : notification.getErrors()) {
            System.err.println("  " + index + ") " + safeText(error.info));

            String reference = error.cause
                    .map(MapValidationInternalsView::formatTopRelevantFrame)
                    .orElse("(no stack trace reference available)");

            System.err.println("     Reference: " + reference);
            index++;
        }
        System.err.println();
    }

    private static String safeText(String text) {
        return text == null ? "(no message)" : text;
    }

    private static String formatTopRelevantFrame(Exception ex) {
        if (ex == null) {
            return "(no stack trace reference available)";
        }

        StackTraceElement[] frames = ex.getStackTrace();
        if (frames == null || frames.length == 0) {
            return "(no stack trace reference available)";
        }

        // Prefer validator-related frames if present.
        for (StackTraceElement frame : frames) {
            String className = frame.getClassName();
            if (className != null && (className.contains("validator") || className.contains("Validator") || className.contains("MapValidator"))) {
                return className + "." + frame.getMethodName() + "()";
            }
        }

        // Fallback: first frame.
        StackTraceElement first = frames[0];
        return first.getClassName() + "." + first.getMethodName() + "()";
    }
}
