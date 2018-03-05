package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SecondFilter implements Filter {

    private static final Logger log = LogManager.getLogger(SecondFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("-----Init of Filter2");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("-----DoFilter of Filter2");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("-----Destroy of Filter2");
    }

}
