package client.model.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Notification {
    private List<Error> errors = new ArrayList<>();

    public void addError(String info, Exception e) {
        errors.add(new Error(info, e));
    }

    public void addError(String info) {
        errors.add(new Error(info, null));
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
        public final Exception cause;

        public Error(String info, Exception cause) {
            this.info = info;
            this.cause = cause;
        }

        @Override
        public String toString() {
            return info + (cause != null ? " (Cause: " + cause.getMessage() + ")" : "");
        }
    }
}