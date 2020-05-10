package ru.andreyksu.annikonenkov.webapp.worker;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.andreyksu.annikonenkov.webapp.commonParameters.ParametersForCoockies;
import ru.andreyksu.annikonenkov.webapp.commonParameters.ParametersOfUser;

public class SetterAndDeleterCookies {

    private static final String _сookieNameOfChat = ParametersForCoockies.Site.getParameter();

    private static final String _valueNameOfChat = ParametersForCoockies.NameOfChat.getParameter();

    private static final String _cookieForEmailField = ParametersOfUser.Login.getParameter();

    private static final int _defaultTime = 60 * 60 * 3;

    private String _emailFromCookie = null;

    private static Logger _log = LogManager.getLogger(SetterAndDeleterCookies.class);

    public void setCookiesWithTime(HttpServletResponse response, String email, int time) {
        _log.debug("Сетим cookie email = {}", email);

        Cookie cookieForNameChat = new Cookie(_сookieNameOfChat, _valueNameOfChat);
        cookieForNameChat.setMaxAge(time);
        cookieForNameChat.setPath("/");
        response.addCookie(cookieForNameChat);

        Cookie cookieForMemberEmail = new Cookie(_cookieForEmailField, email);
        cookieForMemberEmail.setMaxAge(time);
        cookieForMemberEmail.setPath("/");
        response.addCookie(cookieForMemberEmail);
    }

    public void setCookies(HttpServletResponse response, String email) {
        setCookiesWithTime(response, email, _defaultTime);
    }

    public void deleteCookies(HttpServletResponse response, String email) {
        setCookiesWithTime(response, email, 0);
    }

    /**
     * В cookie проверяет наличие параметров: имя чата + email пользователя.
     * 
     * @param request
     * @param response
     * @param mapAuthUser
     * @return
     */
    public boolean isExistsUserByCookies(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            _log.debug("Похоже куков нет: cookies == null !");
            return false;
        }
        boolean presentNameChat = false, presentEmail = false;
        for (Cookie tmpCookie : cookies) {
            if (tmpCookie.getName().equals(_сookieNameOfChat) && tmpCookie.getValue().equals(_valueNameOfChat))
                presentNameChat = true;
            if (tmpCookie.getName().equals(_cookieForEmailField)) {
                _emailFromCookie = tmpCookie.getValue();
                presentEmail = true;
            }
        }
        if (presentNameChat && presentEmail) {
            _log.debug("По Cookies прошли проверку ! Т.е. в куках есть информация по пользователю с email = {}!",
                    _emailFromCookie);
            setCookies(response, _emailFromCookie);// Т.е. обновляем cookie.
            return true;
        }
        return false;
    }

    public String getEmail() {
        return _emailFromCookie;
    }
}
