package ru.andreyksu.annikonenkov.webapp.userrepresent;

import java.io.IOException;
import java.util.List;

public interface IUser {

	boolean isExistUserInSystem() throws IOException;
	
	List<String> getListOfUsers(String activity) throws IOException;

	boolean isUserActive() throws IOException;

	boolean isUserAdmin() throws IOException;

	public String getPassword() throws IOException;

	public String getName() throws IOException;

	public String getData() throws IOException;

	boolean registrateUser(String password, String name) throws IOException;

	boolean setUserActive(boolean stat) throws IOException;

	boolean grandUserAdminRole(boolean admin) throws IOException;
}