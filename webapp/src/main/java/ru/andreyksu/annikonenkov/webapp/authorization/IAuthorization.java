package ru.andreyksu.annikonenkov.webapp.authorization;

import java.io.IOException;

public interface IAuthorization {

    boolean isAuthorizedUserInSystem(String email, String password) throws IOException;

    boolean isPresentUserInLocalMapAsAuthorized(String email);

    boolean isPresentUserInLocalMapAsAuthorized(String email, String password);

    void unAuthorizedUser(String email);

    boolean registrateUserInSystem(String email, String password, String name) throws IOException;

}
