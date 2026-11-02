package com.esl.service;

import com.esl.dao.PhoneticQuestionDAO;
import com.esl.entity.rest.DictionaryResult;
import com.esl.entity.rest.WebItem;
import com.esl.model.PhoneticQuestion;
import com.esl.service.rest.WebParserRestService;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhoneticQuestionService {
    private static final Logger log = LoggerFactory.getLogger(PhoneticQuestionService.class);

    @Autowired
    private PhoneticQuestionDAO phoneticQuestionDAO;
    @Autowired private WebParserRestService webService;
    @Autowired private RestTemplate restTemplate;

    public Optional<PhoneticQuestion> getQuestionFromDB(String word) {
        var question = Optional.ofNullable(phoneticQuestionDAO.getPhoneticQuestionByWord(word));
        // Empty path hides the legacy PIC_FILE_NAME. The client loads images separately.
        question.ifPresent(q -> q.setPicsFullPaths(new String[] { "" }));
        return question;
    }

    public PhoneticQuestion buildQuestionByWebAPI(String word) {
        log.info("buildQuestionByWebAPI for word: {}", word);
        var question = new PhoneticQuestion();
        question.setWord(word);
        question.setPicsFullPaths(new String[] { "" });

        fillQuestionByDictionaryResult(question, webService.queryDictionary(word).join());
        return question;
    }

    private void fillQuestionByDictionaryResult(PhoneticQuestion question, Optional<DictionaryResult> r) {
        if (r.isEmpty()) {
            question.setIPAUnavailable(true);
        } else {
            DictionaryResult result = r.get();

            // only use pronunciation and IPA if exact match
            if (org.apache.commons.lang3.StringUtils.isNotBlank(result.word)
                    && org.apache.commons.lang3.StringUtils.isNotBlank(question.getWord())
                    && !result.word.equalsIgnoreCase(question.getWord())) {
                log.info("Dictionary resolved '{}' to base form '{}'; skipping pronunciationUrl so client uses TTS",
                        question.getWord(), result.word);
                question.setIPAUnavailable(true);
                question.setPronouncedLink(null);
                return;
            }

            if (org.apache.commons.lang3.StringUtils.isNotBlank(result.IPA))
                question.setIPA(result.IPA);
            else
                question.setIPAUnavailable(true);

            question.setPronouncedLink(result.pronunciationUrl);
        }
    }

    public List<WebItem> getImagesFromWeb(String word, int start) {
        log.debug("getImagesFromWeb for {}", word);

        try {
            WebItem[] items = webService.searchGoogleImage(word, start).join();
            return Arrays.stream(items)
                    .filter(i -> !i.thumbnailUrl.endsWith("svg"))
                    .map(i -> {
                        retrieveImageToString(i.thumbnailUrl).ifPresent(s -> i.url = s);
                        return i.url.startsWith("data") ? i : null;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Cannot get images from web for {}", word, e);
        }
        return new LinkedList<>();
    }

    public Optional<String> retrieveImageToString(String url) {
        log.debug("retrieveImageToString from url: {}", url);
        try {
            String extension = url.substring(url.lastIndexOf('.') + 1);
            byte[] byteArray = restTemplate.getForObject(url, byte[].class);
            return Optional.of("data:image/" + extension + ";base64," + StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(byteArray, false)));
        } catch (Exception e) {
            log.error("Cannot retrieve image from url: {}", url, e);
            return Optional.empty();
        }

    }
}
