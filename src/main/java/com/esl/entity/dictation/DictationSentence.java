//package com.esl.entity.dictation;
//
//import jakarta.persistence.*;
//import java.io.Serializable;
//import java.util.Date;
//
//import static jakarta.persistence.GenerationType.IDENTITY;
//
//@Entity
//@Table(name = "dictation_sentence")
//public class DictationSentence implements Serializable {
//	private static final long serialVersionUID = 2676912467971615861L;
//
//	@Id
//	@GeneratedValue(strategy = IDENTITY)
//	@Column(name = "ID", nullable = false)
//	private Long id;
//
//	@Column(name = "CONTENT", length = 1000)
//	private String content;
//
//	@Column(name = "ORDERING")
//	private int ordering;
//
//	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE})
//	@JoinColumn(name="DICTATION_ID")
//	private Dictation dictation;
//
//	@Temporal(TemporalType.TIMESTAMP)
//	@Column(name = "CREATED_DATE")
//	private Date createdDate = new Date();
//
//	// ********************** Constructors ********************** //
//	public DictationSentence() {}
//
//	public DictationSentence(String content, int ordering, Dictation dictation) {
//		this.content = content;
//		this.ordering = ordering;
//		this.dictation = dictation;
//	}
//
//	// ********************** Accessor Methods ********************** //
//	public Long getId() { return id; }
//	public void setId(Long id) { this.id = id; }
//
//	public String getContent() { return content;}
//	public void setContent(String content) {this.content = content;}
//
//	public int getOrdering() {return ordering;}
//	public void setOrdering(int ordering) {this.ordering = ordering;}
//
//	public Dictation getDictation() {return dictation;}
//	public void setDictation(Dictation dictation) {this.dictation = dictation;}
//
//	public Date getCreatedDate() {return createdDate;}
//	public void setCreatedDate(Date createdDate) {this.createdDate = createdDate;}
//
//	// ********************** Common Methods ********************** //
//
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((id == null) ? 0 : id.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		DictationSentence other = (DictationSentence) obj;
//		if (id == null) {
//			if (other.id != null)
//				return false;
//		} else if (!id.equals(other.id))
//			return false;
//		return true;
//	}
//
//	@Override
//	public String toString() {
//		return String
//				.format("DictationSentence(%s) [content=%s, ordering=%s, dictationId=%s]",
//						id, content, ordering, dictation==null?"null":dictation.getId());
//	}
//}
