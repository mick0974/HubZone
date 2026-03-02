package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
public class ApiTemplateHelper {

    private final RestClient restClient;

    public ApiTemplateHelper(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T execute(
            String url,
            Map<String, String> queryParams,
            HttpMethod method,
            HttpHeaders headers,
            Object requestBody,
            Class<T> responseType,
            ErrorStatusHandlerRegistry errorHandlers
    ) {
        RestClient.ResponseSpec responseSpec = executeApiCall(url, queryParams, method, headers, requestBody, errorHandlers);

        if (responseType == Void.class) {
            responseSpec.toBodilessEntity();
            return null;
        } else {
            return responseSpec.body(responseType);
        }
    }

    public <T> T execute(
            String url,
            Map<String, String> queryParams,
            HttpMethod method,
            HttpHeaders headers,
            Object requestBody,
            ParameterizedTypeReference<T> responseType,
            ErrorStatusHandlerRegistry errorHandlers
    ) {

        RestClient.ResponseSpec responseSpec = executeApiCall(url, queryParams, method, headers, requestBody, errorHandlers);
        return responseSpec.body(responseType);
    }

    private RestClient.ResponseSpec executeApiCall(
            String url,
            Map<String, String> queryParams,
            HttpMethod method,
            HttpHeaders headers,
            Object requestBody,
            ErrorStatusHandlerRegistry errorHandlers
    ) {
        if (headers == null) {
            headers = new HttpHeaders();
        }

        if (queryParams != null)
            url = extendUrlWithQueryParams(url, queryParams);

        HttpHeaders finalHeaders = headers;
        RestClient.RequestBodySpec requestSpec = restClient
                .method(method)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(finalHeaders));

        if (requestBody != null) {
            requestSpec = requestSpec.body(requestBody);
        }

        log.info("Request URL: {}", url);
        return requestSpec
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        ((request, response) ->
                                errorHandlers.handle(HttpStatus.valueOf(response.getStatusCode().value()), response)));
    }

    private String extendUrlWithQueryParams(String url, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        queryParams.forEach(builder::queryParam);

        return builder.toUriString();
    }
}
