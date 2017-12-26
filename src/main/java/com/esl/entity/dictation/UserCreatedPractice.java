package com.esl.entity.dictation;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
public abstract class UserCreatedPractice {
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ID", nullable = false)
	protected Long id;

	@Column(name = "TOTAL_RECOMMENDED")
	protected int totalRecommended = 0;

	// ********************** Constructors ********************** //
	public UserCreatedPractice() {
		super();
		totalRecommended = 0;
	}

	// ********************** Accessor Methods ********************** //
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public int getTotalRecommended() { return this.totalRecommended;}
	public void setTotalRecommended(int totalRecommended) { this.totalRecommended = totalRecommended; }

}