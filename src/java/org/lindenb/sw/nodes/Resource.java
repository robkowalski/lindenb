package org.lindenb.sw.nodes;

import java.net.URISyntaxException;
import java.util.Random;

import org.lindenb.util.TimeUtils;

public class Resource
	extends RDFNode
	implements Comparable<Resource>
	{
	private static long ID_GENERATOR=System.currentTimeMillis();
	private static Random RANDOM=new Random();
	private static final long serialVersionUID = 1L;
	private String uri;
	private boolean anonymous;
	
	/**
	 * An anonymous Node
	 */
	public Resource()
		{
		this("_:"+TimeUtils.toYYYYMMDDHHMMSS()+"-"+
				(++ID_GENERATOR)+"-"+RANDOM.nextInt()
			,true);
		}
	
	/**
	 * Copy Constructor for derived classes
	 */
	public Resource(Resource cp)
		{
		this.uri=cp.uri;
		this.anonymous=cp.anonymous;
		}
	
	/** new resource with the given uri/localName */
	public Resource(String uri,String localName)
		{
		this(uri+localName,false);
		}
	
	/** new resource with the given uri */
	public Resource(String uri)
		{
		this(uri,false);
		}
	
	public Resource(String uri,boolean anonymous)
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
		if (obj == null || !(obj instanceof Resource))
			return false;
		Resource other = (Resource) obj;
		return getURI().equals(other.getURI());
		}
	
	@Override
	public int compareTo(Resource o) {
		return this.getURI().compareTo(o.getURI());
		}
	
	@Override
	protected Object clone()
		{
		return new Resource(this);
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
