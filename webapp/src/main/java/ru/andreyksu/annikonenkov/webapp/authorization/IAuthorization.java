package ru.andreyksu.annikonenkov.webapp.authorization;

import java.io.IOException;

public interface IAuthorization {


	boolean authorizedInSystem(String email, String password) throws IOException;

	boolean isAuthorizedUser(String email);

	boolean isAuthorizedUser(String email, String password);

	void unAuthorizedUser(String email);

	boolean registrate(String email, String password, String name) throws IOException;

}
