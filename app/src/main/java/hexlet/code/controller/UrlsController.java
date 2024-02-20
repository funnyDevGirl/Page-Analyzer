package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;


public class UrlsController {

    private static String normalizeUrl(String scheme, String authority) {
        return String.format("%s://%s", scheme, authority);
    }

    private static String normalizeUrl(String scheme, String authority, String port) {
        if (port == null) {
            return normalizeUrl(scheme, authority);
        }
        return String.format("%s://%s:%s", scheme, authority, port);
    }

    public static void create(Context ctx) throws URISyntaxException {

        var url = ctx.formParamAsClass("name", String.class).get();
        URI uri = new URI(url);
        try {
            String scheme = uri.getScheme();
            String authority = uri.getAuthority();
            String port = uri.getHost();

            String urlName = normalizeUrl(scheme, authority, port);
            Timestamp createdAt = new Timestamp(new Date().getTime());

            Url newUrl = new Url(urlName, createdAt);

            if (UrlRepository.existsByName(urlName)) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "warning");
            } else {
                UrlRepository.save(newUrl);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "success");
            }
            ctx.redirect(NamedRoutes.urlsPath());

        } catch (Exception e) {
            var page = new BasePage();
            page.setFlash("Некорректный URL");
            page.setFlashType("error");
            ctx.render("index.jte", Collections.singletonMap("page", page));
        }
        
    }

    public static void index(Context ctx) {
        var flash = ctx.consumeSessionAttribute("flash");
        var flashType = ctx.consumeSessionAttribute("flashType");
        var page = new UrlsPage(UrlRepository.getEntities());
        if (flash != null && flashType != null) {
            page.setFlash((String)flash);
            page.setFlashType((String)flashType);
        }
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void show(Context ctx) {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id)
                .orElseThrow(() -> new NotFoundResponse("URL not found"));

        var page = new UrlPage(url);
        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }
}
