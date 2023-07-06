package com.esl.service;

import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationSearchCriteria;
import com.esl.entity.rest.SearchDictationRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.esl.entity.dictation.DictationSearchCriteria.*;

@Service
public class SearchDictationService {
	private static Logger log = LoggerFactory.getLogger(SearchDictationService.class);

	@Autowired private DictationDAO dictationDAO;

	public SearchDictationService() {}

	public List<Dictation> searchDictation(SearchDictationRequest request, int maxResult) {
		log.info("search dictation: {}", ReflectionToStringBuilder.toString(request));

		if (StringUtils.isNumeric(request.keyword))
			return getDictationByIdToList(Long.valueOf(request.keyword));

		Map<DictationSearchCriteria, Object> searchCriteria = new HashMap<>();
		if (StringUtils.isNotBlank(request.keyword)) {
			if (request.searchTitle) searchCriteria.put(Title, request.keyword);
			if (request.searchDescription) searchCriteria.put(Description, request.keyword);
		}
		if (request.minDate != null) searchCriteria.put(MinDate, request.minDate);
		if (request.maxDate != null) searchCriteria.put(MaxDate, request.maxDate);
		if (StringUtils.isNotBlank(request.creator)) searchCriteria.put(CreatorName, request.creator);
		if (request.suitableStudent != null) searchCriteria.put(SuitableStudent, request.suitableStudent);
		try {
			if (Dictation.DictationType.valueOf(request.type) != null) searchCriteria.put(Type, Dictation.DictationType.valueOf(request.type));
		} catch (IllegalArgumentException | NullPointerException e) {} // ignore

		List<Dictation> result = dictationDAO.searchDictation(searchCriteria, maxResult);

		log.info("dictation found: {}", result.size());
		return result;
	}

	private List<Dictation> getDictationByIdToList(Long id) {
		Dictation dictation = dictationDAO.get(id);
		if (dictation != null) {
			return Collections.singletonList(dictation);
		} else {
			return Collections.emptyList();
		}
	}


}
