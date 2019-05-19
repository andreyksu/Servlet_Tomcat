package ru.andreyksu.annikonenkov.webapp.userrepresent.user;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IUser {

    boolean fillUserFromRequest(ResultSet result) throws SQLException;

    boolean isAdmin();

    boolean isActive();

    String getPassword();

    String getName();

    String getEmail();

    String getRegistrateData();

}
