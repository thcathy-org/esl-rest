package com.esl.enumeration;


public enum Medal {
	Gold(1), Silver(2), Bronze(3);

	public final int weight;

	public static Medal getByWeight(int weight) {
		for (Medal m : Medal.values()) {
			if (m.weight == weight) return m;
		}
		return null;
	}

	private Medal(int weight) {
		this.weight = weight;
	}
}