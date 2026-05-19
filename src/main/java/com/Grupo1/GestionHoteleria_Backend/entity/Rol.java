package com.Grupo1.GestionHoteleria_Backend.entity;

public enum Rol {
	ADMIN,
	CLIENTE;

	private static final String ROLE_PREFIX = "ROLE_";

	public String getAuthority() {
		return ROLE_PREFIX + name();
	}
}
