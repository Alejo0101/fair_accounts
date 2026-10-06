package backend.fair_accounts.archivo.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.files")
public record FileStorageProperties(
        @NotBlank String storageLocation,
        @Min(1) long maxFileSizeBytes,
        @Min(1) long maxPixels
) {
}
