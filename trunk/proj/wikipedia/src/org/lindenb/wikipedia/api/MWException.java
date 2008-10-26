/**
 * 
 */
package org.lindenb.wikipedia.api;

/**
 * @author pierre
 *
 */
public class MWException extends Exception
	{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public MWException() {
		
	}

	/**
	 * @param msg
	 */
	public MWException(String msg) {
		super(msg);

	}

	/**
	 * @param msg
	 */
	public MWException(Throwable msg) {
		super(msg);

	}

	/**
	 * @param msg
	 * @param arg1
	 */
	public MWException(String msg, Throwable arg1) {
		super(msg, arg1);

	}

}
