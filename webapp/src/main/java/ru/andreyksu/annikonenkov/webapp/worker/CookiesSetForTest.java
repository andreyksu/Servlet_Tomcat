package ru.andreyksu.annikonenkov.webapp.worker;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class CookiesSetForTest {

	public void setCookies(HttpServletResponse response) {
		Cookie firstCoockie = new Cookie("firstName", "Andrey");
		Cookie secondCoockie = new Cookie("secondName", "Nik");
		firstCoockie.setMaxAge(60 * 60 * 24);
		secondCoockie.setMaxAge(60 * 60 * 24);
		response.addCookie(firstCoockie);
		response.addCookie(secondCoockie);
	}
}
