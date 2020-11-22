package com.esl.controller.admin;

import com.esl.dao.PendingProcessWordRepository;
import com.esl.dao.VocabImageDAO;
import com.esl.entity.VocabImage;
import com.esl.entity.rest.WebItem;
import com.esl.service.PhoneticQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/admin/vocab/image")
public class VocabImageAdminController {
    private static Logger log = LoggerFactory.getLogger(VocabImageAdminController.class);

	@Resource private PhoneticQuestionService phoneticQuestionService;
	@Resource private VocabImageDAO vocabImageDao;
	@Resource private PendingProcessWordRepository pendingProcessWordRepository;

	@RequestMapping(value = "/get-from-web/{word}")
	public ResponseEntity<List<WebItem>> getImageFromWeb(@PathVariable String word,
														 @RequestParam(required = false, defaultValue = "1") int start) {
		log.debug("getImageFromWeb for {}, start from {}", word, start);
		return ResponseEntity.ok(phoneticQuestionService.getImagesFromWeb(word, start));
	}

	@PostMapping(value = "/save/{word}")
	public VocabImage saveImage(@PathVariable String word, @RequestBody String base64Image) {
		return vocabImageDao.persist(new VocabImage(word, base64Image));
	}

	@RequestMapping(value = "/pending-words/get")
	public List<String> getAllPendingWords() {
		List<String> list = new ArrayList<>();
		pendingProcessWordRepository.findAll().forEach(i -> list.add(i.getWord()));
		return list;
	}

	@DeleteMapping(value = "/pending-words/delete/{word}")
	public void deletePendingWord(@PathVariable String word) {
		log.debug("deletePendingWord: {}", word);
		pendingProcessWordRepository.deleteById(word);
	}
 }
