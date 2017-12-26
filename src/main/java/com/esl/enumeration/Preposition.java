package com.esl.enumeration;


public enum Preposition {
	onTopOf("on top of"),
	inFrontOf("in front of"),
	insteadOf("instead of"),
	outOf("out of"),
	about("about"),
	above("above"),
	across("across"),
	after("after"),
	against("against"),
	along("along"),
	among("among"),
	around("around"),
	at("at"),
	before("before"),
	behind("behind"),
	below("below"),
	beneath("beneath"),
	beside("beside"),
	between("between"),
	by("by"),
	down("down"),
	during("during"),
	except("except"),
	For("for"),
	from("from"),
	inside("inside"),
	in("in"),
	into("into"),
	like("like"),
	near("near"),
	off("off"),
	Of("of"),
	onto("onto"),
	on("on"),
	outside("outside"),
	over("over"),
	past("past"),
	since("since"),
	through("through"),
	to("to"),
	toward("toward"),
	under("under"),
	underneath("underneath"),
	until("until"),
	up("up"),
	upon("upon"),
	with("with"),
	within("within"),
	without("without");

	private final String word;
	static public String PREPOSITION_REGEX;

	private Preposition(String word) {
		this.word = word;
	}

	@Override
	public String toString() { return word; }

	static {
		StringBuffer sb = new StringBuffer();
		for (Preposition p : Preposition.values()) {
			sb.append("\\b").append(p.word).append("\\b").append("|");
		}
		sb.deleteCharAt(sb.length()-1);
		PREPOSITION_REGEX = sb.toString();
	}
}