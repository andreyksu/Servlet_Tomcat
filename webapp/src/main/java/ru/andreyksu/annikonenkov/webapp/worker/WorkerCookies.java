package ru.andreyksu.annikonenkov.webapp.worker;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class WorkerCookies {

	public void setCookies(HttpServletResponse response, String email) {
		Cookie firstCoockie = new Cookie("auth", "Message");
		firstCoockie.setMaxAge(60 * 60);
		response.addCookie(firstCoockie);

		Cookie secondCoockie = new Cookie("LoginMemeber", email);
		secondCoockie.setMaxAge(60 * 60);
		response.addCookie(secondCoockie);
	}
}
