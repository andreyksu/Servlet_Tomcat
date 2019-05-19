package ru.andreyksu.annikonenkov.webapp.commonParameters;

public enum ParametersOfRequest {
	Authorization("authorization"), Registration("registration"), LogOut("logout"), Message("messaging");

	private String _param;

	ParametersOfRequest(String param) {
		_param = param;
	}

	public String getParameter() {
		return _param;
	}
}
