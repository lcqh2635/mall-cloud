package com.mallcloud.commons.banner.printer;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

public class ImageBannerPrinter implements Banner {

    @Override
    public void printBanner(@NonNull Environment environment, Class<?> sourceClass, @NonNull PrintStream out) {

    }

}
