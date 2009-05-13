package org.lindenb.sw.nodes;

import java.net.URISyntaxException;

public class Resource
	extends RDFNode
	implements Comparable<Resource>
	{
	private static final long serialVersionUID = 1L;
	private String uri;
	private boolean anonymous;
	public Resource(String s) throws URISyntaxException
		{
		this(s,false);
		}
	
	public Resource(String uri,boolean anonymous) throws URISyntaxException
		{
		if(uri==null) throw new NullPointerException("null uri");
		this.uri=uri;
		if(uri.startsWith("_:")) anonymous=true;
		this.anonymous=anonymous;
		if(!anonymous)
			{
			try
				{
				new java.net.URI(uri);
				}
			catch(URISyntaxException err)
				{
				throw new IllegalArgumentException("Not a valid URI:"+uri);
				}
			}
		}
	
	public String getURI() {
		return uri;
		}
	
	@Override
	public int hashCode()
		{
		return uri.hashCode();
		}
	/** is it an anonymous resource */
	public boolean isAnonymous()
		{
		return anonymous;
		}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		return getURI().equals(other.getURI());
		}
	
	@Override
	public int compareTo(Resource o) {
		return this.getURI().compareTo(o.getURI());
		}
	
	@Override
	public String toString() {
		return getURI();
		}
	
	@Override
	public boolean isResource()
		{
		return true;
		}
	
	@Override
	public boolean isLiteral() {
		return false;
		}
	
	@Override
	public String asN3()
		{
		return isAnonymous()?getURI():"<"+getURI()+">";
		}
	
	}
