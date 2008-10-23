package org.lindenb.wikipedia.api;

import java.util.Date;

public class Revision
	{
	private Entry entry;
	private Date date;
	private User user;
	private int size;
	public Revision(Entry entry,Date date,User user,int size)
		{
		this.entry=entry;
		this.date=date;
		this.user=user;
		this.size=size;
		}
	
	public Date getDate()
		{
		return this.date;
		}
	public Entry getEntry()
		{
		return this.entry;
		}
	public User getUser()
		{
		return this.user;
		}

	public int getSize()
		{
		return this.size;
		}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getDate().hashCode();
		result = prime * result + getEntry().hashCode();
		result = prime * result + getUser().hashCode();
		return result;
		}

	@Override
	public boolean equals(Object obj)
		{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Revision other= Revision.class.cast(obj);
		return 	getDate().equals(other.getDate()) &&
				getEntry().equals(other.getEntry()) &&
				getUser().equals(other.getUser())
				
				;
		}
	
	@Override
	public String toString() {
		return "{entry:"+getEntry()+
			",date:"+getDate()+
			",user:"+getUser()+
			",size:"+getSize()+
			"}"
			;
		}
	
	}
