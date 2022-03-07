package com.esl.service;

import com.esl.dao.IVocabImageDAO;
import com.esl.dao.PhoneticQuestionDAO;
import com.esl.entity.VocabImage;
import com.esl.entity.rest.DictionaryResult;
import com.esl.entity.rest.WebItem;
import com.esl.model.PhoneticQuestion;
import com.esl.service.rest.WebParserRestService;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class PhoneticQuestionService {
    private static Logger log = LoggerFactory.getLogger(PhoneticQuestionService.class);

    @Value("${NAImage.data}")
    public String NAImage;

    @Resource private IVocabImageDAO vocabImageDao;
    @Resource private PhoneticQuestionDAO phoneticQuestionDAO;
    @Resource private WebParserRestService webService;
    @Resource private RestTemplate restTemplate;

    public Optional<PhoneticQuestion> getQuestionFromDBWithImage(String word, boolean showImage, boolean includeBase64Image) {
        Optional<PhoneticQuestion> question = Optional.ofNullable(phoneticQuestionDAO.getPhoneticQuestionByWord(word));
        question.ifPresent(q -> {
            if (showImage && includeBase64Image)
                enrichVocabImage(q);
            else
                q.setPicsFullPaths(new String[] {includeBase64Image ? NAImage : ""});
        });
        return question;
    }

    public PhoneticQuestion buildQuestionByWebAPI(String word, boolean showImage, boolean includeBase64Image) {
        log.info("buildQuestionByWebAPI for word: {}, showImage={}", word, showImage);
        PhoneticQuestion question = new PhoneticQuestion();
        question.setWord(word);

        CompletableFuture<WebItem[]> imagesResult;
        if (showImage)
            imagesResult = webService.searchGoogleImage(word + " clipart", 1);
        else
            imagesResult = CompletableFuture.completedFuture(new WebItem[0]);

        CompletableFuture<Optional<DictionaryResult>> dictionaryResult = webService.queryDictionary(word);

        fillQuestionByDictionaryResult(question, dictionaryResult.join());
        setPicsFullPaths(question, imagesResult.join(), includeBase64Image);

        return question;
    }

    private void setPicsFullPaths(PhoneticQuestion question, WebItem[] items, boolean includeBase64Image) {
        if (items.length < 1) {
            question.setPicsFullPaths(new String[] {includeBase64Image ? NAImage : ""});
            return;
        }

        List<String> images = Arrays.stream(items)
                .map(i -> i.thumbnailUrl)
                .limit(5)
                .collect(Collectors.toList());

        log.debug("images: {}", images);

        question.setPicsFullPaths(images.toArray(new String[images.size()]));
    }

    private void fillQuestionByDictionaryResult(PhoneticQuestion question, Optional<DictionaryResult> r) {
        if (!r.isPresent()) {
            question.setIPAUnavailable(true);
        } else {
            DictionaryResult result = r.get();
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
        return Collections.EMPTY_LIST;
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

    @Transactional(readOnly = true)
    public void enrichVocabImage(PhoneticQuestion question) {
        log.info("enrichVocabImage: {}", question.getWord());

        List<String> images = vocabImageDao.listByWord(question.getWord()).stream()
                .map(VocabImage::getBase64Image)
                .collect(Collectors.toList());

        if (images.size() > 0) {
            Collections.shuffle(images);
            question.setPicsFullPaths(images.toArray(new String[images.size()]));
        } else {
            question.setPicsFullPaths(new String[] {NAImage});
        }
    }
}
