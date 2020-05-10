package ru.andreyksu.annikonenkov.webapp.commonParameters;

public enum ParametersForFile {
    Direct(ParametersForMessageRequest.Direct.getParameter()),
    LoginMemeber(ParametersForMessageRequest.LoginMemeber.getParameter()), UUID("FileUUD"), FileName("FileName");

    private String _param;

    ParametersForFile(String param) {
        _param = param;
    }

    public String getParameter() {
        return _param;
    }

}
