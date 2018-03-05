package ru.andreyksu.annikonenkov.webapp.worker;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;

public class CollectParameters {

    private String message = "<html><head><title>Параметры!</title></head><body><h2>Ниже представлены параметры</h2>%s</body></html>";

    private final Logger _log;

    public CollectParameters(Logger aLog) {
        _log = aLog;
    }

    public List<String> infoAboutClient(HttpServletRequest request) {
        List<String> infoList = new ArrayList<String>();
        infoList.add("Общая информациия <-----> ");
        Thread thread = Thread.currentThread();
        String threadStr = String.format("ThreadName = %s", thread.getName());

        String addrRemote = request.getRemoteAddr();
        String hostRemote = request.getRemoteHost();
        String userRemote = request.getRemoteUser();
        String uriRemote = request.getRequestURI();
        int portRemote = request.getRemotePort();
        String strRemote =
                String.format("Remote Addr = %s, Host = %s, Port = %d, User = %s, URI = %s", addrRemote, hostRemote, portRemote, userRemote, uriRemote);

        String addrLocal = request.getLocalAddr();
        String nameLocal = request.getLocalName();
        int portLocal = request.getLocalPort();
        String strLocal = String.format("Local Addr = %s, Name = %s, Port = %d", addrLocal, nameLocal, portLocal);

        String scheme = request.getScheme();
        String protocol = request.getProtocol();
        String parameters = String.format("Scheme = %s, Protocol = %s", scheme, protocol);

        String contentType = request.getContentType();
        String characterEncoding = request.getCharacterEncoding();
        int length = request.getContentLength();
        String content = String.format("ContentType = %s, CharacterEncoding = %s, length = %d", contentType, characterEncoding, length);

        String contextPath = request.getContextPath();
        String queryString = request.getQueryString();
        String pathInfo = request.getPathInfo();
        String pathTranslated = request.getPathTranslated();
        String paths =
                String.format("ContextPath = %s, QueryString = %s, PathInfo = %s, PathTranslated=%s", contextPath, queryString, pathInfo, pathTranslated);

        infoList.addAll(Arrays.asList(threadStr, strRemote, strLocal, parameters, content, paths));
        return infoList;
    }

    public List<String> getParametrOfRequest(HttpServletRequest request) {
        List<String> listString = new ArrayList<>();
        listString.add("Параметры запроса <-----> ");
        String nameOfKey = "";
        Map<String, String[]> mapRequest = request.getParameterMap();
        for (java.util.Map.Entry<String, String[]> e : mapRequest.entrySet()) {
            StringBuilder sb = new StringBuilder();
            nameOfKey = e.getKey();
            String[] array = e.getValue();
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
                sb.append(": ");
            }
            listString.add(String.format("%s = %s", nameOfKey, sb.toString()));
        }
        return listString;
    }

    public List<String> getAttributOfRequest(HttpServletRequest request) {
        List<String> listAttribut = new ArrayList<>();
        listAttribut.add("Атрибуты <-----> ");
        Enumeration<String> namesOfAttributs = request.getAttributeNames();
        while (namesOfAttributs.hasMoreElements()) {
            String nameOfAtr = namesOfAttributs.nextElement();
            String valueOfAtr = request.getAttribute(nameOfAtr).toString();
            listAttribut.add(String.format("%s = %s", nameOfAtr, valueOfAtr));
        }
        return listAttribut;
    }

    public List<String> getHeaders(HttpServletRequest request) {
        List<String> listHeaders = new ArrayList<>();
        listHeaders.add("Заголовки <-----> ");
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String valHeader = request.getHeader(name);
            listHeaders.add(String.format("%s = %s", name, valHeader));
        }
        return listHeaders;
    }

    public List<String> getBodyOfRequest(HttpServletRequest request) {
        List<String> listOfBody = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String tmpStr = "";
        try (BufferedReader pw = request.getReader()) {
            while ((tmpStr = pw.readLine()) != null) {
                sb.append(tmpStr);
            }
        } catch (Exception exc) {
            _log.error("Произошла ошибка при чтении из потока", exc);
        }
        listOfBody.add(String.format("Тело сообщения: \n %s", sb.toString()));
        return listOfBody;
    }

    public List<String> getCookieOfRequest(HttpServletRequest request) {
        List<String> listOfCookie = new ArrayList<>();
        listOfCookie.add("Куки <----->");
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    String name = cookie.getName();
                    String value = cookie.getValue();
                    listOfCookie.add(String.format("%s = %s ", name, value));
                }
            }
        } catch (Exception exc) {
            _log.error("Ошибка в getCookieOfRequest", exc);
        }
        return listOfCookie;
    }

    public List<String> getInitParameters(HttpServlet aHttpServlet) {
        List<String> listOfInitParameters = new ArrayList<>();
        Enumeration<String> nameOfParametors = aHttpServlet.getInitParameterNames();
        ServletConfig servletConfit = aHttpServlet.getServletConfig();
        while (nameOfParametors.hasMoreElements()) {
            String name = (String) nameOfParametors.nextElement();
            listOfInitParameters.add(String.format("%s = %s", name, servletConfit.getInitParameter(name)));
        }
        listOfInitParameters.add(String.format("servletName = %s ", servletConfit.getServletName()));
        listOfInitParameters.add(String.format("getContextPath = %s ", aHttpServlet.getServletContext().getContextPath()));
        listOfInitParameters.add(String.format("getRealPath \"/\" = %s ", aHttpServlet.getServletContext().getRealPath("/")));
        listOfInitParameters.add(String.format("getServletContextName = %s ", aHttpServlet.getServletContext().getServletContextName()));
        listOfInitParameters.add(String.format("getServerInfo = %s ", aHttpServlet.getServletContext().getServerInfo()));

        ServletContext servletContext = servletConfit.getServletContext();
        Enumeration<String> paramsContext = servletContext.getInitParameterNames();
        while (paramsContext.hasMoreElements()) {
            String name = (String) paramsContext.nextElement();
            listOfInitParameters.add(String.format("%s = %s", name, servletContext.getInitParameter(name)));
        }

        return listOfInitParameters;
    }

    private String makePrintableForHTML(List<String> list) {
        _log.info(list);

        String delimiter = "<hr align='left' width='1000' size='2' color='Red' />";
        String NexLine = "<br>";
        StringBuilder sb = new StringBuilder();
        sb.append(delimiter);
        for (String str : list) {
            sb.append(NexLine);
            sb.append(str);
        }
        return sb.toString();
    }

    public String getPretty(HttpServletRequest request, HttpServlet httpServlet) {
        String StartDiv = "<div>", EndDiv = "</div>";
        StringBuilder sb = new StringBuilder();
        sb.append(StartDiv);
        sb.append(makePrintableForHTML(infoAboutClient(request)));
        sb.append(makePrintableForHTML(getParametrOfRequest(request)));
        sb.append(makePrintableForHTML(getAttributOfRequest(request)));
        sb.append(makePrintableForHTML(getHeaders(request)));
        sb.append(makePrintableForHTML(getBodyOfRequest(request)));
        sb.append(makePrintableForHTML(getCookieOfRequest(request)));
        sb.append(makePrintableForHTML(getInitParameters(httpServlet)));
        sb.append(EndDiv);
        return String.format(message, sb.toString());
    }

}
