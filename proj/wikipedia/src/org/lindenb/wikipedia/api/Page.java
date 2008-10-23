package org.lindenb.wikipedia.api;

public class Page
extends Entry
	{
	public Page(String pageName)
		{
		super(pageName);
		}
	//no prefix for Main NS
	@Override
	public String getQName() {
		return getLocalName();
		}
	@Override
	public MWNamespace getNamespace() {
		return MWNamespace.Main;
		}
	}
