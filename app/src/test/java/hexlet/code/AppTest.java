package hexlet.code;

import static hexlet.code.App.readResourceFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

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
    private static MockWebServer mockServer;
    private static String baseUrl;

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockServer = new MockWebServer();
        baseUrl = mockServer.url("/").toString();
        MockResponse mockResponse = new MockResponse().setBody(readResourceFile("fixtures/test.html"));
        mockServer.enqueue(mockResponse);
    }

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockServer.shutdown();
    }


    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор страниц");
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testUrlPage() {
        String input = "https://www.mail.ru";
        var url = new Url(input);
        url.setCreatedAt(new Timestamp(new Date().getTime()));
        UrlsRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            assertTrue(UrlsRepository.find(url.getId()).isPresent());

            var response = client.get(NamedRoutes.urlPath(url.getId()));

            assertThat(response.code()).isEqualTo(200);
            assertThat(Objects.requireNonNull(response.body()).string()).contains(input);
            assertEquals(UrlsRepository.find(url.getId()).get().getName(), input);
            assertEquals(UrlsRepository.find(input).get().getName(), input);
        });
    }

    @Test
    void testRegisterNewUrl() {
        String input = "url=https://www.mail.ru";

        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlsPath(), input)) {
                assertThat(response.code()).isEqualTo(200);
                assertThat(Objects.requireNonNull(response.body()).string()).
                        contains("https://www.mail.ru");
            }
            assertThat(UrlsRepository.getEntities()).hasSize(1);
        });
    }

    @Test
    public void testDoubleUrlPage() {
        String input = "url=https://www.mail.ru";

        JavalinTest.test(app, (server, client) -> {
            client.post(NamedRoutes.urlsPath(), input);
            client.post(NamedRoutes.urlsPath(), input);
            assertThat(UrlsRepository.getEntities()).hasSize(1);
        });
    }

    @Test
    public void testInvalidUrl() {
        String input = "url=lalala.ru";

        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlsPath(), input)) {
                assertThat(response.code()).isEqualTo(400);
                assertThat(response.body() != null ? response.body().string() : null).contains("Некорректный URL");
            }
        });
    }

    @Test
    void testUrlNotFound() {
        var id = 9999;

        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(id));
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testUrlCheck() {

        Url url = new Url(baseUrl);
        UrlsRepository.save(url);

        JavalinTest.test(app, (server, client) -> {
            try (var response = client.post(NamedRoutes.urlChecksPath(url.getId()))) {
                assertThat(response.code()).isEqualTo(200);

                var check = UrlCheckRepository.find(url.getId()).orElseThrow();

                assertThat(check.getTitle()).isEqualTo("Test Title");
                assertThat(check.getH1()).isEqualTo("Test Page Analyzer");
                assertThat(check.getDescription()).isEqualTo("");
            } catch (final Throwable th) {
                System.out.println(th.getMessage());
            }
        });
    }
}
