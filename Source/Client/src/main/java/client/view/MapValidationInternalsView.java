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
        System.err.println("Type: Half-map validation errors");
        System.err.println("Details:");

        int index = 1;
        for (Notification.Error error : notification.getErrors()) {
            String message = safeText(error.info);
            StackTraceElement referenceFrame = error.cause
                .map(MapValidationInternalsView::findTopRelevantFrame)
                .orElse(null);
            String kind = determineKind(referenceFrame, message);
            String reference = referenceFrame == null
                ? "(no stack trace reference available)"
                : formatFrameLikeStackTrace(referenceFrame);

            System.err.println("  " + index + ") Kind: " + kind);
            System.err.println("     Message: " + message);
            System.err.println("     Reference: " + reference);
            index++;
        }
        System.err.println();
    }

    private static String safeText(String text) {
        return text == null ? "(no message)" : text;
    }

    private static StackTraceElement findTopRelevantFrame(Exception ex) {
        if (ex == null) {
            return null;
        }

        StackTraceElement[] frames = ex.getStackTrace();
        if (frames == null || frames.length == 0) {
            return null;
        }

        // Prefer frames that look like validation logic.
        for (StackTraceElement frame : frames) {
            String className = frame.getClassName();
            if (className == null) {
                continue;
            }
            String lower = className.toLowerCase();
            if (lower.contains("validator") || lower.contains("mapvalidator")) {
                return frame;
            }
        }

        // Fallback: first frame.
        return frames[0];
    }

    private static String determineKind(StackTraceElement referenceFrame, String message) {
        if (referenceFrame != null) {
            String className = String.valueOf(referenceFrame.getClassName());
            if (className.contains("HalfMapStructureValidator")) {
                return "STRUCTURE";
            }
            if (className.contains("HalfMapTerrainValidator")) {
                return "TERRAIN_AND_FORT";
            }
            if (className.contains("HalfMapReachabilityValidator")) {
                return "REACHABILITY";
            }
            if (className.contains("HalfMapEdgeValidator")) {
                return "EDGE_CONSTRAINTS";
            }
            if (className.contains("MapValidator")) {
                return "ORCHESTRATION";
            }
        }

        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("must be provided") || lower.contains("missing")) {
                return "PRECONDITION";
            }
            if (lower.contains("dimension") || lower.contains("width") || lower.contains("height")) {
                return "DIMENSIONS";
            }
        }

        return "VALIDATION";
    }

    private static String formatFrameLikeStackTrace(StackTraceElement frame) {
        String className = String.valueOf(frame.getClassName());
        String methodName = String.valueOf(frame.getMethodName());
        String fileName = frame.getFileName();
        int line = frame.getLineNumber();

        if (fileName != null && line > 0) {
            return "at " + className + "." + methodName + "(" + fileName + ":" + line + ")";
        }
        if (fileName != null) {
            return "at " + className + "." + methodName + "(" + fileName + ")";
        }
        return "at " + className + "." + methodName + "()";
    }
}
