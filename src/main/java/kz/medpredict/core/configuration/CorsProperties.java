package kz.medpredict.core.configuration;

import java.net.URI;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "medpredict.cors")
public record CorsProperties(List<String> allowedOrigins) {
    public CorsProperties {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException("At least one CORS origin is required.");
        }
        if (allowedOrigins.stream().anyMatch(origin -> origin == null || origin.isBlank())) {
            throw new IllegalArgumentException("CORS origins must not be blank.");
        }
        allowedOrigins = allowedOrigins.stream().map(String::trim).toList();
        for (String origin : allowedOrigins) {
            URI uri = URI.create(origin);
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null || uri.getPort() > 65535
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())) {
                throw new IllegalArgumentException("CORS entries must be HTTP(S) origins without paths.");
            }
        }
    }
}
