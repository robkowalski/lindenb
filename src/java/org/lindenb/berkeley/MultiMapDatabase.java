package org.lindenb.berkeley;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;

public class MultiMapDatabase<K,V>
	extends AbstractDatabase<K,V> {

	public MultiMapDatabase(
			Database database,
			TupleBinding<K> keyBinding,
			TupleBinding<V> valueBinding) {
		super(database, keyBinding, valueBinding);
		}

}
