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


public static String toYYYYMMDDHHMMSS(GregorianCalendar cal,Character delimiter)
	{
	String delim=delimiter==null?"":delimiter.toString();
	return 	toYYYYMMDD(cal,delimiter)+delim+
		(cal.get(GregorianCalendar.HOUR)<10?"0":"")+(cal.get(GregorianCalendar.HOUR))+delim+
		(cal.get(GregorianCalendar.MINUTE)<10?"0":"")+(cal.get(GregorianCalendar.MINUTE))+delim+
		(cal.get(GregorianCalendar.SECOND)<10?"0":"")+(cal.get(GregorianCalendar.SECOND))
		;
	}

public static String toYYYYMMDD(Character delimiter)
	{
	return toYYYYMMDD(new GregorianCalendar(),delimiter);
	}

public static String toYYYYMMDDHHMMSS(Character delimiter)
	{
	return toYYYYMMDDHHMMSS(new GregorianCalendar(),delimiter);
	}

public static String toYYYYMMDDHHMMSS()
	{
	return toYYYYMMDDHHMMSS(null);
	}

}
