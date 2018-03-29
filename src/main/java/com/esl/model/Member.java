package com.esl.model;

import com.esl.model.group.MemberGroup;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "member")
public class Member implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MEMBER_ID", nullable = false)
	private Long id;

	@Column(name = "USER_ID", unique = true)
	private String userId;

	@Embedded
	private Name name = new Name();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "BIRTHDAY")
	private Date birthday;
	
	@Pattern(regexp="[^'&lt;&gt;&quot;]{10,}|^$", message="{invalidAddress}")
	@Column(name = "ADDRESS")
	private String address;
	
	@Size(max=30, message="{incorrectSize}")
	@Pattern(regexp="[^'&lt;&gt;&quot;]*", message="{containInvalidChar}")
	@Column(name = "PHONE_NUMBER")
	private String phoneNumber;
	
	@Size(min=6, max=20, message="{incorrectSize}")
	@Column(name = "PIN")
	private String PIN;

	@Column(name = "SCHOOL")
	private String school;

	@Column(name = "TOTAL_WORD_LEARNT")
	private int totalWordLearnt = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ACTIVATED_DATE")
	private Date activatedDate;

	//@Column(name = "ACCEPTED_TERM")
	@Transient
	private boolean acceptedTerm = false;
	
	@Pattern(regexp="^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message="{invalidEmail}")
	@Column(name = "EMAIL_ADDRESS")
	private String emailAddress;

	@ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@JoinColumn(name = "GRADE_ID")
	private Grade grade;

	@Column(name = "LOGINED_SESSION_ID")
	private String loginedSessionId;

	@Column(name = "IMAGE_PATH")
	private String imagePath;

	@Transient
	private Collection<Receipt> receipts = new ArrayList<Receipt>();

	@Transient
	private List phoneticPractices = new ArrayList();

	@Transient
	private List practiceResults = new ArrayList();

	@Transient
	private List<MemberGroup> groups = new ArrayList<MemberGroup>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE")
	private Date createdDate = new Date();

	// ********************** Constructors ********************** //
	public Member() {}

	public Member(String userId, Name name, Date birthday, String address, String phoneNumber, String PIN, String school, Date activatedDate, boolean acceptedTerm, String emailAddress) {
		this.userId = userId;
		this.name = name;
		this.birthday = birthday;
		this.address = address;
		this.phoneNumber = phoneNumber;
		this.PIN = PIN;
		this.school = school;
		this.activatedDate = activatedDate;
		this.emailAddress = emailAddress;
	}

	public Member(String userId, Name name) {
		this.userId = userId;
		this.name = name;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() { return id; }
	private void setId(Long id) { this.id = id; }

	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }

	public Name getName() { return name; }
	public void setName(Name name) { this.name = name; }
	public void setName(String lastName, String firstName) {
		Name aName = new Name(lastName, firstName);
		this.name = aName;
	}

	public Date getBirthday() { return birthday; }
	public void setBirthday(Date birthday) { this.birthday = birthday; }
	public int getAge() {
		if (birthday == null) return -1;

		Calendar dateOfBirth = Calendar.getInstance();
		dateOfBirth.setTime(birthday);
		Calendar today = Calendar.getInstance();

		int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
		// Add the tentative age to the date of birth to get this year's birthday
		dateOfBirth.add(Calendar.YEAR, age);
		// If this year's birthday has not happened yet, subtract one from age
		if (today.before(dateOfBirth)) age--;
		return age;
	}

	public Grade getGrade() {return grade;}
	public void setGrade(Grade grade) {this.grade = grade;}

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = StringUtils.trimToEmpty(address); }

	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = StringUtils.trimToEmpty(phoneNumber); }

	public String getPIN() { return PIN; }
	public void setPIN(String PIN) { this.PIN = PIN; }

	public String getSchool() { return school; }
	public void setSchool(String school) { this.school = StringUtils.trimToEmpty(school); }

	public Date getActivatedDate() { return activatedDate; }
	public void setActivatedDate(Date activatedDate) { this.activatedDate = activatedDate; }

	public boolean isAcceptedTerm() { return acceptedTerm; }
	public void setAcceptedTerm(boolean acceptedTerm) { this.acceptedTerm = acceptedTerm; }

	public String getEmailAddress() { return emailAddress; }
	public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

	public int getTotalWordLearnt() {return totalWordLearnt;}
	public void setTotalWordLearnt(int totalWordLearnt) {this.totalWordLearnt = totalWordLearnt;}

	public Date getCreatedDate() { return createdDate; }
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

	public Collection getReceipts() { return receipts; }
	public void setReceipts(Collection receipts) { this.receipts = receipts; }
	public void addReceipt(Receipt receipt) {
		if (receipt == null) throw new IllegalArgumentException("Can't add a null receipt.");

		receipt.setOwner(this);				// bidirectional mapping
		this.receipts.add(receipt);
	}

	public List getPhoneticPractices() { return phoneticPractices; }
	public void setPhoneticPractices(List phoneticPractices) { this.phoneticPractices = phoneticPractices; }
	public void addPhoneticPractices(PhoneticPracticeHistory phoneticPractice) {
		if (phoneticPractice == null) throw new IllegalArgumentException("Can't add a null phonetic practice.");
		phoneticPractice.setMember(this);
		this.phoneticPractices.add(phoneticPractice);
	}


	public List<MemberGroup> getGroups() {return groups;}
	public void setGroups(List<MemberGroup> groups) {this.groups = groups;}
	public void addGroup(MemberGroup group) {
		if (group == null) throw new IllegalArgumentException("Can't add a null group.");
		group.addMember(this);
		this.groups.add(group);
	}

	public List getPracticeResults() { return practiceResults; }
	public void setPracticeResults(List practiceResults) { this.practiceResults = practiceResults; }

	public String getLoginedSessionId() {return loginedSessionId;}
	public void setLoginedSessionId(String loginedSessionId) {this.loginedSessionId = loginedSessionId;}

	public String getImagePath() {return imagePath;}
	public void setImagePath(String imagePath) {this.imagePath = imagePath;}

	public String getDisplayName() {
		if (name != null && name.getFullName() != null)
			return name.getFullName();
		else
			return emailAddress;
	}

	// ********************** Common Methods ********************** //
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof Member)) return false;

		final Member member = (Member) o;
		return this.id.equals(member.getId());
	}

	@Override
	public int hashCode()
	{
		return id==null ? System.identityHashCode(this) : id.hashCode();
	}

	@Override
	public String toString() {
		return  "Member ('" + getId() + "'), " +
		"User ID: '" + getUserId() + "' " +
		"Name: '" + getName() + "' ";
	}

	public void copy(Member member) {
		this.id = member.id;
		this.userId = member.userId;
		this.name = member.name;
		this.birthday = member.birthday;
		this.address = member.address;
		this.phoneNumber = member.phoneNumber;
		this.PIN = member.PIN;
		this.school = member.school;
		this.activatedDate = member.activatedDate;
		this.acceptedTerm = member.acceptedTerm;
		this.emailAddress = member.emailAddress;
		this.grade = member.grade;
		this.receipts = member.receipts;
		this.phoneticPractices = member.phoneticPractices;
		this.createdDate = member.createdDate;
	}


}
