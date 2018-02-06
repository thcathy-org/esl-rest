package com.esl.entity.dictation;

import com.esl.model.Member;
import com.esl.model.group.MemberGroup;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "dictation")
public class Dictation extends UserCreatedPractice {
	public enum AgeGroup {
		@Column(name="AGE_GROUP")
		@Enumerated(EnumType.STRING)
		Age3to6("3-6",3,6), Age7to9("7-9",7,9), Age10to12("10-12",10,12), Age13to15("13-15",13,15), Age16to18("16-18",16,18), AgeOver18(">18",19,100), AgeAny("Any",-1,-1);

		private final String display;
		private final int minAge;
		private final int maxAge;
		private AgeGroup(String display, int minAge, int maxAge) {
			this.display = display;
			this.minAge = minAge;
			this.maxAge = maxAge;
		}
		public int getMinAge() { return minAge; }
		public int getMaxAge() { return maxAge; }
		@Override
		public String toString() {	return display;	}
		public String getDisplay() { return display; }
		public static AgeGroup getAgeGroup(int age) {
			for (AgeGroup a : AgeGroup.values()) {
				if (age >= a.minAge && age <=a.maxAge) return a;
			}
			return AgeAny;
		}
	}

	public enum DictationType {
		Vocab, Article;
	}

	public static final int SHORT_TITLE_LENGHT = 30;
	public static final String SEPARATOR = ",";

	@Column(name = "TITLE")
	private String title;

	@Column(name = "SUITABLE_MIN_AGE")
	private int suitableMinAge;

	@Column(name = "SUITABLE_MAX_AGE")
	private int suitableMaxAge;

	@Column(name = "RATING")
	private double rating;

	@Column(name = "TOTAL_RATED")
	private int totalRated;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "TAGS")
	private String tags;

	@Column(name = "IS_PUBLIC")
	private boolean isPublicAccess;

	@Column(name = "TOTAL_ATTEMPT")
	private int totalAttempt;

	@Column(name = "PASSWORD")
	private String password;

	@Column(name = "NOT_ALLOW_IPA")
	private boolean notAllowIPA;

	@Column(name = "NOT_ALLOW_RAND_CHAR")
	private boolean notAllowRandomCharacters;

	@Column(name = "SHOW_IMAGE")
	private boolean showImage;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_PRACTICE_DATE")
	private Date lastPracticeDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_MODIFY_DATE")
	private Date lastModifyDate;

	@Column(name="ARTICLE")
	private String article;

	@ManyToOne()
	@JoinColumn(name="MEMBER_ID")
	private Member creator;

	@OneToMany(mappedBy="dictation", cascade={CascadeType.ALL}, fetch= FetchType.EAGER)
	private List<Vocab> vocabs;

	//@ManyToMany()
	//@JoinTable(name="dictation_membergroup", joinColumns=@JoinColumn(name="DICTATION_ID"), inverseJoinColumns=@JoinColumn(name="MEMBERGROUP_ID"))
	@Transient
	private List<MemberGroup> accessibleGroups;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate;

	// ********************** Constructors ********************** //
	public Dictation() {
		super();
		suitableMinAge = -1;
		rating = 2.5;
		createdDate = new Date();
		isPublicAccess = true;
		totalAttempt = 0;
		createdDate = new Date();
		lastModifyDate = new Date();
		vocabs = new ArrayList<Vocab>();
		accessibleGroups = new ArrayList<MemberGroup>();
		article = "";
		showImage = true;
	}

	public Dictation(String title) {
		this();
		this.title = title;
	}

	// ********************** Accessor Methods ********************** //
	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;	}

	public String getShortTitle() {
		if (title.length() > SHORT_TITLE_LENGHT) return title.substring(0, SHORT_TITLE_LENGHT);
		else return title;
	}

	public int getSuitableMinAge() {return suitableMinAge;}
	public void setSuitableMinAge(int suitableMinAge) {this.suitableMinAge = suitableMinAge;}

	public int getSuitableMaxAge() {return suitableMaxAge;}
	public void setSuitableMaxAge(int suitableMaxAge) {this.suitableMaxAge = suitableMaxAge;}

	public List<Integer> getSuitableAgeGroups() {
		List<Integer> ags = new ArrayList<Integer>();

		// return any if minAge is not set
		if (suitableMinAge < 1) { ags.add(AgeGroup.AgeAny.ordinal()); return ags; }

		for (AgeGroup a : AgeGroup.values()) {
			if (a.minAge >= suitableMinAge && a.minAge <= suitableMaxAge) {
				ags.add(a.ordinal());
				continue;
			}
			if (a.maxAge >= suitableMinAge && a.maxAge <= suitableMaxAge) {
				ags.add(a.ordinal());
				continue;
			}
		}
		return ags;
	}
	public void setSuitableAgeGroups(List<String> suitableAgeGroups) {
		if (suitableAgeGroups != null && suitableAgeGroups.size() > 0) {
			suitableMinAge = AgeGroup.values()[Integer.parseInt(suitableAgeGroups.get(0))].minAge;
			suitableMaxAge = AgeGroup.values()[Integer.parseInt(suitableAgeGroups.get(0))].maxAge;
		}
		for (String i : suitableAgeGroups) {
			AgeGroup a = AgeGroup.values()[Integer.parseInt(i)];
			if (a == AgeGroup.AgeAny) {
				suitableMinAge = suitableMaxAge = -1;
				return;
			}
			if (suitableMinAge > a.minAge) suitableMinAge = a.minAge;
			if (suitableMaxAge < a.maxAge) suitableMaxAge = a.maxAge;
		}
	}
	public String getSuitableAge() {
		if (suitableMinAge < 1) return AgeGroup.AgeAny.toString();
		return suitableMinAge + " - " + suitableMaxAge;
	}

	public double getRating() {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(rating));
	}
	public void setRating(double rating) {this.rating = rating;}

	public String getDescription() {return description;}
	public void setDescription(String description) {this.description = description;}

	public String getTags() {return tags;}
	public void setTags(String tags) {this.tags = tags.toLowerCase();}

	public boolean isPublicAccess() {return isPublicAccess;}
	public void setPublicAccess(boolean isisPublicAccess) {this.isPublicAccess = isPublicAccess;}

	public int getTotalAttempt() {return totalAttempt;}
	public void setTotalAttempt(int totalAttempt) {this.totalAttempt = totalAttempt;}

	public Date getLastPracticeDate() {return lastPracticeDate;}
	public void setLastPracticeDate(Date lastPracticeDate) {this.lastPracticeDate = lastPracticeDate;}

	public String getPassword() {return password;}
	public void setPassword(String password) {this.password = password;}
	public boolean isRequirePassword() { return (password != null && !password.equals("")); }


	public boolean isNotAllowIPA() {return notAllowIPA;}
	public void setNotAllowIPA(boolean notAllowIPA) {this.notAllowIPA = notAllowIPA;}

	public boolean isNotAllowRandomCharacters() {return notAllowRandomCharacters;}
	public void setNotAllowRandomCharacters(boolean notAllowRandomCharacters) {this.notAllowRandomCharacters = notAllowRandomCharacters;}

	public Date getLastModifyDate() {return lastModifyDate;}
	public void setLastModifyDate(Date lastModifyDate) {this.lastModifyDate = lastModifyDate;}

	public int getTotalRated() {return totalRated;}
	public void setTotalRated(int totalRated) {this.totalRated = totalRated;}

	public Member getCreator() {return creator;}
	public void setCreator(Member creator) {this.creator = creator;}

	public String getArticle() {return article;}
	public void setArticle(String article) {this.article = article;}

	public boolean isShowImage() {return showImage;	}
	public void setShowImage(boolean showImage) {this.showImage = showImage;}

	public List<Vocab> getVocabs() {return vocabs;}
	public void setVocabs(List<Vocab> vocabs) {	this.vocabs = vocabs;}
	public void addVocab(Vocab vocab) {
		vocabs.add(vocab);
		vocab.setDictation(this);
	}
	public int getVocabsSize() { return vocabs.size();}
	public String getVocabsString() {
		if (vocabs == null || vocabs.size() < 1) return "";
		StringBuilder sb = new StringBuilder();
		for (Vocab v : vocabs) {
			sb.append(v.getWord());	sb.append(SEPARATOR);
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}

	public List<MemberGroup> getAccessibleGroups() {return accessibleGroups;}
	public void setAccessibleGroups(List<MemberGroup> accessibleGroups) {this.accessibleGroups = accessibleGroups;}
	public void addAccessibleGroup(MemberGroup group) {
		accessibleGroups.add(group);
	}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof Dictation)) return false;

		final Dictation h = (Dictation) o;
		return this.id.equals(h.getId());
	}

	@Override
	public int hashCode() {
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Dictation ("); sb.append(getId()); sb.append(")");
		return  sb.toString();
	}

	public DictationType getType() {
		if (StringUtils.isBlank(article)) {
			return DictationType.Vocab;
		} else {
			return DictationType.Article;
		}
	}

	public Map<Long, Vocab> vocabToMap() {
		return vocabs.stream().collect(
				Collectors.toMap(v -> v.getId(), v -> v)
		);
	}

}
