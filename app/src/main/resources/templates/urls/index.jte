@import hexlet.code.dto.urls.UrlsPage
@import hexlet.code.repository.UrlCheckRepository
@import hexlet.code.util.NamedRoutes
@import java.time.format.DateTimeFormatter
@param UrlsPage page

@template.layout.page(
    content = @`
        <section>
            <div class="container-lg mt-5">
                <h1>Сайты</h1>

                <table class="table table-bordered table-hover mt-3">
                    <thead>
                    <tr>
                        <th class="col-1">ID</th>
                        <th>Имя</th>
                        <th class="col-2">Последняя проверка</th>
                        <th class="col-1">Код ответа</th>
                    </tr>
                    </thead>
                    <tbody>

                    @for(var url : page.getUrls())
                        <tr>
                            <td>
                                ${url.getId()}
                            </td>
                            <td>
                                <a href="${NamedRoutes.urlPath(url.getId())}">${url.getName()}</a>
                            </td>
                            <td>
                                @if(UrlCheckRepository.find(url.getId()).isPresent())
                                    ${UrlCheckRepository.getChecksById(url.getId())
                                    .get(UrlCheckRepository.getChecksById(url.getId()).size() - 1)
                                    .getCreatedAt().toLocalDateTime()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}
                                @endif
                            </td>
                            <td>
                                @if(UrlCheckRepository.find(url.getId()).isPresent())
                                    ${UrlCheckRepository.getChecksById(url.getId())
                                    .get(UrlCheckRepository.getChecksById(url.getId()).size() - 1)
                                    .getStatusCode()}
                                @endif
                            </td>
                        </tr>
                    @endfor
                    </tbody>
                </table>
            </div>
        </section>
    `
)