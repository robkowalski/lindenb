package org.lindenb.lang;




public class IllegalTokenCount extends IllegalInputException
	{
	private static final long serialVersionUID = 1L;

	public IllegalTokenCount(int expected,String tokens[])
		{
		super(buildMessage(expected,tokens));
		}

	public IllegalTokenCount(int expected,int found)
		{
		super("Expected "+expected+" tokens but found "+found);
		}
	
	static private String buildMessage(int expected,String tokens[])
		{
		StringBuilder b= new StringBuilder("Expected "+expected+" tokens but found "+tokens.length+"\n");
		for(String s:tokens)
			{
			b.append(s).append("\n");
			}
		b.append("\n");
		return b.toString();
		}

	

}
