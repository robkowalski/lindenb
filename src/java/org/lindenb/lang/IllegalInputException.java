package org.lindenb.lang;


import java.io.IOException;


public class IllegalInputException extends IOException
	{
	private static final long serialVersionUID = 1L;

	public IllegalInputException() {
		this("Illegal Input");
	}

	public IllegalInputException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalInputException(String message) {
		super(message);
	}

	public IllegalInputException(Throwable cause) {
		super("Illegal Input",cause);
	}

	
	

}
