package client.controller.network.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import client.controller.ControllerTextConfig;
import client.exception.GameCommunicationException;
import messagesbase.ResponseEnvelope;

import java.util.Objects;

/**
 * Transport wrapper around {@link NetworkCenterHttpClient} that maps low-level HTTP/network failures
 * into {@link GameCommunicationException} with consistent messages.
 */
final class NetworkCenterTransport {

    private static final int HTTP_STATUS_UNKNOWN = -1;

    private final String serverBaseUrl;
    private final NetworkCenterHttpClient httpClient;

    NetworkCenterTransport(String serverBaseUrl, NetworkCenterHttpClient httpClient) {
        this.serverBaseUrl = Objects.requireNonNull(serverBaseUrl, ControllerTextConfig.REQUIRE_SERVER_BASE_URL);
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is required");
    }

    <T> ResponseEnvelope<T> post(
            String operation,
            String httpErrorMessagePrefix,
            String networkErrorMessage,
            String path,
            Object requestBody,
            ParameterizedTypeReference<ResponseEnvelope<T>> responseType
    ) throws GameCommunicationException {
        try {
            return httpClient.post(path, requestBody, responseType);
        } catch (WebClientResponseException e) {
            throw new GameCommunicationException(
                    httpErrorMessagePrefix + e.getMessage(),
                    e,
                    serverBaseUrl,
                    operation,
                    e.getStatusCode().value()
            );
        } catch (Exception e) {
            throw new GameCommunicationException(
                    networkErrorMessage,
                    e,
                    serverBaseUrl,
                    operation,
                    HTTP_STATUS_UNKNOWN
            );
        }
    }

    <T> ResponseEnvelope<T> get(
            String operation,
            String httpErrorMessagePrefix,
            String networkErrorMessage,
            String path,
            ParameterizedTypeReference<ResponseEnvelope<T>> responseType
    ) throws GameCommunicationException {
        try {
            return httpClient.get(path, responseType);
        } catch (WebClientResponseException e) {
            throw new GameCommunicationException(
                    httpErrorMessagePrefix + e.getMessage(),
                    e,
                    serverBaseUrl,
                    operation,
                    e.getStatusCode().value()
            );
        } catch (Exception e) {
            throw new GameCommunicationException(
                    networkErrorMessage,
                    e,
                    serverBaseUrl,
                    operation,
                    ControllerTextConfig.HTTP_STATUS_UNKNOWN_INT
            );
        }
    }
}
