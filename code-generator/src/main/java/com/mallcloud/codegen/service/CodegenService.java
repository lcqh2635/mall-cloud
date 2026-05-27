package com.mallcloud.codegen.service;

import java.util.Set;

public interface CodegenService {
    void generate(String dbUrl, String dbUser, String dbPwd, Set<String> tableNames, String basePackage);
}
