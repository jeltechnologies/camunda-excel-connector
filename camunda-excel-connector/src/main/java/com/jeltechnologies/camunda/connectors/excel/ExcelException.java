package com.jeltechnologies.camunda.connectors.excel;

public class ExcelException extends Exception {
	private static final long serialVersionUID = -15521519202765431L;

	public ExcelException(String message) {
		super(message);
	}

	public ExcelException(String message, Throwable cause) {
		super(message, cause);
	}
}
