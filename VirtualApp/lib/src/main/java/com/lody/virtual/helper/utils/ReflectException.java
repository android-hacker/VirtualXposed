package com.lody.virtual.helper.utils;

/**
 * @author Lody
 */
public class ReflectException extends RuntimeException {

	private static final long serialVersionUID = 663038727503637969L;

	public ReflectException(String message) {
		super(message);
	}

	public ReflectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectException() {
		super();
	}

	public ReflectException(Throwable cause) {
		super(cause);
	}
}
