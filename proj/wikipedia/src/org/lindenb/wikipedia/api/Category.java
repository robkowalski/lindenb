package org.lindenb.wikipedia.api;

public class Category
extends Entry
	{
	public Category(String pageName)
		{
		super(pageName);
		}
	
	@Override
	public MWNamespace getNamespace() {
		return MWNamespace.Category;
		}
	}
