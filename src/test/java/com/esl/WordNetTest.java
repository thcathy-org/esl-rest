package com.esl;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WordNetTest {

    @Test
    public void dictionary() throws JWNLException {
        Dictionary d = Dictionary.getDefaultResourceInstance();

        String lemma = "personally";
        List<IndexWord> indexWords = POS.getAllPOS().stream()
                .map(pos -> getIndexWord(d, pos, lemma))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        System.out.println(indexWords);
    }

    IndexWord getIndexWord(Dictionary d, POS pos, String lemma) {
        try {
            return d.getIndexWord(pos, lemma);
        } catch (JWNLException e) {
            return null;
        }
    }
}
