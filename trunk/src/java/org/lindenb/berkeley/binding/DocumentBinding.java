/**
 * 
 */
package org.lindenb.berkeley.binding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.lindenb.xml.DocumentSerializer;
import org.w3c.dom.Document;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author lindenb
 * Serialization of a DOM
 */
public class DocumentBinding extends TupleBinding<Document>
	{
	private DocumentBuilder docBuilder=null;
	private DocumentSerializer serializer=new DocumentSerializer();
	
	public DocumentBinding()
		{
		try {
			DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
			f.setCoalescing(true);
			f.setNamespaceAware(true);
			f.setValidating(false);
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(false);
			f.setIgnoringElementContentWhitespace(false);
			this.docBuilder= f.newDocumentBuilder();
			} 
		catch (ParserConfigurationException e)
			{
			throw new RuntimeException(e);
			}
		}
	
	public DocumentBinding(DocumentBuilder docBuilder)
		{
		this.docBuilder=docBuilder;
		}
	
	public DocumentBuilder getDocumentBuilder() {
		return docBuilder;
		}
	
	
	
	/**
	 * serialize TupleInput to Document
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	@Override
	public Document entryToObject(TupleInput input)
		{
		ByteArrayInputStream in=new ByteArrayInputStream(
				input.getBufferBytes(),
				input.getBufferOffset(),
				input.getBufferLength()
				);
		try
			{
			return serializer.readDocument(getDocumentBuilder(),in);
			}
		catch(IOException err)
			{
			throw new RuntimeException(err);
			}
		}

	/**
	 * Serialize Document to TupleInput
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	@Override
	public void objectToEntry(Document object, TupleOutput output)
		{
		try
			{
			ByteArrayOutputStream out= new ByteArrayOutputStream();
			serializer.writeDocument(object, out);
			byte bytes[]=out.toByteArray();
			output.write(bytes);
			}
		catch(IOException err)
			{
			throw new RuntimeException(err);
			}

	}

}
