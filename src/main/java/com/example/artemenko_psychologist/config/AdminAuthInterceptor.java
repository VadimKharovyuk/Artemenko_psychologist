package com.example.artemenko_psychologist.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Интерцептор для защиты административных URL
 */
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final String ADMIN_AUTH_KEY = "adminAuthenticated";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Получаем текущий URL
        String requestURI = request.getRequestURI();

        // Пропускаем страницу логина и обработку формы логина
        if (requestURI.equals("/admin/login") || requestURI.equals("/admin/login-process")) {
            return true;
        }

        // Проверяем сессию
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(ADMIN_AUTH_KEY))) {
            // Пользователь авторизован как администратор
            return true;
        }

        // Пользователь не авторизован - перенаправляем на страницу входа
        response.sendRedirect("/admin/login");
        return false;
    }
}