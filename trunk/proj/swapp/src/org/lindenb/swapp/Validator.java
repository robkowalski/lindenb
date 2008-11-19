package org.lindenb.swapp;

public interface Validator<T extends RDFEditor>
	{
	public String getMessage(T editor);
	}
