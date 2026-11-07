package com.esl.controller;

import com.esl.dao.repository.ImageFlaggingRepository;
import com.esl.entity.ImageFlagging;
import com.esl.service.JWTService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.esl.security.JWTAuthorizationFilter.TESTING_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class ImageFlaggingControllerTests {

    private static final List<Hit> hits = new CopyOnWriteArrayList<>();
    private static final AtomicInteger imageCount = new AtomicInteger(2);
    private static final AtomicInteger postStatus = new AtomicInteger(200);
    private static HttpServer server;

    @DynamicPropertySource
    static void imageServer(DynamicPropertyRegistry registry) {
        var port = ensureServer();
        registry.add("IMAGE_GENERATION_SERVER_HOST", () -> "http://127.0.0.1:" + port);
        registry.add("IMAGE_GENERATION_SERVER_APIKEY", () -> "test-key");
        registry.add("ImageFlagging.ImagesHost", () -> "http://127.0.0.1:" + port + "/images");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ImageFlaggingRepository repository;
    @MockBean JWTService jwtService;

    @Before
    public void clean() {
        repository.deleteAll();
        hits.clear();
        imageCount.set(2);
        postStatus.set(200);
    }

    @Test
    public void anonymousFlagging_storesNullMemberId() throws Exception {
        mockMvc.perform(post("/image-flagging")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"apple\",\"imageIndex\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phrase", is("apple")))
                .andExpect(jsonPath("$.imageIndex", is(0)))
                .andExpect(jsonPath("$.memberId", nullValue()));

        var rows = rows();
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).getMemberId(), nullValue());
        assertThat(rows.get(0).isDone(), is(false));
    }

    @Test
    public void loggedInFlagging_storesMemberId() throws Exception {
        mockMvc.perform(post("/image-flagging")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TESTING_HEADER, "tester@esl.com")
                        .content("{\"phrase\":\"apple\",\"imageIndex\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId", is(1)));

        assertThat(rows().get(0).getMemberId(), is(1L));
    }

    @Test
    public void secondFlagging_insertsAnotherRowForSamePhraseAndIndex() throws Exception {
        postFlagging("apple", 1, null);
        postFlagging("apple", 1, null);

        var rows = rows();
        assertThat(rows, hasSize(2));
        assertThat(rows.stream().map(ImageFlagging::getPhrase).collect(Collectors.toList()), everyItem(is("apple")));
        assertThat(rows.stream().map(ImageFlagging::getImageIndex).collect(Collectors.toList()), everyItem(is(1)));
        assertThat(rows.get(0).getId().equals(rows.get(1).getId()), is(false));
    }

    @Test
    public void adminList_groupsByPhraseAndIndexWithCount() throws Exception {
        postFlagging("apple", 0, null);
        postFlagging("apple", 0, null);
        postFlagging("zoo", 1, null);
        postFlagging("banana", 0, null);
        var finished = rows().stream().filter(row -> row.getPhrase().equals("banana")).findFirst().orElseThrow();
        finished.setDone(true);
        repository.save(finished);

        mockMvc.perform(asAdmin(get("/admin/image-review/flaggings")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].phrase", is("apple")))
                .andExpect(jsonPath("$[0].imageIndex", is(0)))
                .andExpect(jsonPath("$[0].count", is(2)))
                .andExpect(jsonPath("$[1].phrase", is("zoo")))
                .andExpect(jsonPath("$[1].imageIndex", is(1)))
                .andExpect(jsonPath("$[1].count", is(1)));
    }

    @Test
    public void remove_callsIndexRemoveAndMarksThoseFlaggingsDone() throws Exception {
        imageCount.set(3);
        postFlagging("apple", 1, null);
        postFlagging("apple", 1, null);
        postFlagging("zoo", 0, null);

        mockMvc.perform(asAdmin(post("/admin/image-review/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"apple\",\"imageIndex\":1}")))
                .andExpect(status().isNoContent());

        var verified = postsTo("/verify/verified");
        assertThat(verified, hasSize(1));
        assertThat(verified.get(0).apiKey(), is("test-key"));
        JsonNode body = objectMapper.readTree(verified.get(0).body());
        assertThat(body.size(), is(1));
        assertThat(body.get(0).get("phrase").asText(), is("apple"));
        assertThat(body.get(0).get("removeImageIndex").size(), is(1));
        assertThat(body.get(0).get("removeImageIndex").get(0).asInt(), is(1));
        assertThat(postsTo("/verify/remove"), hasSize(0));

        var apple = rows().stream().filter(row -> row.getPhrase().equals("apple")).toList();
        assertThat(apple, hasSize(2));
        assertThat(apple.stream().map(ImageFlagging::isDone).collect(Collectors.toList()), everyItem(is(true)));
        var zoo = rows().stream().filter(row -> row.getPhrase().equals("zoo")).findFirst().orElseThrow();
        assertThat(zoo.isDone(), is(false));

        mockMvc.perform(asAdmin(get("/admin/image-review/flaggings")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].phrase", is("zoo")))
                .andExpect(jsonPath("$[0].imageIndex", is(0)));
    }

    @Test
    public void remove_doesNotWipeRemainingImagesWhenMoreThanOneExists() throws Exception {
        imageCount.set(2);
        postFlagging("apple", 0, null);

        mockMvc.perform(asAdmin(post("/admin/image-review/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"apple\",\"imageIndex\":0}")))
                .andExpect(status().isNoContent());

        assertThat(postsTo("/verify/remove"), hasSize(0));
        var verified = postsTo("/verify/verified");
        assertThat(verified, hasSize(1));
        JsonNode body = objectMapper.readTree(verified.get(0).body());
        assertThat(body.get(0).get("removeImageIndex").toString(), is("[0]"));
        assertThat(rows().get(0).isDone(), is(true));
    }

    @Test
    public void remove_wipesPhraseOnlyWhenItIsTheLastImage() throws Exception {
        imageCount.set(1);
        postFlagging("banana", 0, null);

        mockMvc.perform(asAdmin(post("/admin/image-review/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"banana\",\"imageIndex\":0}")))
                .andExpect(status().isNoContent());

        assertThat(postsTo("/verify/verified"), hasSize(0));
        var removed = postsTo("/verify/remove");
        assertThat(removed, hasSize(1));
        assertThat(removed.get(0).apiKey(), is("test-key"));
        assertThat(objectMapper.readTree(removed.get(0).body()).get("phrase").asText(), is("banana"));
        assertThat(rows().get(0).isDone(), is(true));
    }

    @Test
    public void remove_keepsFlaggingsOpenWhenImageGenerationFails() throws Exception {
        imageCount.set(2);
        postStatus.set(500);
        postFlagging("apple", 0, null);

        mockMvc.perform(asAdmin(post("/admin/image-review/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"apple\",\"imageIndex\":0}")))
                .andExpect(status().isInternalServerError());

        assertThat(postsTo("/verify/verified"), hasSize(1));
        assertThat(postsTo("/verify/remove"), hasSize(0));
        assertThat(rows().get(0).isDone(), is(false));
    }

    @Test
    public void adminPage_isHtmlWithoutImageGenerationKey() throws Exception {
        mockMvc.perform(get("/admin/image-review"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Remove")))
                .andExpect(content().string(containsString("Preview")))
                .andExpect(content().string(containsString("count > 1")))
                .andExpect(content().string(not(containsString("test-key"))))
                .andExpect(content().string(not(containsString("X-API-KEY"))));
    }

    @Test
    public void adminFlaggings_requireAdminRole() throws Exception {
        mockMvc.perform(get("/admin/image-review/flaggings"))
                .andExpect(status().isUnauthorized());

        given(jwtService.parseClaims(any())).willReturn(Optional.of(claims("user")));
        mockMvc.perform(post("/admin/image-review/remove")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phrase\":\"apple\",\"imageIndex\":0}"))
                .andExpect(status().isForbidden());
        assertThat(hits, hasSize(0));
    }

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder builder) {
        given(jwtService.parseClaims(any())).willReturn(Optional.of(claims("admin")));
        return builder.header("Authorization", "Bearer test-token");
    }

    private DefaultClaims claims(String role) {
        var claims = new DefaultClaims();
        claims.put("email", "tester@esl.com");
        claims.put("https://funfunspell.com/roles", new ArrayList<>(List.of(role)));
        return claims;
    }

    private void postFlagging(String phrase, int imageIndex, String email) throws Exception {
        var request = post("/image-flagging")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phrase\":\"" + phrase + "\",\"imageIndex\":" + imageIndex + "}");
        if (email != null) request.header(TESTING_HEADER, email);
        mockMvc.perform(request).andExpect(status().isOk());
    }

    private List<ImageFlagging> rows() {
        var list = new ArrayList<ImageFlagging>();
        repository.findAll().forEach(list::add);
        return list;
    }

    private List<Hit> postsTo(String path) {
        return hits.stream().filter(hit -> hit.path().equals(path)).collect(Collectors.toList());
    }

    private static int ensureServer() {
        if (server != null) return server.getAddress().getPort();
        synchronized (ImageFlaggingControllerTests.class) {
            if (server != null) return server.getAddress().getPort();
            try {
                var created = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                created.createContext("/", exchange -> {
                    var method = exchange.getRequestMethod();
                    var path = exchange.getRequestURI().getPath();
                    var body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    var apiKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
                    if ("POST".equals(method)) hits.add(new Hit(path, body, apiKey));
                    var count = Math.max(imageCount.get(), 0);
                    var images = new StringBuilder();
                    for (var i = 0; i < count; i++) {
                        if (i > 0) images.append(',');
                        images.append("\"img").append(i).append('"');
                    }
                    var response = ("{\"images\":[" + images + "],\"isVerify\":false}").getBytes(StandardCharsets.UTF_8);
                    var status = "POST".equals(method) ? postStatus.get() : 200;
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(status, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                });
                var executor = Executors.newCachedThreadPool(runnable -> {
                    var thread = new Thread(runnable, "image-flagging-stub");
                    thread.setDaemon(true);
                    return thread;
                });
                created.setExecutor(executor);
                created.start();
                server = created;
                return created.getAddress().getPort();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private record Hit(String path, String body, String apiKey) {}
}
