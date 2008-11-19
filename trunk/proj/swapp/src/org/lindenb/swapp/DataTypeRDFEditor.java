package org.lindenb.swapp;


import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class DataTypeRDFEditor
	extends AbstractTextRDFEditor
	{
	private static class XSDEditor
		extends DataTypeRDFEditor
		{
		protected XSDEditor(com.hp.hpl.jena.datatypes.xsd.XSDDatatype dataType) { super(dataType);}
		}
	
	private static abstract class NumericEditor
	extends XSDEditor
		{
		private Number minValue;
		private Number maxValue;
		protected NumericEditor(com.hp.hpl.jena.datatypes.xsd.XSDDatatype dataType)
			{
			super(dataType);
			}
		public void setMinValue(Number minValue) {
			this.minValue = minValue;
			}
		
		public void setMaxValue(Number maxValue) {
			this.maxValue = maxValue;
			}
		
		public Number getMinValue() {
			return minValue;
			}
		
		public Number getMaxValue() {
			return maxValue;
			}
		
		@Override
		public String getValidationMessage() {
			String msg= super.getValidationMessage();
			if(msg!=null) return msg;
			if(getMinValue()==null && getMaxValue()==null) return null;
			Object o= getValue();
			if(o==null) throw new RuntimeException("??? was valid but null now ???");
			if(getMinValue()!=null && compare(getMinValue(),Number.class.cast(o))>0)
				{
				return getSchema().lang("TOO_LOW");
				}
			if(getMaxValue()!=null && compare(getMaxValue(),Number.class.cast(o))<=0)
				{
				return getSchema().lang("TOO_LARGE");
				}
			return null;
			}
		protected abstract int compare(Number a,Number b);
		}
	
	
	private static class IntegerEditor
		extends NumericEditor
		{
		protected IntegerEditor(com.hp.hpl.jena.datatypes.xsd.XSDDatatype dataType)
			{
			super(dataType);
			}
		@Override
		protected int compare(Number arg0, Number arg1) {
			return new BigInteger(arg0.toString()).compareTo(new BigInteger(arg1.toString()));
			}
		}
	
	private static class DecimalEditor
	extends NumericEditor
		{
		protected DecimalEditor(com.hp.hpl.jena.datatypes.xsd.XSDDatatype dataType)
			{
			super(dataType);
			}
		@Override
		protected int compare(Number arg0, Number arg1) {
			return new BigDecimal(arg0.toString()).compareTo(new BigDecimal(arg1.toString()));
			}
		}
	
	/** implementation for XSDfloat */
	public static class XSDfloat extends DecimalEditor { public XSDfloat() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDfloat); } }
	/** implementation for XSDdouble */
	public static class XSDdouble extends DecimalEditor { public XSDdouble() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdouble); } }
	/** implementation for XSDint */
	public static class XSDint extends IntegerEditor { public XSDint() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDint); } }
	/** implementation for XSDlong */
	public static class XSDlong extends IntegerEditor { public XSDlong() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDlong); } }
	/** implementation for XSDshort */
	public static class XSDshort extends IntegerEditor { public XSDshort() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDshort); } }
	/** implementation for XSDbyte */
	public static class XSDbyte extends XSDEditor { public XSDbyte() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDbyte); } }
	/** implementation for XSDunsignedByte */
	public static class XSDunsignedByte extends XSDEditor { public XSDunsignedByte() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDunsignedByte); } }
	/** implementation for XSDunsignedShort */
	public static class XSDunsignedShort extends IntegerEditor { public XSDunsignedShort() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDunsignedShort); } }
	/** implementation for XSDunsignedInt */
	public static class XSDunsignedInt extends IntegerEditor { public XSDunsignedInt() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDunsignedInt); } }
	/** implementation for XSDunsignedLong */
	public static class XSDunsignedLong extends IntegerEditor { public XSDunsignedLong() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDunsignedLong); } }
	/** implementation for XSDdecimal */
	public static class XSDdecimal extends DecimalEditor { public XSDdecimal() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdecimal); } }
	/** implementation for XSDinteger */
	public static class XSDinteger extends IntegerEditor { public XSDinteger() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDinteger); } }
	/** implementation for XSDnonPositiveInteger */
	public static class XSDnonPositiveInteger extends IntegerEditor { public XSDnonPositiveInteger() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDnonPositiveInteger); } }
	/** implementation for XSDnonNegativeInteger */
	public static class XSDnonNegativeInteger extends IntegerEditor { public XSDnonNegativeInteger() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDnonNegativeInteger); } }
	/** implementation for XSDpositiveInteger */
	public static class XSDpositiveInteger extends IntegerEditor { public XSDpositiveInteger() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDpositiveInteger); } }
	/** implementation for XSDnegativeInteger */
	public static class XSDnegativeInteger extends IntegerEditor { public XSDnegativeInteger() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDnegativeInteger); } }
	/** implementation for XSDboolean */
	public static class XSDboolean extends XSDEditor { public XSDboolean() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDboolean); } }
	/** implementation for XSDstring */
	public static class XSDstring extends XSDEditor { public XSDstring() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDstring); } }
	/** implementation for XSDnormalizedString */
	public static class XSDnormalizedString extends XSDEditor { public XSDnormalizedString() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDnormalizedString); } }
	/** implementation for XSDanyURI */
	public static class XSDanyURI extends XSDEditor { public XSDanyURI() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDanyURI); } }
	/** implementation for XSDtoken */
	public static class XSDtoken extends XSDEditor { public XSDtoken() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDtoken); } }
	/** implementation for XSDName */
	public static class XSDName extends XSDEditor { public XSDName() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDName); } }
	/** implementation for XSDQName */
	public static class XSDQName extends XSDEditor { public XSDQName() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDQName); } }
	/** implementation for XSDlanguage */
	public static class XSDlanguage extends XSDEditor { public XSDlanguage() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDlanguage); } }
	/** implementation for XSDNMTOKEN */
	public static class XSDNMTOKEN extends XSDEditor { public XSDNMTOKEN() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDNMTOKEN); } }
	/** implementation for XSDENTITY */
	public static class XSDENTITY extends XSDEditor { public XSDENTITY() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDENTITY); } }
	/** implementation for XSDID */
	public static class XSDID extends XSDEditor { public XSDID() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDID); } }
	/** implementation for XSDNCName */
	public static class XSDNCName extends XSDEditor { public XSDNCName() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDNCName); } }
	/** implementation for XSDIDREF */
	public static class XSDIDREF extends XSDEditor { public XSDIDREF() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDIDREF); } }
	/** implementation for XSDNOTATION */
	public static class XSDNOTATION extends XSDEditor { public XSDNOTATION() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDNOTATION); } }
	/** implementation for XSDhexBinary */
	public static class XSDhexBinary extends XSDEditor { public XSDhexBinary() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDhexBinary); } }
	/** implementation for XSDbase64Binary */
	public static class XSDbase64Binary extends XSDEditor { public XSDbase64Binary() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDbase64Binary); } }
	/** implementation for XSDdate */
	public static class XSDdate extends XSDEditor { public XSDdate() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdate); } }
	/** implementation for XSDtime */
	public static class XSDtime extends XSDEditor { public XSDtime() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDtime); } }
	/** implementation for XSDdateTime */
	public static class XSDdateTime extends XSDEditor { public XSDdateTime() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDdateTime); } }
	/** implementation for XSDduration */
	public static class XSDduration extends XSDEditor { public XSDduration() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDduration); } }
	/** implementation for XSDgDay */
	public static class XSDgDay extends XSDEditor { public XSDgDay() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgDay); } }
	/** implementation for XSDgMonth */
	public static class XSDgMonth extends XSDEditor { public XSDgMonth() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgMonth); } }
	/** implementation for XSDgYear */
	public static class XSDgYear extends XSDEditor { public XSDgYear() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgYear); } }
	/** implementation for XSDgYearMonth */
	public static class XSDgYearMonth extends XSDEditor { public XSDgYearMonth() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgYearMonth); } }
	/** implementation for XSDgMonthDay */
	public static class XSDgMonthDay extends XSDEditor { public XSDgMonthDay() {super(com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDgMonthDay); } }

	
	private RDFDatatype dataType;
	protected DataTypeRDFEditor(RDFDatatype dataType)
		{
		this.dataType=dataType;
		}
	
	public RDFDatatype getDataType()
		{
		return this.dataType;
		}
	
	public Object getValue()
		{
		String s=getTextField().getText().trim();
		if(!getDataType().isValid(s)) return null;
		try {
			return getDataType().parse(s);
		} catch (DatatypeFormatException  e) {
			return null;
			}
		}
	
	
    @Override
    public String getValidationMessage()
		{
    	String s= super.getValidationMessage();
    	if(s!=null) return s;
    	if(isEmpty())
    		{
    		if(isRequired())
    			{
    			return getSchema().lang("VALUE_REQUIRED");
    			}
    		}
    	else if(!getDataType().isValid(getTextField().getText().trim()))
        	{
        	return getSchema().lang("ILLEGAL_VALUE");
            }
        return null;
        }
    
    
    
    private Literal validLiteral(RDFNode node)
    	{
    	if(node==null || !node.isLiteral()) return null;
         Literal L= Literal.class.cast(node);
         if( L.getDatatypeURI()==null ||
         	!L.getDatatypeURI().equals(getDataType().getURI())) return null;
        return L;
    	}
    
    @Override
    public void loadFromModel()
            {
    		getTextField().setText("");
            NodeIterator iter= getModel().listObjectsOfProperty(getSubject(),getProperty());
            while(iter.hasNext())
                    {
                    RDFNode node= iter.nextNode();
                    Literal L= validLiteral(node);
                    if(L==null) continue;
                    getTextField().setText(L.getLexicalForm());
                    break;
                    }
            iter.close();
            getTextField().setCaretPosition(0);
            }

    @Override
    public void saveToModel()
    		{
            String s= getTextField().getText().trim();
            StmtIterator iter= getModel().listStatements(getSubject(), getProperty(),(RDFNode)null);
            while(iter.hasNext())
	            {
	            Statement stmt= iter.nextStatement();
	            if(validLiteral(stmt.getObject())==null) continue;
	            stmt.remove();
	            }
            
            
            if(s.length()!=0) getModel().add(
                            getSubject(),
                            getProperty(),
                            getModel().createTypedLiteral(s, getDataType())
                            );
            }
	}
