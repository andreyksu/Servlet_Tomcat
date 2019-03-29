package ru.andreyksu.annikonenkov.webapp.wrappers;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс предназначен для добавления к запросу необходимых/кастомных header.
 * Применяется для авторизации - добавляется доп. информация к запросу.
 */
public class WrapperMutableHttpServletRequest extends HttpServletRequestWrapper {

    private static Logger _log = LogManager.getLogger(WrapperMutableHttpServletRequest.class);

    private final Map<String, String> customHeaders;

    public WrapperMutableHttpServletRequest(HttpServletRequest request) {
        super(request);
        customHeaders = new HashMap<String, String>();
    }

    /**
     * Добавляет необходимый header и исходному, что доступен из запроса.
     * 
     * @param name - Имя в header
     * @param value - Значение в header
     */

    public void putHeader(String name, String value) {
        this.customHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name);
        if (headerValue != null) {
            _log.debug("Из кастомного RequestWrapper {} = {}", name, headerValue);
            return headerValue;
        }
        String tmp = ((HttpServletRequest) getRequest()).getHeader(name);
        if (tmp != null) {
            _log.debug("Из исходного Request {} = {}", name, tmp);
        } else {
            _log.debug("Похоже что ни в Request ни в RequestWrapper заголовка для имени '{}' нет", name, tmp);
        }
        return tmp;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> set = new HashSet<String>(customHeaders.keySet());
        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements()) {
            String n = e.nextElement();
            set.add(n);
        }
        return Collections.enumeration(set);
    }
}
