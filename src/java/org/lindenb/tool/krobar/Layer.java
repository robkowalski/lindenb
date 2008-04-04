package org.lindenb.tool.krobar;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.util.Iterator;
import java.util.Vector;

public class Layer
	implements Iterable<Figure>
	{
	private float alpha=1f;
	private boolean visible=true;
	private Model model;
	private Vector<Figure> figures_=new Vector<Figure>();
	public Layer(Model model)
		{
		this.model=model;
		}
	
	public Vector<Figure> figures()
		{
		return this.figures_;
		}
	
	@Override
	public Iterator<Figure> iterator() {
		return figures().iterator();
		}
	
	public Model getModel()
		{
		return model;
		}
	
	public boolean isVisible() {
		return visible;
		}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		}
	
	public float getOpacity()
		{
		return alpha;
		}

	public Composite getComposite()
		{
		return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getOpacity());
		}

	public void setOpacity(float alpha) {
		this.alpha = alpha;
		}
	}
