package com.esl.service.rest;

import com.esl.entity.rest.DictionaryResult;
import com.esl.entity.rest.WebItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class WebParserRestService {
    private final String host;

    @Autowired
    ExecutorService executorService;

    @Autowired
    RestTemplate restTemplate;

    public WebParserRestService(@Value("${APISERVER_HOST}") String apiHost) {
        if (StringUtils.isBlank(apiHost)) throw new IllegalArgumentException("Cannot create WebParserRestService without API server's host");

        if (!apiHost.contains("http://")) apiHost = "http://" + apiHost;
        if (!apiHost.endsWith("/")) apiHost = apiHost + "/";
        this.host = apiHost;
    }

    public CompletableFuture<WebItem[]> searchGoogleImage(String query) {
        return CompletableFuture.supplyAsync(() ->
                restTemplate.getForObject(host + "rest/search/image/" + query, WebItem[].class), executorService);
    }

    public CompletableFuture<Optional<DictionaryResult>> queryDictionary(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.ofNullable(restTemplate.getForObject(host + "rest/dictionary/" + query, DictionaryResult.class));
            } catch (HttpClientErrorException ex)   {
                return Optional.empty();
            }
        }, executorService);
    }

}
