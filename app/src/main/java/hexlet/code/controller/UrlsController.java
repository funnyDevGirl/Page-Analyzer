package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class UrlsController {
    public static void create(Context ctx) {

        var url = ctx.formParamAsClass("url", String.class).get().trim();

        try {
            Url newUrl = getUrl(url);

            if (UrlsRepository.find(newUrl.getName()).isPresent()) {
                //сохраняю флеш-сообщение:
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flashType", "warning");
            } else {
                UrlsRepository.save(newUrl);

                //сохраняю флеш-сообщение:
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flashType", "success");
            }
            ctx.redirect(NamedRoutes.urlsPath());

        } catch (URISyntaxException e) {
            var page = new BasePage("Некорректный URL", "error");
            ctx.status(400);
            ctx.render("index.jte", Collections.singletonMap("page", page));

            //прерываю метод, чтобы не сохранялся "null://null":
            return;
        }
    }

    @NotNull
    private static Url getUrl(String url) throws URISyntaxException {
        var uri = new URI(url);
        if (Objects.equals(uri.getScheme(), null) || Objects.equals(uri.getHost(), null)) {
            throw new URISyntaxException(uri.toString(), "Некорректный URL");
        }
        String urlName = String.
                format("%s://%s%s",
                        uri.getScheme(),
                        uri.getHost(),
                        uri.getPort() < 0 || uri.getPort() > 65535 ? "" : ":" + uri.getPort()
                ).toLowerCase();

        return new Url(urlName);
    }

    public static void index(Context ctx) {

        //читаю флеш-сообщение из сессии по ключу:
        var flash = ctx.consumeSessionAttribute("flash");
        var flashType = ctx.consumeSessionAttribute("flashType");

        var page = new UrlsPage(UrlsRepository.getEntities());

        page.setFlash((String) flash);
        page.setFlashType((String) flashType);

        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void show(Context ctx) {

        long id = ctx.pathParamAsClass("id", Long.class).get();

        Optional<Url> optional = UrlsRepository.find(id);

        if (optional.isEmpty()) {
            throw new NotFoundResponse("Url with id = " + id + " not found");
        }

        Url url = optional.get();

        List<UrlCheck> checks = UrlCheckRepository.getChecksById(id);

        var page = new UrlPage(url, checks);

        //читаю флеш-сообщение из сессии:
        var flash = ctx.consumeSessionAttribute("flash");
        var flashType = ctx.consumeSessionAttribute("flashType");

        page.setFlash((String) flash);
        page.setFlashType((String) flashType);

        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }
}
