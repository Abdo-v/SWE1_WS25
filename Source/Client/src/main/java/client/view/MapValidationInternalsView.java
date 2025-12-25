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
        Objects.requireNonNull(notification, "notification is required");

        if (!notification.hasErrors()) {
            return;
        }

        System.err.println("\n🧪 Map Validation Internals");
        System.err.println("Type: Half-map validation errors");
        System.err.println("Details:");

        int index = 1;
        for (Notification.Error error : notification.getErrors()) {
            String message = safeText(error.info);
            var referenceFrame = error.cause
                    .flatMap(MapValidationInternalsView::findTopRelevantFrame);
            ValidationInternalsKind kind = ValidationInternalsKind.from(referenceFrame, message);
            String reference = referenceFrame
                    .map(MapValidationInternalsView::formatFrameLikeStackTrace)
                    .orElse("(no stack trace reference available)");

            System.err.println("  " + index + ") Kind: " + kind.displayText());
            System.err.println("     Message: " + message);
            System.err.println("     Reference: " + reference);
            index++;
        }
        System.err.println();
    }

    private static String safeText(String text) {
        return Objects.requireNonNullElse(text, "(no message)");
    }

    private static java.util.Optional<StackTraceElement> findTopRelevantFrame(Exception ex) {
        Objects.requireNonNull(ex, "exception is required");
        StackTraceElement[] frames = Objects.requireNonNullElse(ex.getStackTrace(), new StackTraceElement[0]);
        if (frames.length == 0) {
            return java.util.Optional.empty();
        }

        // Prefer frames that look like validation logic.
        for (StackTraceElement frame : frames) {
            String lower = frame.getClassName().toLowerCase();
            if (lower.contains("validator") || lower.contains("mapvalidator")) {
                return java.util.Optional.of(frame);
            }
        }

        // Fallback: first frame.
        return java.util.Optional.of(frames[0]);
    }
    private static String formatFrameLikeStackTrace(StackTraceElement frame) {
        String className = frame.getClassName();
        String methodName = frame.getMethodName();
        String fileName = Objects.requireNonNullElse(frame.getFileName(), "");
        int line = frame.getLineNumber();

        if (!fileName.isBlank() && line > 0) {
            return "at " + className + "." + methodName + "(" + fileName + ":" + line + ")";
        }
        if (!fileName.isBlank()) {
            return "at " + className + "." + methodName + "(" + fileName + ")";
        }
        return "at " + className + "." + methodName + "()";
    }
}
