package com.esl.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esl.dao.dictation.DictationDAO;
import com.esl.entity.dictation.Dictation;
import com.esl.entity.dictation.DictationSearchCriteria;
import com.esl.entity.rest.SearchDictationRequest;

import static com.esl.entity.dictation.DictationSearchCriteria.CreatorName;
import static com.esl.entity.dictation.DictationSearchCriteria.Description;
import static com.esl.entity.dictation.DictationSearchCriteria.MaxDate;
import static com.esl.entity.dictation.DictationSearchCriteria.MinDate;
import static com.esl.entity.dictation.DictationSearchCriteria.SuitableStudent;
import static com.esl.entity.dictation.DictationSearchCriteria.Title;

@Service
public class SearchDictationService {
	private static Logger log = LoggerFactory.getLogger(SearchDictationService.class);

	@Autowired private DictationDAO dictationDAO;

	public SearchDictationService() {}

	public List<Dictation> searchDictation(SearchDictationRequest request, int maxResult) {
		log.info("search dictation: ", ReflectionToStringBuilder.toString(request));

		Map<DictationSearchCriteria, Object> searchCriteria = new HashMap<>();
		if (StringUtils.isNotBlank(request.keyword)) {
			if (request.searchTitle) searchCriteria.put(Title, request.keyword);
			if (request.searchDescription) searchCriteria.put(Description, request.keyword);
		}
		if (request.minDate != null) searchCriteria.put(MinDate, request.minDate);
		if (request.maxDate != null) searchCriteria.put(MaxDate, request.maxDate);
		if (StringUtils.isNotBlank(request.creator)) searchCriteria.put(CreatorName, request.creator);
		if (request.suitableStudent != null) searchCriteria.put(SuitableStudent, request.suitableStudent);

		List<Dictation> result = dictationDAO.searchDictation(searchCriteria, maxResult);

		log.info("dictation found: {}", result.size());
		return result;
	}
}
