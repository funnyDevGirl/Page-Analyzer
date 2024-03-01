package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlsRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class UrlCheckController {
    public static void createCheck(Context ctx) throws SQLException {
        int urlId = ctx.formParamAsClass("id", Integer.class).get();
        //var url = UrlsRepository.find(urlId).orElseThrow(() -> new NotFoundResponse("Url not found"));
        var url = UrlsRepository.find(urlId).get();

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String title = doc.title();
            String h1 = doc.select("h1").text();
            String description = doc.select("meta[name=description]").attr("content");

            //Timestamp createdAt = new Timestamp(new Date().getTime());
            //var urlCheck = new UrlCheck(urlId, statusCode, title, h1, description, createdAt);

            var urlCheck = new UrlCheck(urlId, statusCode, title, h1, description);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "success");
            ctx.redirect(NamedRoutes.urlPath(urlId));
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlPath(urlId));
        }
    }
}
