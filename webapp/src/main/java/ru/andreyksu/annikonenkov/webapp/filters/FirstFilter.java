package ru.andreyksu.annikonenkov.webapp.filters;

import java.io.IOException;
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

public class FirstFilter implements Filter {

	private static final Logger _log = LogManager.getLogger(FirstFilter.class);

	private static Map<String, String> _mapOfAuthUser;

	private static DataSourceProvider dataSP;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		_log.debug("___Filter1____Init");
		dataSP = DataSourceProvider.getSQLDataSource();
		_mapOfAuthUser = dataSP.getMapAuthUser();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		_log.debug(request.getParameterNames());
		_log.debug("___Filter1___DoFilter REQ");
		String mainParam = request.getParameter("mode");
		if (mainParam != null && mainParam.equals("Message")) {
			_log.debug("___Filter1___ Похоже что mode = Message");
			SetterAndDeleterCookies workerCookies = new SetterAndDeleterCookies(_log);
			boolean isExistCookie = workerCookies.isExistsUserByCookies((HttpServletRequest) request, (HttpServletResponse) response, _mapOfAuthUser);
			if (isExistCookie) {
				_log.debug(String.format("___Filter1___ workerCookies.getEmail() = %s ", workerCookies.getEmail()));
				request.setAttribute("LoginMemeber", workerCookies.getEmail());
			}
		}
		chain.doFilter(request, response);
		_log.debug("___Filter1___DoFilter RESP");
	}

	@Override
	public void destroy() {
		_log.debug("___Filter1___Destroy");
	}

}
