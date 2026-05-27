package com.mallcloud.code.generator.service;

import java.util.Set;

public interface CodeGenService {
    void generate(String dbUrl, String dbUser, String dbPwd, Set<String> tableNames, String basePackage);
}
