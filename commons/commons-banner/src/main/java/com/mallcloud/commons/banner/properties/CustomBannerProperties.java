package com.mallcloud.commons.banner.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = CustomBannerProperties.PREFIX)
public class CustomBannerProperties {

    public static final String PREFIX = "custom.banner";

    // getters and setters
    private boolean enabled = true;
    private String location = "classpath:banner/default-banner.txt";
    private String applicationName;
    private String version = "1.0.0";
    private String author;
    private String description;
    private boolean showInfo = true;
    private BannerType type = BannerType.TEXT;

    @Getter
    @RequiredArgsConstructor
    public enum BannerType {
        TEXT("text", "Text banner"),
        FIGLET("figlet", "Figlet banner"),
        IMAGE("image", "Image banner");

        private final String value;
        private final String description;
    }

}