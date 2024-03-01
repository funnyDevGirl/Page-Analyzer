package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;

import java.net.URI;
import java.util.Collections;


public class UrlsController {
    public static void create(Context ctx) {

        //var url = ctx.formParamAsClass("name", String.class).get();
        var url = ctx.formParamAsClass("url", String.class).get();

        try {
            URI uri = new URI(url);

            String urlName = String
                .format(
                        "%s://%s%s",
                        uri.getScheme(),
                        uri.getHost(),
                        uri.getPort() < 0 || uri.getPort() > 65535 ? "" : ":" + uri.getPort()
                )
                .toLowerCase();

            Url newUrl = new Url(urlName);

            //Timestamp createdAt = new Timestamp(new Date().getTime());
            //Url newUrl = new Url(urlName, createdAt);

            if (UrlsRepository.find(newUrl.getName()).isPresent()) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "warning");
            } else {
                UrlsRepository.save(newUrl);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "success");
            }
            ctx.redirect(NamedRoutes.urlsPath());

        } catch (Exception e) {
            var page = new BasePage("Некорректный URL", "error");
            ctx.render("index.jte", Collections.singletonMap("page", page));
        }
    }

    public static void index(Context ctx) {
        var flash = ctx.consumeSessionAttribute("flash");
        var flashType = ctx.consumeSessionAttribute("flashType");
        var page = new UrlsPage(UrlsRepository.getEntities());

        if (flash != null && flashType != null) {
            page.setFlash(String.valueOf(flash));
            page.setFlashType(String.valueOf(flashType));
        }
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void show(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();

//        var url = UrlsRepository.find(id)
//                .orElseThrow(() -> new NotFoundResponse("URL not found"));

        var url = UrlsRepository.find(id).isPresent() ? UrlsRepository.find(id).get() : null;

        var checks = UrlCheckRepository.getChecksById(id);
        var flash = ctx.consumeSessionAttribute("flash");
        var flashType = ctx.consumeSessionAttribute("flashType");
        var page = new UrlPage(url, checks);
        if (flash != null && flashType != null) {
            page.setFlash(String.valueOf(flash));
            page.setFlashType(String.valueOf(flashType));
        }
        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }
}
