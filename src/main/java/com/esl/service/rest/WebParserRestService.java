package com.esl.service.rest;

import com.esl.entity.rest.DictionaryResult;
import com.esl.entity.rest.WebItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class WebParserRestService {
    final String host;

    @Autowired
    ExecutorService executorService;

    @Autowired
    RestTemplate restTemplate;

    public WebParserRestService(@Value("${APISERVER_HOST:}") String apiHost) {
        if (!apiHost.startsWith("http://") && !apiHost.startsWith("https://")) apiHost = "http://" + apiHost;
        if (!apiHost.endsWith("/")) apiHost = apiHost + "/";
        this.host = apiHost;
    }

    public CompletableFuture<WebItem[]> searchGoogleImage(String query, int start) {
        String url = host + "rest/search/image/" + query + "?imgSize=all&start=" + start;
        return CompletableFuture.supplyAsync(() ->
                restTemplate.getForObject(url, WebItem[].class), executorService);
    }

    public CompletableFuture<Optional<DictionaryResult>> queryDictionary(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.ofNullable(restTemplate.getForObject(host + "rest/dictionary/" + query, DictionaryResult.class));
            } catch (Exception ex)   {
                return Optional.empty();
            }
        }, executorService);
    }

}
