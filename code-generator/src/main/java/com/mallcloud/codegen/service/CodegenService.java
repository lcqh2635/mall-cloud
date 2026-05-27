package com.mallcloud.codegen.service;

import com.mallcloud.codegen.model.CodegenRequest;
import jakarta.servlet.ServletOutputStream;

import java.util.Set;

public interface CodegenService {
    void generate(String dbUrl, String dbUser, String dbPwd, Set<String> tableNames, String basePackage);

    void generateAndDownloadZip(CodegenRequest request, ServletOutputStream outputStream);
}
