package org.lindenb.util;



/**
 * 
 * Date
 *
 */
public class Date extends XObject implements Comparable<Date>
	{
	private Long year= null;
	private Integer month =null;
	private Integer dayOfMonth =null;
	
	public Date()
		{
		this(java.util.Calendar.getInstance());
		}
	
	

	public Date(String s)
		{
		s=s.trim();
		long sign=1;
		if(s.startsWith("-"))
			{
			sign=-1;
			s=s.substring(1);
			}
		String tokens[]=s.split("[\\-]");
		switch(tokens.length)
			{
			case 3: dayOfMonth= new Integer(tokens[2]);//threw
				if(!(dayOfMonth>0 && dayOfMonth<32))
					{
					throw new IllegalArgumentException("Bad day of month: \""+s+"\"");
					}
			case 2: month= new Integer(tokens[1]);//threw
				if(!(month>0 && month<13))
					{
					throw new IllegalArgumentException("Bad month: \""+s+"\"");
					}
			case 1: year=  sign* Long.parseLong(tokens[0]);break;
			default: throw new IllegalArgumentException("Bad date: \""+s+"\"");
			}
		if(!(month>0 && month<13) || !(dayOfMonth>0 && dayOfMonth<32))
				{
				throw new IllegalArgumentException("Bad date: \""+s+"\"");
				}
		}
	
	public Date(long Y)
		{
		year=Y;month=null;dayOfMonth=null;
		}
	
	public Date(long Y,int M)
		{
		year=Y;month=M;dayOfMonth=null;
		}
	public Date(long Y,int M,int D)
		{
		year=Y;month=M;dayOfMonth=D;
		}
	
	public Date(java.util.Date date)
		{
		java.util.Calendar c=java.util.Calendar.getInstance();
		c.setTime(date);
		year= new Long(c.get(java.util.Calendar.YEAR));
		month= c.get(java.util.Calendar.MONTH)+1;
		dayOfMonth= c.get(java.util.Calendar.DAY_OF_MONTH);
		}
	
	
	public Date(java.util.Calendar c)
		{
		year= new Long(c.get(java.util.Calendar.YEAR));
		month= c.get(java.util.Calendar.MONTH)+1;
		dayOfMonth= c.get(java.util.Calendar.DAY_OF_MONTH);
		}
	
	public Long getYear() {
		return year;
		}
	
	public Integer getMonth() {
		return month;
		}
	
	public Integer getDayOfMonth() {
		return dayOfMonth;
		}
	
	public int compareTo(Date o) {
		if(o==this) return 0;
		int i= year.compareTo(o.year);
		if(i!=0) return i;
		if(this.month==null && o.month==null) return 0;
		if(this.month==null && o.month!=null) return -1;
		if(this.month!=null && o.month==null) return 1;
		i= month.compareTo(o.month);
		if(i!=0) return i;
		
		if(this.dayOfMonth==null && o.dayOfMonth==null) return 0;
		if(this.dayOfMonth==null && o.dayOfMonth!=null) return -1;
		if(this.dayOfMonth!=null && o.dayOfMonth==null) return 1;
		return dayOfMonth.compareTo(o.dayOfMonth);
		}
	
	@Override
	public boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null || !(obj instanceof Date)) return false;
		return compareTo(Date.class.cast(obj))==0;
		}
	
	@Override
	public int hashCode() {
		return year.hashCode();
		}
	
	public String toString(Character delim)
		{
		String s= (delim==null?"":delim.toString());
		return year+(month==null?"":s+(month<10?"0"+month:month)+(dayOfMonth==null?"":s+(dayOfMonth<10?"0"+dayOfMonth:dayOfMonth)));
		}
	
	@Override
	public String toString() {
		return toString('-');
		}
	
	public double toDouble()
		{
		double n=year.doubleValue();
		if(month!=null)
			{
			n+=month.doubleValue()/12.0;
			if(dayOfMonth!=null) n+=dayOfMonth.doubleValue()/365.0;
			}
		return n;
		}
	
}
