package org.lindenb.bio;

/**
 * A CharSequence implementing a reverse complement sequence
 * @author lindenb
 *
 */
public class ReverseComplement
	implements CharSequence
	{
	private CharSequence delegate;
	
	public ReverseComplement(CharSequence delegate)
		{
		this.delegate=delegate;
		}
	
	protected CharSequence getDelegate()
		{
		return this.delegate;
		}
	
	@Override
	public int length() {
		return getDelegate().length();
		}
	
	@Override
	public int hashCode() {
		return getDelegate().hashCode();
		}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==this) return true;
		if(obj ==null || !(obj instanceof ReverseComplement)) return false;
		return this.getDelegate().equals(ReverseComplement.class.cast(obj).getDelegate());
		}
	
	@Override
	public CharSequence subSequence(int start, int end)
		{
		StringBuilder b=new StringBuilder(end-start);
		while(start<end) b.append(charAt(start++));
		return b;
		}
	
	@Override
	public char charAt(int index)
		{
		return NucleotideUtils.complement(getDelegate().charAt((length()-1)-index));
		}
	
	@Override
	public String toString()
		{
		return NucleotideUtils.reverseComplement(getDelegate());
		}
	
	@Override
	protected Object clone()
		{
		return new ReverseComplement(getDelegate());
		}
	}
