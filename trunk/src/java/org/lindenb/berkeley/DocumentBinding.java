/**
 * 
 */
package org.lindenb.berkeley;

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
	
	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#entryToObject(com.sleepycat.bind.tuple.TupleInput)
	 */
	@Override
	public Document entryToObject(TupleInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sleepycat.bind.tuple.TupleBinding#objectToEntry(java.lang.Object, com.sleepycat.bind.tuple.TupleOutput)
	 */
	@Override
	public void objectToEntry(Document object, TupleOutput output) {
		// TODO Auto-generated method stub

	}

}
