package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@ToString
public final class Url {
    @Setter
    private long id;

    @ToString.Include
    private String name;

    @Setter
    private Timestamp createdAt;

    //@Setter
    //    private List<UrlCheck> checks;

    public Url(String name) {
        this.name = name;
    }
}
