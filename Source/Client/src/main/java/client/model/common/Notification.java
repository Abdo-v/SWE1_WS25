package client.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Notification {
    private List<Error> errors = new ArrayList<>();

    public void addError(String info, Exception e) {
        errors.add(new Error(info, e));
    }

    public void addError(String info) {
        // Capture stack trace at the call site (typically within a validator)
        // so technical-internals views can reference the relevant validation logic.
        errors.add(new Error(info, new Exception("Validation error")));
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
            this.info = info;
            this.cause = Optional.ofNullable(cause).orElseGet(Optional::empty);
        }

        public Error(String info, Exception cause) {
            this(info, Optional.ofNullable(cause));
        }

        @Override
        public String toString() {
            return info + cause
                    .map(ex -> " (Cause: " + ex.getMessage() + ")")
                    .orElse("");
        }
    }
}