package org.lindenb.treemap;

import java.awt.geom.Rectangle2D;

/**
 * TreeMap
 * @author lindenb
 *
 */
public interface TreeMap {
public void setBounds(Rectangle2D bounds);
public Rectangle2D getBounds();
public double getWeight();
}
