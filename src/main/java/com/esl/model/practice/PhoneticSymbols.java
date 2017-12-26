package com.esl.model.practice;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PhoneticSymbols {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(PhoneticSymbol.class);

	// Difficulties for phonetic symbols practice
	public enum Level {
		Rookie(1), Low(3), Medium(6), Full(10);

		final public int weight;

		private Level(int weight) { this.weight = weight; }
	}

	public static Set<String> allPhonics;
	public static Map<Level, Integer> difficultyValueMap;
	public static Map<String, String> googlePhonicsMap;
	
	static {
		allPhonics = new HashSet<String>();
		allPhonics.add("i:");
		allPhonics.add("i");
		allPhonics.add("e");
		allPhonics.add("æ");
		allPhonics.add("ɑ:");
		allPhonics.add("ɔ");
		allPhonics.add("ɔ:");
		allPhonics.add("u");
		allPhonics.add("u:");
		allPhonics.add("ʌ");
		allPhonics.add("ə:");
		allPhonics.add("ə");
		allPhonics.add("ei");
		allPhonics.add("ai");
		allPhonics.add("au");
		allPhonics.add("ɔi");
		allPhonics.add("əu");
		allPhonics.add("iə");
		allPhonics.add("ɛə");
		allPhonics.add("uə");
		allPhonics.add("p");
		allPhonics.add("t");
		allPhonics.add("k");

		allPhonics.add("f");
		allPhonics.add("θ");
		allPhonics.add("s");
		allPhonics.add("ʃ");

		allPhonics.add("h");
		allPhonics.add("tʃ");
		allPhonics.add("w");
		allPhonics.add("b");
		allPhonics.add("d");
		allPhonics.add("g");
		allPhonics.add("m");
		allPhonics.add("n");
		allPhonics.add("ŋ");
		allPhonics.add("l");
		allPhonics.add("v");
		allPhonics.add("ð");
		allPhonics.add("z");
		allPhonics.add("ʒ");
		allPhonics.add("r");

		allPhonics.add("dʒ");

		allPhonics.add("j");
		//allPhonics.add("ˈ");
		//allPhonics.add("͵");

		googlePhonicsMap = new HashMap<String, String>();
		googlePhonicsMap.put("ː", ":");
		googlePhonicsMap.put("ɪ", "i");
		//googlePhonicsMap.put("ː", ":");
		googlePhonicsMap.put("ɒ", "ɔ");
		googlePhonicsMap.put("ʊ", "u");
		googlePhonicsMap.put("ɜ", "ə");
		googlePhonicsMap.put("aʊ", "au");
		googlePhonicsMap.put("eə", "ɛə");
		googlePhonicsMap.put("ˌ", "͵");
		//googlePhonicsMap.put("ˈ", "'");
		googlePhonicsMap.put("ɡ", "g");
		
	}

	// ===================== Functions ======================//

	public static String filterIPA(String IPA) {
		final int maxIPALength = 3;

		StringBuilder sb = new StringBuilder();
		int startPos = 0, endPos = maxIPALength;
		int ipaLength = IPA.length();
		while (startPos < ipaLength) {
			if (endPos > ipaLength) endPos = ipaLength;

			String s = IPA.substring(startPos, endPos);
			if (allPhonics.contains(s)) {
				sb.append(s);
				startPos += endPos - startPos;
				endPos = startPos + maxIPALength;
			} else {
				endPos--;
				if (startPos == endPos) {
					startPos++;
					endPos = startPos + maxIPALength;
				}
			}
		}
		log.debug("filterIPA: input [{}] , output [{}]", IPA, sb.toString());
		return sb.toString();
	}


	public static String convertGoogleIPA(String googleIPA) {
		final int maxGoogleIPALength = 3;

		StringBuilder sb = new StringBuilder();
		int startPos = 0, endPos = maxGoogleIPALength;
		int ipaLength = googleIPA.length();
		while (startPos < ipaLength) {
			if (endPos > ipaLength) endPos = ipaLength;

			String s =googlePhonicsMap.get(googleIPA.substring(startPos, endPos));
			if (s!=null) {
				sb.append(s);
				startPos += endPos - startPos;
				endPos = startPos + maxGoogleIPALength;
			} else {
				endPos--;
				if (startPos == endPos) {
					endPos++;
					sb.append(googleIPA.substring(startPos, endPos));
					startPos++;
					endPos = startPos + maxGoogleIPALength;
				}
			}
		}
		log.debug("convertGoogleIPA: input [{}] , output [{}]", googleIPA, sb.toString());
		return sb.toString();
	}

	// ===================== Getter / setter =============== //

	public Map<Level, Integer> getDifficultyValueMap() {
		return difficultyValueMap;
	}
	public static Set<String> getAllPhonics() {
		return allPhonics;
	}

	public static void setAllPhonics(Set<String> allPhonics) {
		PhoneticSymbols.allPhonics = allPhonics;
	}

	public void setDifficultyValueMap(Map<Level, Integer> difficultyValueMap) {
		PhoneticSymbols.difficultyValueMap = difficultyValueMap;
	}

}
