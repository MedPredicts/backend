package kz.medpredict.core.configuration;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsPropertiesTest {
    @Test
    void rejectsMissingOriginsAndInvalidPorts() {
        assertThatThrownBy(() -> new CorsProperties(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CorsProperties(List.of())).isInstanceOf(IllegalArgumentException.class);
        for (String origin : List.of(" ", "http://localhost:65536")) {
            assertThatThrownBy(() -> new CorsProperties(List.of(origin)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        var withNull = new java.util.ArrayList<String>();
        withNull.add(null);
        assertThatThrownBy(() -> new CorsProperties(withNull)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWildcardAndNonOriginUrls() {
        for (String origin : List.of("*", "https://example.com/path", "https://example.com?token=x",
                "https://user@example.com", "file:///tmp")) {
            assertThatThrownBy(() -> new CorsProperties(List.of(origin)))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void defensivelyCopiesOrigins() {
        var properties = new CorsProperties(List.of(" http://localhost:5173 "));
        assertThat(properties.allowedOrigins()).containsExactly("http://localhost:5173");
        assertThatThrownBy(() -> properties.allowedOrigins().add("https://example.com"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
