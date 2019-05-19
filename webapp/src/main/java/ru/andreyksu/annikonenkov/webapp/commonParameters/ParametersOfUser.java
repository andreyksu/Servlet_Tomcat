package ru.andreyksu.annikonenkov.webapp.commonParameters;

public enum ParametersOfUser {
    Login("LoginMemeber"), Password("PasswordMember"), Name("NameMember");

    private String _param;

    ParametersOfUser(String param) {
        _param = param;
    }

    public String getParameter() {
        return _param;
    }

}
