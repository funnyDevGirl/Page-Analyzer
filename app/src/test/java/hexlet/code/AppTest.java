package hexlet.code;

import static hexlet.code.App.readResourceFile;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.model.Url;
import hexlet.code.util.NamedRoutes;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;


public class AppTest {
    private static Javalin app;
    private static MockWebServer server;

    @BeforeAll
    public static void beforeAll() throws IOException{
        server = new MockWebServer();
        MockResponse response = new MockResponse().setBody(readResourceFile("fixtures/test.html"));
        server.enqueue(response);
    }

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        server.shutdown();
    }


    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор страниц");
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls");
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @Test
    public void testUrlPage(){
        var url = new Url("https://www.mail.ru", new Timestamp(new Date().getTime()));
        UrlsRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("https://www.mail.ru");
        });
    }

    @Test
    void testUrlNotFound() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/999999");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testUrlCheck(){
        var url = server.url("/").toString();
        Url testUrl = new Url(url);
        UrlsRepository.save(testUrl);

        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlChecksPath(testUrl.getId()))) {
                assertThat(response.code()).isEqualTo(200);
            }
            var check = UrlCheckRepository.find(testUrl.getId()).orElseThrow();

            assertThat(check.getTitle()).isEqualTo("Test Title");
            assertThat(check.getH1()).isEqualTo("Test Page Analyzer");
            assertThat(check.getDescription()).isEqualTo("");
        });
    }
}
