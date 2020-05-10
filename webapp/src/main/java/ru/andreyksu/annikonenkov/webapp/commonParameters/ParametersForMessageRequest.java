package ru.andreyksu.annikonenkov.webapp.commonParameters;

public enum ParametersForMessageRequest {
    Direct("Direct"), LoginMemeber(ParametersOfUser.Login.getParameter()), Interlocutor("Interlocutor"),
    TimeMillisec("timeMillisec"), Historical("historical"), TextMessage("textMessage");

    private String _param;

    ParametersForMessageRequest(String param) {
        _param = param;
    }

    public String getParameter() {
        return _param;
    }

}
