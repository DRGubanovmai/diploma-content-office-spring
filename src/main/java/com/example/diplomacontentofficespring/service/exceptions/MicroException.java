package com.example.diplomacontentofficespring.service.exceptions;

/**
 * Общий класс исключения без привязки к конкретной проблеме.
 *
 * @author Aksenov Ivan
 * @since 0.0.1
 */
public class MicroException extends Exception {

	@SuppressWarnings("CdiInjectionPointsInspection")
	public MicroException(String message, Object... params) {
		super(String.format(message, params));
	}

}
