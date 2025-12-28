package client.controller.network.service;

import client.controller.ControllerTextConfig;
import client.exception.GameCommunicationException;
import messagesbase.ResponseEnvelope;
import messagesbase.messagesfromclient.ERequestState;

import java.util.Objects;

/**
 * Validates {@link ResponseEnvelope} and translates server-side rejections into
 * {@link GameCommunicationException} with consistent messages.
 */
final class NetworkCenterEnvelopeValidator {

    void throwOnServerRejection(String operation, ResponseEnvelope<?> envelope, String serverBaseUrl)
            throws GameCommunicationException {
        Objects.requireNonNull(serverBaseUrl, ControllerTextConfig.REQUIRE_SERVER_BASE_URL);
        Objects.requireNonNull(envelope, ControllerTextConfig.REQUIRE_ENVELOPE);

        if (envelope.getState() == ERequestState.Error) {
            throw new GameCommunicationException(
                    formatServerRejection(operation, envelope),
                    serverBaseUrl,
                    operation,
                    ControllerTextConfig.HTTP_STATUS_SERVER_REJECTED,
                    envelope.getExceptionName(),
                    envelope.getExceptionMessage()
            );
        }
    }

    private static String formatServerRejection(String operation, ResponseEnvelope<?> envelope) {
        String safeOp = ControllerTextConfig.safeStringOrDefault(operation, ControllerTextConfig.FALLBACK_OPERATION_UNKNOWN);
        String exceptionName = ControllerTextConfig.safeStringOrDefault(envelope.getExceptionName(), ControllerTextConfig.FALLBACK_SERVER_ERROR_NAME).trim();
        String exceptionMessage = ControllerTextConfig.safeStringOrDefault(envelope.getExceptionMessage(), "").trim();
        if (!exceptionMessage.isBlank()) {
            return ControllerTextConfig.SERVER_REJECTED_PREFIX + safeOp
                    + ControllerTextConfig.SERVER_REJECTED_SEPARATOR_1 + exceptionName
                    + ControllerTextConfig.SERVER_REJECTED_SEPARATOR_2 + exceptionMessage;
        }
        return ControllerTextConfig.SERVER_REJECTED_PREFIX + safeOp
                + ControllerTextConfig.SERVER_REJECTED_SEPARATOR_1 + exceptionName;
    }
}
