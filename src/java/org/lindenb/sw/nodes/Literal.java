package org.lindenb.sw.nodes;

import org.lindenb.util.C;

public class Literal extends RDFNode
	implements Comparable<Literal>
	{
	private static final long serialVersionUID = 1L;
	private String text;
	private String dataType;
	private String lang;
	
	public Literal(String text)
		{
		this(text,null,null);
		}
	
	public Literal(String text,String dataType,String lang)
		{
		if(text==null) throw new NullPointerException("text is null");
		this.text=text;
		this.dataType=dataType;
		this.lang=lang;
		if(this.lang!=null && this.dataType!=null)
			{
			throw new IllegalArgumentException("Literal cannot be typed AND localized");
			}
		}
	
	
	
	@Override
	public int hashCode()
		{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((lang == null) ? 0 : lang.hashCode());
		return result;
		}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Literal other = (Literal) obj;
		if(!this.text.equals(other.text)) return false;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (lang == null) {
			if (other.lang != null)
				return false;
		} else if (!lang.equals(other.lang))
			return false;
		return true;
		}

	@Override
	public int compareTo(Literal other) {
		if(other==this) return 0;
		if(other==null) return 1;
		int i= getLexicalForm().compareTo(other.getLexicalForm());
		if(i!=0) return i;
		
		if (getDatatypeURI() == null)
			{
			if (other.getDatatypeURI() != null)
				return -1;
			}
		else if ((i=getDatatypeURI().compareTo(other.getDatatypeURI()))!=0)
			{
			return i;
			}
		if (getLanguage() == null)
			{
			if (other.getLanguage() != null)
				return -1;
			}
		else if ((i=getLanguage().compareTo(other.getLanguage()))!=0)
			{
			return i;
			}
		return 0;
		}
	
	
	@Override
	public boolean isResource()
		{
		return false;
		}
	
	@Override
	public boolean isLiteral() {
		return true;
		}
	
	public String getLanguage() {
		return lang;
		}
	
	public boolean isPlainLiteral()
		{
		return getDatatypeURI()==null;
		}
	
	public String getDatatypeURI() {
		return dataType;
		}
	
	public String getLexicalForm()
		{
		return text;
		}

	@Override
	public String toString()
		{
		return getLexicalForm();
		}
	
	@Override
	public String asN3()
		{
		StringBuilder b= new StringBuilder();
		b.append("\"").append(C.escape(getLexicalForm())).append("\"");
		if(getDatatypeURI()!=null)
			{
			b.append("^^<").append(getDatatypeURI()).append("> ");
			}
		else if(getLanguage()!=null)
			{
			b.append("@").append(getLanguage());
			}
		return b.toString();
		}

	
	}
