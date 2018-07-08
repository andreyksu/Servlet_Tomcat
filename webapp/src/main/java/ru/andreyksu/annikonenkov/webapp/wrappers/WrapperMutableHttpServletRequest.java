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

public class WrapperMutableHttpServletRequest extends HttpServletRequestWrapper {

	private static final Logger ___log = LogManager.getLogger(WrapperMutableHttpServletRequest.class);

	private final Map<String, String> customHeaders;

	public WrapperMutableHttpServletRequest(HttpServletRequest request) {
		super(request);
		customHeaders = new HashMap<String, String>();
	}

	public void putHeader(String name, String value) {
		this.customHeaders.put(name, value);
	}

	public String getHeader(String name) {
		String headerValue = customHeaders.get(name);
		___log.debug("Получаем локального map нашего Wrapper {} = {}", name, headerValue);
		if (headerValue != null) {
			return headerValue;
		}
		String tmp = ((HttpServletRequest) getRequest()).getHeader(name);
		___log.debug("Получаем из исходного Request {} = {}", name, tmp);
		return tmp;
	}

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
