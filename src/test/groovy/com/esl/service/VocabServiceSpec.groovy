package com.esl.service

import com.esl.enumeration.VocabDifficulty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Unroll

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class VocabServiceSpec extends Specification {
    @Autowired VocabService service

    @Unroll
    def "generate practice in difficulty: #difficulty"(VocabDifficulty difficulty) {
        when: "generate a practice"
        def practice = service.generatePractice(difficulty)

        then:
        practice.getVocabs().size() == 3
        practice.isGenerated()
        practice.vocabDifficulty == difficulty
        practice.id != null && practice.id < 0

        where:
        difficulty << VocabDifficulty.values()
    }

}
