package org.lindenb.swapp;

import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * ComboRDFEditor
 * @author pierre
 */
public class ComboRDFEditor extends RDFEditor
	{
	private JComboBox combo=null;
	private boolean required=false;
	
	/**
	 * ComboRDFEditor
	 */
	public ComboRDFEditor()
		{
		this.combo= new JComboBox(new DefaultComboBoxModel());
		this.combo.setRenderer(createListCellRenderer());
		}
	
	public void setComboValues(Collection<Resource> list)
		{
		DefaultComboBoxModel m=new DefaultComboBoxModel();
		m.addElement(null);
		for(Resource r:list) m.addElement(r);
		getComboBox().setModel(m);
		}
	
	public void setComboValues(Resource...list)
		{
		Set<Resource> set= new HashSet<Resource>();
		for(Resource r:list) set.add(r);
		setComboValues(set);
		}
	
	/** create a list cell renderer for this combo */
	protected ListCellRenderer createListCellRenderer()
		{
		return new DefaultListCellRenderer()
			{
			private static final long serialVersionUID = 1L;
	
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus)
				{
				if(value!=null && (value instanceof Resource))
					{
					String title= getSchema().getTitle(Resource.class.cast(value));
					if(title!=null) value=title;
					}
				Component c= super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				return c;
				}
			
			};
		}
	
	@Override
	public JComponent getComponent()
		{
		return getComboBox();
		}

	private JComboBox getComboBox()
		{
		return this.combo;
		}
	
	/** get all the possible values in that combo */
	public Set<Resource> getComboValues()
		{
		Set<Resource> set= new HashSet<Resource>();
		ComboBoxModel comboModel = getComboBox().getModel();
		for(int i=0;i< comboModel.getSize();++i)
			{
			 Resource k=Resource.class.cast(comboModel.getElementAt(i));
             if(k==null) continue;
             set.add(k);
			}
		return set;
		}
	
	@Override
	public String getValidationMessage()
		{
		if(isEmpty() && isRequired())
			{
			return getSchema().lang("VALUE_REQUIRED");
			}
		return null;
		}

	public void setRequired(boolean required)
		{
		this.required = required;
		}
	
	public boolean isRequired() {
		return required;
		}
	
	@Override
	public boolean isEmpty() {
		return  getComboBox().getSelectedIndex()==-1 ||
				getComboBox().getSelectedItem()==null
				;
		}

	@Override
	public void loadFromModel()
		{
		Set<Resource> possible = getComboValues();
        getComboBox().setSelectedIndex(-1);
        NodeIterator iter= getModel().listObjectsOfProperty(
        		getSubject(),
        		getProperty()
        		);
        while(iter.hasNext())
                {
                RDFNode node = iter.nextNode();
                if(!node.isResource()) continue;
                if(node.isAnon()) continue;
                if(!possible.contains(node)) continue;
                getComboBox().setSelectedItem(node);
                break;
                }
        iter.close();
		}

	@Override
	public void saveToModel()
			{
			Resource s= Resource.class.cast(getComboBox(). getSelectedItem());
    		removeAll(getSubject(), getProperty(),null);
            if(s!=null) getModel().add(
                    getSubject(),
                    getProperty(),
                    s
                    );
			}

}
