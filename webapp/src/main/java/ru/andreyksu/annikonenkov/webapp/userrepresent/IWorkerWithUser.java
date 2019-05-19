package ru.andreyksu.annikonenkov.webapp.userrepresent;

import java.io.IOException;
import java.util.List;

import ru.andreyksu.annikonenkov.webapp.userrepresent.user.IUser;

public interface IWorkerWithUser {

    public IUser getUserByEmail(String email) throws IOException;

    List<String> getListOfUsers(boolean activity) throws IOException;

    IUser registrateUser(String email, String password, String name) throws IOException;

    boolean setUserActive(String email, boolean stat) throws IOException;

    boolean grandUserAdminRole(String email, boolean admin) throws IOException;
}