package com.esl.service;

import com.esl.dao.PhoneticQuestionDAO;
import com.esl.service.rest.ReplicateAIService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class VocabServiceTest {
    @Mock private PhoneticQuestionService mockPhoneticQuestionService;
    @Mock private PhoneticQuestionDAO mockPhoneticQuestionDAO;

    @Mock private ReplicateAIService mockReplicateAIService;
    AutoCloseable closeable;
    VocabService vocabService;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        vocabService = new VocabService(
                mockPhoneticQuestionService,
                mockPhoneticQuestionDAO,
                mockReplicateAIService
        );
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetMeaning_SimpleWord() {
        String word = "apple";
        List<String> aiResponse = Arrays.asList("A ", "round ", "fruit ", "with ", "red ", "or ", "green ", "skin.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);

        assertEquals("A round fruit with red or green skin.", result);
    }

    @Test
    public void testGetMeaning_WordWithSpecialCharacters() {
        String word = "apple!@#";
        List<String> aiResponse = Arrays.asList("A ", "round ", "fruit ", "with ", "red ", "or ", "green ", "skin.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);

        assertEquals("A round fruit with red or green skin.", result);
    }

    @Test
    public void testGetMeaning_CompoundWord() {
        String word = "bus-stop";
        List<String> aiResponse = Arrays.asList("A ", "place ", "where ", "buses ", "stop ", "to ", "pick ", "up ", "passengers.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);

        assertEquals("A place where ***es *** to pick up passengers.", result);
    }

    @Test
    public void testGetMeaning_ShortWordParts() {
        String word = "to-do";
        List<String> aiResponse = Arrays.asList("A ", "list ", "of ", "tasks ", "that ", "need ", "to ", "be ", "completed.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);
        assertEquals("A list of tasks that need to be completed.", result);
    }

    @Test
    public void testGetMeaning_WordWithHyphenAndSpace() {
        String word = "ice-cream sundae";
        List<String> aiResponse = Arrays.asList("A ", "dessert ", "with ", "ice ", "cream ", "and ", "toppings.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);
        assertEquals("A dessert with *** *** and toppings.", result);
    }

    @Test
    public void testGetMeaning_CaseInsensitivity() {
        String word = "Computer";
        List<String> aiResponse = Arrays.asList("An ", "electronic ", "device ", "for ", "processing ", "data. ", "Computers ", "are ", "essential.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);
        assertEquals("An electronic device for processing data. ***s are essential.", result);
    }

    @Test
    public void testGetMeaning_WordWithMultipleOccurrences() {
        String word = "repeat";
        List<String> aiResponse = Arrays.asList("To ", "repeat ", "something ", "again. ", "You ", "may ", "need ", "to ", "repeat ", "the ", "process.");
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);

        assertEquals("To *** something again. You may need to *** the process.", result);
    }

    @Test
    public void testGetMeaning_ReplaceNewlinesWithSpace_FilterOnlySpaces() {
        String word = "example";
        List<String> aiResponse = Arrays.asList(
                "A ",
                "\n",
                "",
                "\n\n",
                "word\nhere",
                " \n ",
                " test\nend"
        );
        when(mockReplicateAIService.getDefinition(word)).thenReturn(aiResponse);

        String result = vocabService.getMeaning(word);

        assertEquals("A word here test end", result);
    }
}
