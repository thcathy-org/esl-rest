package com.esl.service.practice

import com.esl.service.ArticleDictationService
import spock.lang.Specification
import spock.lang.Unroll

class ArticleDictationServiceSpec extends Specification {
    ArticleDictationService service = new ArticleDictationService()

    def setup() {
        service.maxSentenceLength = 40
    }

    @Unroll
    def "compare sentence: input=#input"(String input, List<Boolean> isCorrect) {
        String question = '''Jane Bailey described Miss Tweddle-Taylor, 51, as a "well-loved member of staff" and "wonderful friend and colleague".'''

        expect:
        service.compare(question, input).isCorrect == isCorrect

        where:
        input | isCorrect
        "Jane Bailey described Miss Tweddle-Taylor, 51, as a well-loved member of staff and wonderful friend and colleague." | [true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true]
        "Bailey described Miss Tweddle-Taylor, 51, as a well-loved member of staff and wonderful friend and colleague." | [false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true]
        "Tweddle-Taylor member" | [false, false, false, false, true, true, false, false, false, true, false, false, false, false, false, false, false]
        "abc Tweddle-Taylor member" | [false, false, false, false, true, true, false, false, false, true, false, false, false, false, false, false, false]
        "abc bailey described as a well lived member of staff and wonderful friend and colleague" | [false, true, true, false, false, true, true, true, false, true, true, true, true, true, true, true, true]
        "Jane Bailey described Miss Tweddle-Taylor, five one, as a well-loved member of staff and wonderful friend and colleague." | [true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true]
    }

    def "separate sentence contain quote after full stop"() {
        String article = '''Victim Jane Tweddle-Taylor, a receptionist at South Shore Academy School in Blackpool, was a "bubbly, kind, welcoming, funny, generous" colleague, the school's principal has said. Jane Bailey described Miss Tweddle-Taylor, 51, as a "well-loved member of staff" and "wonderful friend and colleague". She added: "Our thoughts are with her friends and family at this terrible time."'''

        when: "divide article to sentences"
        List<String> sentences = service.divideArticleToSentences(article)
        sentences.each {println it}

        then: "the sentences are separated by newline"
        sentences[0] == '''Victim Jane Tweddle-Taylor, a receptionist'''
        sentences[1] == '''at South Shore Academy School in Blackpool'''
        sentences[2] == ''', was a "bubbly, kind, welcoming'''
        sentences[3] == ''', funny, generous" colleague, the school's'''
        sentences[4] == '''principal has said.'''
        sentences[5] == '''Jane Bailey described Miss Tweddle-Taylor'''
        sentences[6] == ''', 51, as a "well-loved member of staff" and'''
        sentences[7] == '''"wonderful friend and colleague".'''
        sentences[8] == '''She added: "Our thoughts are with her friends'''
        sentences[9] == '''and family at this terrible time.".'''
    }

    def "separate sentence which is too long by comma"() {
        String article = '''A zoo-keeper who died after a tiger entered an enclosure at a wildlife park in Cambridgeshire has been named as 33-year-old Rosa King.
                            The death happened at Hamerton Zoo Park, near Huntingdon, at about 11:15 BST on Monday.
                            Friend Garry Chisholm, a wildlife photographer in his spare time, said she was the "focal point" and "shining light" of the wildlife park.
                            The zoo said it was a freak accident, and police said it was not suspicious.
                            Mr Chisholm, 59, of Irchester, Northamptonshire, said the wildlife park revolved around the zoo-keeper.'''

        when: "start a dictation practice"
        List<String> sentences = service.divideArticleToSentences(article)
        sentences.each {println it}

        then: "the sentences are separated by newline"
        sentences.size() == 15
        sentences[0] == '''A zoo-keeper who died after a tiger entered'''
        sentences[1] == '''an enclosure at a wildlife park in Cambridgeshire'''
        sentences[2] == '''has been named as 33-year-old Rosa King.'''
        sentences[3] == '''The death happened at Hamerton Zoo Park'''
        sentences[4] == ''', near Huntingdon, at about 11:15 BST on'''
        sentences[5] == '''Monday.'''
    }

}
