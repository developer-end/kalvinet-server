package app.school.administration.auth.application.constant;

import app.school.administration.common.utils.AppModuleApi;

import java.util.Set;

public interface AuthConstant {

    Set<String> PUBLIC_ENDPOINTS = Set.of(
            AppModuleApi.AUTH,
            AppModuleApi.O_AUTH,
            AppModuleApi.SCHOOL,
            AppModuleApi.TENANT,
            "/api/v1/dashboard",
            "/ws",
            "/v3/api-docs",
            "/swagger-ui",
            "/api/requests",
            "/api/health"
    );

}
