package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;

import io.javalin.http.NotFoundResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import kong.unirest.HttpResponse;

public class UrlCheckController {
    public static void createCheck(Context ctx) {
        long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlsRepository.find(urlId)
                .orElseThrow(() -> new NotFoundResponse("Url with id = " + urlId + " not found"));

        try {
            //дергаю страницу по url:
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            //парсинг содержимого:
            Document doc = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String title = doc.title();
            String h1 = doc.select("h1").text();
            String description = doc.select("meta[name=description]").attr("content");

            var urlCheck = new UrlCheck(urlId, statusCode, title, h1, description);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "success");
            ctx.redirect(NamedRoutes.urlPath(urlId));

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlPath(urlId));

        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlPath(urlId));
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
