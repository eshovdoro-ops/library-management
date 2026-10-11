package com.library.service;

import java.util.Locale;

final class SearchUtil {
    private SearchUtil() {}

    /** Превращает пользовательскую строку в безопасный LIKE-шаблон (значение всё равно передаётся параметром). */
    static String pattern(String q) {
        if (q == null || q.isBlank()) return "%";
        String t = q.trim();
        if (t.length() > 100) t = t.substring(0, 100);
        return "%" + t.toLowerCase(Locale.ROOT) + "%";
    }
}
