package com.esl.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "phonetic_question")
public class PhoneticQuestion implements Serializable {
	private static Logger logger = LoggerFactory.getLogger(PhoneticQuestion.class);

	public final static String SYMBOL_GIF_PREFIX = "l.yimg.com/mq/i/dic/";
	public final static String SYMBOL_GIF_SUFFIX =  ".gif";
	public final static String PIC_FILE_FOLDER_PATH = "/ESL/images/graphic/word/";
	public static boolean USE_SECEONDARY_PRONOUNCE_LINK = false;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "PHONETICQUESTION_ID", nullable = false)
	private Long id = null;

	@Column(name = "IPA")
	private String IPA;

	@Column(name = "PRONOUNCED_LINK")
	private String pronouncedLink;

	@Column(name = "PRONOUNCED_LINK_BACKUP")
	private String pronouncedLinkBackup;

	@Column(name = "WORD")
	private String word;

	@Column(name = "PIC_FILE_NAME")
	private String picFileName;

	@Column(name = "FREQUENCY")
	private double frequency;

	@Column(name = "RANK")
	private int rank;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name="grade_phoneticquestion",
			joinColumns=@JoinColumn(name="PHONETICQUESTION_ID"),
			inverseJoinColumns=@JoinColumn(name="GRADE_ID"))
	private List<Grade> grades = new ArrayList<Grade>();

	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// not persist in DB
	@Transient
	private boolean IPAUnavailable = false;

	@Transient
	private String[] phonics;

	@Transient
	private String[] picsFullPaths;

	// ********************** Constructors ********************** //
	public PhoneticQuestion() {}

	public PhoneticQuestion(String word, String IPA, String pronouncedLink, String backupLink) {
		this.word = word;
		this.IPA = IPA;
		this.pronouncedLink = pronouncedLink;
		this.pronouncedLinkBackup = backupLink;
	}

	public PhoneticQuestion(String word, String IPA) {
		this.word = word;
		this.IPA = IPA;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public Date getCreatedDate() { return createdDate; }
	private void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	public String getWord() { return word; }
	public void setWord(String word) { this.word = word; }

	public String getIPA() {return IPA; }
	public void setIPA(String IPA) {
		this.IPA = IPA;
		//setPhonics(IPA);
	}

	public String getPronouncedLink() { return pronouncedLink; }
	public void setPronouncedLink(String pronouncedLink) { this.pronouncedLink = pronouncedLink; }
	
	public String getPronouncedLinkBackup() { return pronouncedLinkBackup; }
	public void setPronouncedLinkBackup(String pronouncedLink) { this.pronouncedLinkBackup = pronouncedLink; }

	public String getPicFileName() {return picFileName;}
	public void setPicFileName(String picFileName) {this.picFileName = picFileName;}

	public double getFrequency() {return frequency;}
	public void setFrequency(double frequency) {this.frequency = frequency;	}

	public int getRank() {return rank;}
	public void setRank(int rank) {this.rank = rank;}

	public String getActivePronounceLink() {
		if (USE_SECEONDARY_PRONOUNCE_LINK && StringUtils.isNotBlank(pronouncedLinkBackup))
			return pronouncedLinkBackup;
		else
			return pronouncedLink;
	}
//
//	public String[] getPhonics() {	return phonics;	}
//	public void setPhonics(String[] phonics) {	this.phonics = phonics;	}

	public List<Grade> getGrades() { return grades; }
	public void setGrades(List<Grade> grades) { this.grades = grades; }
	public void addGrades(Grade grade) {
		if (grade == null) throw new IllegalArgumentException("Can't add a null grade.");

		grade.getPhoneticQuestions().add(this);
		this.grades.add(grade);
	}

	public String[] getPicsFullPaths() {
		if (picsFullPaths != null) return picsFullPaths;

		if (StringUtils.isNotBlank(picFileName)) {
			picsFullPaths = new String[]{PIC_FILE_FOLDER_PATH + picFileName};
		}
		return picsFullPaths;
	}

	public void setPicsFullPaths(String[] picsFullPaths) {this.picsFullPaths = picsFullPaths;}

	public void setPicsFullPathsInString(String dummy) {}
	public String getPicsFullPathsInString() {
		StringBuilder sb = new StringBuilder();
		getPicsFullPaths();
		for (String path : picsFullPaths) {
			sb.append(path + "^_^");
		}
		if (sb.length() > 3) sb.delete(sb.length()-3, sb.length());
		return sb.toString();
	}

	public boolean isIPAUnavailable() {	return IPAUnavailable;}
	public void setIPAUnavailable(boolean IPAUnavailable) {this.IPAUnavailable = IPAUnavailable; }

	public String getSuffledWord() {
		Character[] chars = new Character[word.length()];

		for (int i = 0; i < word.length(); i++)
			chars[i] = word.charAt(i);
		List<Character> list = Arrays.asList(chars);

		int maxTrial = word.length() / 2;
		int trial = 0;
		boolean similarWord = false;

		do
		{
			Collections.shuffle(list);
			trial++;
			similarWord = false;

			// check shuffle word is too similar
			int sameChar = 0;
			for (int i=0; i < list.size(); i++) {
				if (list.get(i).equals(word.charAt(i))) {
					sameChar++;
				}
				if (sameChar > maxTrial)  {
					similarWord = true;
					break;
				}
			}
		} while (trial < maxTrial && similarWord == true);

		// reverse word if shuffle word same
		StringBuilder sb = new StringBuilder();
		if (similarWord) {
			sb.append(word);
			sb.reverse();
		} else {
			for (Character c : list) sb.append(c);
		}

		return sb.toString();
	}

	// ********************** Supporting Methods ********************//
//	private void setPhonics(String IPA) {
//		int startPos, endPos;
//		List<String> phonicList = new ArrayList<String>();
//
//		startPos = IPA.indexOf(SYMBOL_GIF_PREFIX);
//		while (startPos >= 0) {
//			endPos = IPA.indexOf(SYMBOL_GIF_SUFFIX, startPos);
//			String symbolId = IPA.substring(startPos + SYMBOL_GIF_PREFIX.length(), endPos);
//			phonicList.add(symbolId);
//			startPos = IPA.indexOf(SYMBOL_GIF_PREFIX, endPos);
//		}
//		phonics = phonicList.toArray(new String[phonicList.size()]);
//		logger.info("setPhonics: phonics[" + Arrays.toString(phonics) + "]");
//	}

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof PhoneticQuestion)) return false;

		final PhoneticQuestion question = (PhoneticQuestion) o;
		return this.id.equals(question.getId());
	}

	@Override
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		return MessageFormat.format("Phonetic Question ({0}): Pic[{1}], IPA[{2}], PronunUrl[{3}], PronunUrl2[{4}]", word, picFileName, IPA, pronouncedLink, pronouncedLinkBackup);
	}

	public boolean ipaEqual(String input) {
		return IPA.replaceAll("ˋ", "").equals(input.replaceAll("ˋ", ""));
	}
	
	public boolean wordEqual(String input) { 
		return word.replaceAll(" ", "").replaceAll("-", "").toLowerCase()
				.equals(input.replaceAll(" ", "").replaceAll("-", "").toLowerCase());
	}

	public boolean enriched() {		
		return StringUtils.isNotBlank(getIPA()) && StringUtils.isNotBlank(getPronouncedLink());
	}

	public String plainWord() {
		return word.replaceAll("[^A-Za-z]+", "");
	}
}
