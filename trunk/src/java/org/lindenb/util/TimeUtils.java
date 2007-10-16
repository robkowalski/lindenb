package org.lindenb.util;

import java.util.GregorianCalendar;

public class TimeUtils
{
	
public static String toYYYYMMDD(GregorianCalendar cal)
	{
	return 	toYYYYMMDD(cal,'-');
	}

public static String toYYYYMMDD()
	{
	return toYYYYMMDD(new GregorianCalendar());
	}

public static String toYYYYMMDD(GregorianCalendar cal,Character delimiter)
	{
	String delim=delimiter==null?"":delimiter.toString();
	return 	cal.get(GregorianCalendar.YEAR)+delim+
			(cal.get(GregorianCalendar.MONTH)>8?"":"0")+(cal.get(GregorianCalendar.MONTH)+1)+delim+
			(cal.get(GregorianCalendar.DAY_OF_MONTH)>8?"":"0")+(cal.get(GregorianCalendar.DAY_OF_MONTH))
			;
	}

public static String toYYYYMMDD(Character delimiter)
	{
	return toYYYYMMDD(new GregorianCalendar(),delimiter);
	}


}
