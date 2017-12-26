package com.esl.util.practice;

import com.esl.model.PhoneticQuestion;
import com.esl.util.web.HttpURLConnectionBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.util.List;

import static org.apache.commons.lang3.CharEncoding.UTF_8;

@Service("phoneticQuestionUtil")
public class PhoneticQuestionUtil {	
	private static Logger logger = LoggerFactory.getLogger(PhoneticQuestionUtil.class);
	
	public PhoneticQuestionUtil() {}

	public void findIPA(PhoneticQuestion question) {
		// should not be use anymore
//		DictionaryParser parser = new CambridgeDictionaryParser(question.getWord());
//		if (parser.parse()) {
//			question.setIPA(parser.getIpa());
//			question.setPronouncedLink(parser.getAudioLink());
//			logger.debug("Found IPA [{}] and PronounceLink [{}]", question.getIPA(), question.getPronouncedLink());
//		}
	}

	public class FindIPAAndPronoun implements Runnable {
		List<PhoneticQuestion> questions;
		PhoneticQuestion question;

		public FindIPAAndPronoun(List<PhoneticQuestion> questions,
                                 PhoneticQuestion question, String rootPath, String contextPath) {
			super();
			this.questions = questions;
			this.question = question;
		}

		public void run() {
			try{
				if (!question.enriched()) {
					logger.info("FindIPAAndPronoun.run: Do not have IPA, Start getting IPA");
					findIPA(question);
				}
				if (!question.enriched()) {
					logger.info("FindIPAAndPronoun.run: Do not have IPA, Start get text 2 speech");
					question.setIPAUnavailable(true);
					//generatePronounceLink(question, rootPath, contextPath);
					question.setPronouncedLink(getText2SpeechPronounceLink(question.getWord()));
				}
				synchronized (this) {
					logger.info("FindIPAAndPronoun.run: Add question to list: " + question);
					if (question.getPronouncedLink() != null) questions.add(question);
				}
			} catch (Exception e) {
				logger.error("FindIPAAndPronoun.run: " + e, e);
			}
		}
	}
	
	public String getText2SpeechPronounceLink(String word) {
		String text2SpeechURL = "http://www.text2speech.org/";
		String paramUrl = "text=" + word.replace(" ", "+") + "&voice=slt&speed=1&outname=speech";
		
		Document doc = null;		
		try {						
			HttpURLConnection connection = new HttpURLConnectionBuilder().setURL(text2SpeechURL).setEncodedParams(paramUrl).createConnection();
			doc = Jsoup.parse(connection.getInputStream(), UTF_8, connection.getURL().getPath());			
			
			String mp3PageUrl = exactForwardUrl(doc);
			logger.debug("Extracted url: {}", mp3PageUrl);
			
			connection = new HttpURLConnectionBuilder().setURL(text2SpeechURL + mp3PageUrl).createConnection();
			Thread.sleep(200);
			doc = Jsoup.parse(connection.getInputStream(), UTF_8, connection.getURL().getPath());
			logger.debug("Mp3 page: {}", doc.html());
		} catch (Exception e) {
			logger.info("Cannot get text 2 speech link", e);
		}
		return text2SpeechURL + doc.select("a[href$=mp3]").get(0).attr("href");
	}

	private String exactForwardUrl(Document doc) {
		for (Element c : doc.select("script[type=text/javascript]")) {
			String html = c.childNode(0).toString();

			if (html.contains("/FW/result.php")) {
				int startPos = html.indexOf("/FW/result.php");
				return html.substring(startPos+1, html.indexOf("'", startPos));
			}
		}
		throw new RuntimeException("Cannot find forward url from: " + doc.ownText());
	}

}
