package ru.andreyksu.annikonenkov.webapp.worker;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;

public class SetterAndDeleterCookies {

	private static final String _сookieOfNameChat = "site";

	private static final String _nameOfChat = "MyChat";

	private static final String _cookieForEmailField = "LoginMemeber";

	private static final int _defaultSec = 60 * 60;

	private String _innerEmail = null;
	
	private Logger _log = null;
	
	public SetterAndDeleterCookies(Logger log) {
		_log = log;
	}

	public void setCookiesWithTime(HttpServletResponse response, String email, int time) {
		Cookie firstCoockie = new Cookie(_сookieOfNameChat, _nameOfChat);
		firstCoockie.setMaxAge(time);
		response.addCookie(firstCoockie);

		Cookie secondCoockie = new Cookie(_cookieForEmailField, email);
		_log.debug(String.format("___Cookies____ сетим cookie email = %s", email));
		secondCoockie.setMaxAge(time);
		response.addCookie(secondCoockie);
	}

	public void setCookies(HttpServletResponse response, String email) {
		setCookiesWithTime(response, email, _defaultSec);
	}

	public void deleteCookies(HttpServletResponse response, String email) {
		setCookiesWithTime(response, email, 0);
	}

	public boolean isExistsUserByCookies(HttpServletRequest request, HttpServletResponse response, Map<String, String> mapAuthUSer) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			_log.debug("Похожу куков вообще нет");
			return false;
		}
		String innerEmail = null;
		boolean boolNameChat = false, boolEmail = false;
		for (Cookie tmpCookie : cookies) {
			if (tmpCookie.getName().equals(_сookieOfNameChat) && tmpCookie.getValue().equals(_nameOfChat))
				boolNameChat = true;
			if (tmpCookie.getName().equals(_cookieForEmailField)) {
				innerEmail = tmpCookie.getValue();
				if (mapAuthUSer.containsKey(innerEmail))
					boolEmail = true;
			}
		}
		if (boolNameChat && boolEmail) {
			_log.debug("По Cookies прошли проверку метод isExistsUserByCookies");
			_log.debug(String.format("___Cookies____ innerEmail = %s", innerEmail));
			setCookies(response, innerEmail);
			_innerEmail = innerEmail;
			return true;
		}
		return false;
	}

	public String getEmail() {
		return _innerEmail;
	}
}
