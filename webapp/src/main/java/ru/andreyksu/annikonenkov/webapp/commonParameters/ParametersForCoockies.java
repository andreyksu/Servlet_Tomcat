package ru.andreyksu.annikonenkov.webapp.commonParameters;

public enum ParametersForCoockies {
    Site("site"), NameOfChat("MyChat");

    private String _param;

    ParametersForCoockies(String param) {
        _param = param;
    }

    public String getParameter() {
        return _param;
    }

}
