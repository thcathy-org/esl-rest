package com.esl.service.rest;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WebParserRestServiceTest {
    @Test
    public void testSetupHost() {
        assertThat(new WebParserRestService("https://ffs.com/").host, is("https://ffs.com/"));
        assertThat(new WebParserRestService("https://ffs.com").host, is("https://ffs.com/"));
        assertThat(new WebParserRestService("http://ffs.com").host, is("http://ffs.com/"));
        assertThat(new WebParserRestService("ffs.com").host, is("http://ffs.com/"));
    }
}
