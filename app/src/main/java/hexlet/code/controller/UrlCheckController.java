package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;

import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import kong.unirest.HttpResponse;

import java.sql.SQLException;
import java.util.Objects;


public class UrlCheckController {
    public static void createCheck(Context ctx) throws SQLException {
        long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        //получаю из бд сохранённый урл:
        String urlName;
        if (UrlsRepository.find(urlId).isPresent()) {
            urlName = UrlsRepository.find(urlId).get().getName();
        } else {
            throw (new SQLException("No such mane in DB"));
        }

        try {
            //дергаю страницу по url:
            HttpResponse<String> response = Unirest.get(urlName).asString();
            //парсинг содержимого:
            Document doc = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String title = doc.title();
            //String h1 = doc.select("h1").text();
            String h1 = Objects.requireNonNull(doc.selectFirst("h1"))
                    .getElementsByTag("h1").isEmpty()
                    ? "" : Objects.requireNonNull(doc.selectFirst("h1"))
                    .getElementsByTag("h1").html();
            //String description = doc.select("meta[name=description]").attr("content");
            String description = doc.getElementsByAttributeValue("name", "description").isEmpty() ? "" :
                    doc.getElementsByAttributeValue("name", "description").get(0).attr("content");

            var urlCheck = new UrlCheck(urlId, statusCode, title, h1, description);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "success");
            //ctx.redirect(NamedRoutes.urlPath(urlId));

        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flashType", "danger");
            //Lctx.redirect(NamedRoutes.urlPath(urlId));
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
