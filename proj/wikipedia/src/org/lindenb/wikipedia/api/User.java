package org.lindenb.wikipedia.api;

public class User
extends Entry
	{
	public User(String pageName)
		{
		super(pageName);
		}
	
	@Override
	public MWNamespace getNamespace() {
		return MWNamespace.User;
		}
	}
