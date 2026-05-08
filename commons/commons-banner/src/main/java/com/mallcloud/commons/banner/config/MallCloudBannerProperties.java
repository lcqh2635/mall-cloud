package com.mallcloud.commons.banner.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
//@ConfigurationProperties(prefix = MallCloudBannerProperties.PREFIX)
public class MallCloudBannerProperties {

    public static final String PREFIX = "mallcloud.banner";

    private boolean enabled = true;
    // 样式主题：default / minimal / box / colorful
    private String theme = "default";
    private String location = "classpath:banner/default-banner.txt";
    private String applicationName;
    private String version = "1.0.0";
    private String author;
    private String description;
    private boolean showInfo = true;
    private BannerType type = BannerType.FIGLET;

    @Getter
    @RequiredArgsConstructor
    public enum BannerType {
        FIGLET("figlet", "Figlet banner"),
        TEXT("text", "Text banner"),
        IMAGE("image", "Image banner");

        private final String value;
        private final String description;
    }

}