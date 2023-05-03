package com.esl.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Deprecated
@Entity
@Table(name = "vocab_image")
public class VocabImage implements Serializable {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	private int id;

	@Column(name = "WORD", nullable = false)
	private String word;

	@Column(name="IMAGE", columnDefinition="MEDIUMTEXT")
	private String base64Image;

    @Column(name="CREATED_DATE")
    private Date createdDate;

	// ********************** Constructors ********************** //
	public VocabImage() {}

	public VocabImage(String word, String base64Image) {
        this.createdDate = new Date();
		this.word = word;
		this.base64Image = base64Image;
	}

	// ********************** Accessor Methods ********************** //
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getWord() { return word; }
	public void setWord(String word) { this.word = word; }

	public String getBase64Image() { return base64Image; }
	public void setBase64Image(String base64Image) { this.base64Image = base64Image; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    // ********************** Common Methods ********************** //

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("word", word)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		VocabImage that = (VocabImage) o;

		return id == that.id;

	}

	@Override
	public int hashCode() {
		return id;
	}
}
