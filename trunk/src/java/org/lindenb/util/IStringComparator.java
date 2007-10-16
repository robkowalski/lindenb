/**
 * 
 */
package org.lindenb.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Simple case insensible String comparator. Uses a java.text.Collator
 * @author lindenb
 *
 */
public class IStringComparator implements Comparator<String>
	{
	private Collator collator;
	
	/** construct with default locale */
	public IStringComparator()
		{
		this(Locale.getDefault());
		}
	
	/** construct with custom locale */
	public IStringComparator(Locale locale)
		{
		this.collator= Collator.getInstance(locale);
		this.collator.setStrength(Collator.PRIMARY);
		}
	
	protected Collator getCollator() {
		return collator;
		}
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(String o1, String o2)
		{
		return getCollator().compare(o1,o2);
		}

	
	}
