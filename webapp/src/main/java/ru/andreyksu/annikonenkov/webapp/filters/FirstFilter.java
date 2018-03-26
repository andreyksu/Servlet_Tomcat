package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.postgressql.DataSourceProvider;
import ru.andreyksu.annikonenkov.webapp.worker.SetterAndDeleterCookies;
import ru.andreyksu.annikonenkov.webapp.wrappers.WrapperMutableHttpServletRequest;

public class FirstFilter implements Filter {

	private static final Logger ___log = LogManager.getLogger(FirstFilter.class);

	private static Map<String, String> _mapOfAuthUser;

	private static DataSourceProvider dataSP;

	private static final String _loginMember = "LoginMemeber";

	private static final String _message = "Message";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		___log.debug("___Filter1____Init");
		dataSP = DataSourceProvider.getSQLDataSource();
		_mapOfAuthUser = dataSP.getMapAuthUser();
	}

	private void printInfo(HttpServletRequest httpreq) {
		Enumeration<String> enumAtr = httpreq.getAttributeNames();
		___log.debug("Attrebut - доступные значения из запроса");
		while (enumAtr.hasMoreElements()) {
			String string = (String) enumAtr.nextElement();
			String val = httpreq.getParameter(string);
			___log.debug("{} = {}", string, val);
		}
		Enumeration<String> enumHeader = httpreq.getHeaderNames();
		___log.debug("Headers - доступные значения из запроса");
		while (enumHeader.hasMoreElements()) {
			String string = (String) enumHeader.nextElement();
			String val = httpreq.getHeader(string);
			___log.debug("{} = {}", string, val);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpreq = (HttpServletRequest) request;
		WrapperMutableHttpServletRequest mutableRequest = new WrapperMutableHttpServletRequest(httpreq);
		printInfo(httpreq);

		___log.debug("___Filter1___DoFilter REQ");
		String mainParam = mutableRequest.getParameter("mode");
		if (mainParam != null && mainParam.equals(_message)) {
			___log.debug("___Filter1___ Похоже что mode = Message");
			SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies(___log);
			boolean isExistCookie = workerCookies.isExistsUserByCookies((HttpServletRequest) mutableRequest, (HttpServletResponse) response, _mapOfAuthUser);
			if (isExistCookie) {
				___log.debug(String.format("___Filter1___ workerCookies.getEmail() = %s ", workerCookies.getEmail()));
				mutableRequest.putHeader(_loginMember, workerCookies.getEmail());
			}
		}
		chain.doFilter(mutableRequest, response);
		___log.debug("___Filter1___DoFilter RESPONSE");
	}

	@Override
	public void destroy() {
		___log.debug("___Filter1___Destroy");
	}

}
