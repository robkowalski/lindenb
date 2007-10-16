/**
 * 
 */
package org.lindenb.util.iterator;

import java.util.Iterator;

/**
 * @author pierre
 *
 */
public interface CloseableIterator<T> extends Iterator<T> {
public void close();
}
