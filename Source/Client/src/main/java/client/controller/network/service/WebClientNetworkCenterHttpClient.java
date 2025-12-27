package client.controller.network.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import messagesbase.ResponseEnvelope;

/**
 * {@link NetworkCenterHttpClient} implementation backed by Spring {@link WebClient}.
 */
final class WebClientNetworkCenterHttpClient implements NetworkCenterHttpClient {

    private final WebClient webClient;

    WebClientNetworkCenterHttpClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public <T> ResponseEnvelope<T> post(
            String uri,
            Object body,
            ParameterizedTypeReference<ResponseEnvelope<T>> responseType
    ) {
        return webClient
                .method(HttpMethod.POST)
                .uri(uri)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    @Override
    public <T> ResponseEnvelope<T> get(String uri, ParameterizedTypeReference<ResponseEnvelope<T>> responseType) {
        return webClient
                .method(HttpMethod.GET)
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
