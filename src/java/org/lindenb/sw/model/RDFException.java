/**
 * 
 */
package org.lindenb.sw.model;

/**
 * @author pierre
 *
 */
public class RDFException extends Exception
	{
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public RDFException()
		{
		this("RDFException");
		}

	/**
	 * @param message
	 */
	public RDFException(String message) {
		super(message);
		}

	/**
	 * @param cause
	 */
	public RDFException(Throwable cause) {
		this(cause.getMessage()==null?"RDFException":cause.getMessage(),cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RDFException(String message, Throwable cause) {
		super(message, cause);
		}

}
