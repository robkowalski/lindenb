package org.lindenb.util.iterator;

import java.util.Iterator;

/**
 * @author pierre
 * An iterator that should be closed
 */
public interface CloseableIterator<T> extends Iterator<T> {
public void close();
}
