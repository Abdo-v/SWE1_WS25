package client.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Notification {
    private final List<Error> errors = new ArrayList<>();

    public void addError(String info, Exception e) {
        errors.add(new Error(
                Objects.requireNonNull(info, "info is required"),
                Objects.requireNonNull(e, "exception is required")
        ));
    }

    public void addError(String info) {
        // Capture stack trace at the call site (typically within a validator)
        // so technical-internals views can reference the relevant validation logic.
        errors.add(new Error(
                Objects.requireNonNull(info, "info is required"),
                new Exception("Validation error")
        ));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String getErrorMessages() {
        if (errors.isEmpty()) {
            return "";
        }
        return errors.stream()
                .map(eachError -> eachError.info)
                .collect(Collectors.joining(", "));
    }

    public List<Error> getErrors() {
        return new ArrayList<>(errors); // Return a copy
    }

    public static class Error {
        public final String info;
        public final Optional<Exception> cause;

        public Error(String info, Optional<Exception> cause) {
            this.info = Objects.requireNonNull(info, "info is required");
            this.cause = Objects.requireNonNull(cause, "cause is required");
        }

        public Error(String info, Exception cause) {
            this(info, Optional.of(Objects.requireNonNull(cause, "exception is required")));
        }

        @Override
        public String toString() {
            return info + cause
                    .map(ex -> " (Cause: " + ex.getMessage() + ")")
                    .orElse("");
        }
    }
}