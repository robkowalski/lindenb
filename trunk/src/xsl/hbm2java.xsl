<?xml version='1.0'  encoding="ISO-8859-1" ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<xsl:output method='xml' indent="no"/>

<xsl:template match="/">
<archive>
<xsl:apply-templates select="hibernate-mapping"/>
</archive>
</xsl:template>

<xsl:template match="hibernate-mapping">
<xsl:apply-templates select="class" mode="interface"/>
<xsl:apply-templates select="class" mode="class"/>
<xsl:apply-templates select="class" mode="tableModel"/>
<xsl:apply-templates select="class" mode="editorPane"/>
</xsl:template>

<xsl:template match="class" mode="interface">
<xsl:variable name="package">
 <xsl:call-template name="class2package">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:variable name="className">
 <xsl:call-template name="class2className">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:element name="file">
<xsl:attribute name="path"><xsl:value-of select="concat(&apos;src/&apos;,translate($package,&apos;.&apos;,&apos;/&apos;),&apos;/&apos;,$className,&apos;.java&apos;)"/></xsl:attribute>
	

/**
 * <xsl:value-of select="$className"/>
 */
public interface <xsl:value-of select="$className"/>
	{
	<xsl:for-each select="id|property">
	
	<xsl:variable name="pptyName">
		<xsl:call-template name="propertyName">
		  <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:variable name="pptyClass">
		<xsl:call-template name="property2class">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
	</xsl:variable>
	/** getter for <xsl:value-of select="@name"/> <xsl:value-of select="column/comment"/>*/
	public <xsl:value-of select="@type"/> get<xsl:value-of select="$pptyName"/>();
	/** setter for <xsl:value-of select="@name"/> <xsl:value-of select="column/comment"/>*/
	<xsl:choose>
	<xsl:when test="local-name(.)=&apos;id&apos;">private</xsl:when>
	<xsl:when test="local-name(.)=&apos;property&apos;">public</xsl:when>
	</xsl:choose> set<xsl:value-of select="$pptyName"/>(<xsl:value-of select="@type"/> value);
	</xsl:for-each>
	
	<xsl:for-each select="map|set">
		<xsl:variable name="pptyClass">
			<xsl:call-template name="property2class">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:variable>
		Join&lt;<xsl:value-of select="$pptyClass"/>&gt; get<xsl:value-of select="@name"/>();
	</xsl:for-each>
	
	}
</xsl:element>
</xsl:template>

<xsl:template match="class" mode="class">
<xsl:variable name="package">
 <xsl:call-template name="class2package">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:variable name="className">
 <xsl:call-template name="class2className">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:element name="file">
<xsl:attribute name="path"><xsl:value-of select="concat(&apos;src/&apos;,translate($package,&apos;.&apos;,&apos;/&apos;),&apos;/&apos;,$className,&apos;Impl.java&apos;)"/></xsl:attribute>
/**
 * <xsl:value-of select="$className"/>
 *
 */
public class <xsl:value-of select="$className"/>Impl
	implements <xsl:value-of select="$className"/>
	{
	<xsl:for-each select="id|property">
	<xsl:variable name="pptyName">
		<xsl:call-template name="propertyName">
		  <xsl:with-param name="name"><xsl:value-of select="@name"/></xsl:with-param>
		</xsl:call-template>
	</xsl:variable>
	
	<xsl:variable name="pptyClass">
		<xsl:call-template name="property2class">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
	</xsl:variable>


	private <xsl:value-of select="$pptyClass"/> _<xsl:value-of select="@name"/><xsl:choose>
		<xsl:when test="@type=&apos;&apos;"></xsl:when>
		<xsl:otherwise>=null</xsl:otherwise>
		</xsl:choose>;
	
	public <xsl:value-of select="$pptyClass"/> get<xsl:value-of select="$pptyName"/>()
		{
		return this._<xsl:value-of select="@name"/>;
		}
	<xsl:choose>
	<xsl:when test="local-name(.)=&apos;id&apos;">private</xsl:when>
	<xsl:when test="local-name(.)=&apos;property&apos;">public</xsl:when>
	</xsl:choose> set<xsl:value-of select="$pptyName"/>(<xsl:value-of select="$pptyClass"/> value)
		{
		this._<xsl:value-of select="@name"/>=value;
		}
		
	</xsl:for-each>
	
	
	
	@Override
	public boolean equals(Object o)
		{
		if(this==o) return true;
		if(o==null || !(o instance of <xsl:value-of select="$className"/> )) return false;
		<xsl:if test="id">
		
		<xsl:variable name="pptyName">
			<xsl:call-template name="propertyName">
			<xsl:with-param name="name"><xsl:value-of select="id/@name"/></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:variable name="pptyClass">
			<xsl:call-template name="property2class">
				<xsl:with-param name="node" select="id"/>
			</xsl:call-template>
		</xsl:variable>
		if( get<xsl:value-of select="$pptyName"/>().equals(<xsl:value-of select="$className"/>.class.cast(o.get<xsl:value-of select="$pptyName"/>())))
			{
			return true;
			}
		
		</xsl:if>
		return false;//FIX THIS
		}
	
	@Override
	public String toString()
		{
		StringBuilder b= new StringBuilder();
		return b.toString();
		}
	}
</xsl:element>
</xsl:template>



<xsl:template match="class" mode="tableModel">
<xsl:element name="file">
/**
 * <xsl:value-of select="@name"/>TableModel
 *
 */
public class <xsl:value-of select="@name"/>TableModel
	extends AbstractTableModel
	{
	private Vector&lt;<xsl:value-of select="@name"/>&gt; values = new Vector&lt;<xsl:value-of select="@name"/>&gt;();
	
	public <xsl:value-of select="@name"/>TableModel()
		{
		}
	
	public Class&lt;?&gt; getColumnClass(int column)
		{
		switch(column)
			{
			<xsl:for-each select="id|property">
			case <xsl:value-of select="position()-1"/>:break;
			</xsl:for-each>
			default:return null;
			}
		
		}
	public String getColumnName(int column)
		{
		switch(column)
			{
			<xsl:for-each select="id|property">
			case <xsl:value-of select="position()-1"/>: return "<xsl:value-of select="@name"/>";
			</xsl:for-each>
			default:return null;
			}
		}
	public int getColumnCount()
		{
		return <xsl:value-of select="count(id|property)"/>;
		}
	public Object getValueAt(int row,int column)
		{
		<xsl:value-of select="@name"/> o= this.values.elementAt(row);
		switch(column)
			{
			<xsl:for-each select="id|property">
			case <xsl:value-of select="position()-1"/>: return o.get();
			</xsl:for-each>
			default:return null;
			}
		}
		
	public boolean isCellEditable(int row,int column)
		{
		switch(column)
			{
			<xsl:for-each select="id|property">
			case <xsl:value-of select="position()-1"/>: return false;
			</xsl:for-each>
			default:return false;
			}
		}
	}
</xsl:element>
</xsl:template>

<xsl:template match="class" mode="tupleBinding">
<xsl:element name="file">
/**
 * <xsl:value-of select="@name"/>TableModel
 *
 */
public class <xsl:value-of select="@name"/>Binding
	extends TupleBinding&lt;<xsl:value-of select="@name"/>&gt;
	{
	public <xsl:value-of select="@name"/> entryToObject(TupleInput in)
		{
		}
	public void objectToEntry(<xsl:value-of select="@name"/> object,TupleOutput out)
		{
		}
	}
</xsl:element>
</xsl:template>


<xsl:template match="class" mode="editorPane">

<xsl:variable name="package">
 <xsl:call-template name="class2package">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:variable name="className">
 <xsl:call-template name="class2className">
   <xsl:with-param name="node" select="."/>
 </xsl:call-template>
</xsl:variable>
<xsl:element name="file">
<xsl:attribute name="path"><xsl:value-of select="concat(&apos;src/&apos;,translate($package,&apos;.&apos;,&apos;/&apos;),&apos;/&apos;,$className,&apos;EditorPane.java&apos;)"/></xsl:attribute>

/**
 * <xsl:value-of select="$className"/>EditorPane
 *
 */
public class <xsl:value-of select="$className"/>EditorPane
	extends JPanel
	{
	<xsl:for-each select="id|property">
		private JTextField <xsl:value-of select="@name"/>=null;
	</xsl:for-each>
	
	
	public <xsl:value-of select="$className"/>EditorPane()
		{
		super(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		<xsl:for-each select="id|property">
		JLabel label = new JLabel(myResources.getString("label.<xsl:value-of select="$className"/>.<xsl:value-of select="@name"/>"));
		label.setTooltipText(myResources.getString("tootip.<xsl:value-of select="$className"/>.<xsl:value-of select="@name"/>"));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = <xsl:value-of select="position()"/>;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(5, 5, 5, 5);
		c.anchor = GridBagConstraints.EAST ;
		pane.add(button, c);

		this.<xsl:value-of select="@name"/>= new JTextField();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = <xsl:value-of select="position()"/>;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.insets = new Insets(5, 5, 5, 5);
		pane.add(this.<xsl:value-of select="@name"/>, c);

		</xsl:for-each>
		}
	}
</xsl:element>
</xsl:template>

<xsl:template name="xsd2java">
<xsl:param name="uri" select="java.lang.String"/>
<xsl:choose>
 <xsl:when test="$uri = &apos;java:java.lang.String&apos;">java.lang.String</xsl:when>
 <xsl:otherwise><xsl:value-of select="$uri"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="propertyName">
<xsl:param name="name"/>
<xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
<xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
<xsl:value-of select="concat(translate(substring($name,1,1),$lcletters,$ucletters),substring($name,2))"/>
</xsl:template>


<xsl:template name="string-after-last">
<xsl:param name="s"/>
<xsl:param name="delim">.</xsl:param>
<xsl:choose>
<xsl:when test="contains($s,$delim)">
	<xsl:call-template name="string-after-last">
		<xsl:with-param name="delim" select="$delim"/>
		<xsl:with-param name="s" select="substring-after($s,$delim)"/>
	</xsl:call-template>
</xsl:when>
<xsl:otherwise><xsl:value-of select="$s"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="string-before-last">
<xsl:param name="s"/>
<xsl:param name="delim">.</xsl:param>
<xsl:param name="result"></xsl:param>

<xsl:choose>
<xsl:when test="contains($s,$delim)">
	<xsl:call-template name="string-before-last">
		<xsl:with-param name="delim" select="$delim"/>
		<xsl:with-param name="s" select="substring-after($s,$delim)"/>
		<xsl:with-param name="result">
			<xsl:choose>
			<xsl:when test="string-length($result) = 0">
				<xsl:value-of select="substring-before($s,$delim)"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat($result,$delim,substring-before($s,$delim))"/>
			</xsl:otherwise>
			</xsl:choose>
		</xsl:with-param>
	</xsl:call-template>
</xsl:when>
<xsl:otherwise><xsl:value-of select="$result"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="class2className">
<xsl:param name="node"/>
<xsl:call-template name="propertyName">
<xsl:with-param name="name">
<xsl:choose>
<xsl:when test="contains($node/@name,&apos;.&apos;)">	
	<xsl:call-template name="string-after-last">
		<xsl:with-param name="delim">.</xsl:with-param>
		<xsl:with-param name="s" select="$node/@name"/>
	</xsl:call-template>
</xsl:when>
<xsl:when test="$node/@name">	
	<xsl:value-of select="$node/@name"/>
</xsl:when>
<xsl:when test="$node/@table">	
	<xsl:value-of select="$node/@table"/>
</xsl:when>
<xsl:otherwise>!!!!
</xsl:otherwise>
</xsl:choose>
</xsl:with-param>
</xsl:call-template>
</xsl:template>

<xsl:template name="class2package">
<xsl:param name="node"/>
<xsl:choose>
<xsl:when test="contains($node/@name,&apos;.&apos;)">	
	<xsl:call-template name="string-before-last">
		<xsl:with-param name="delim">.</xsl:with-param>
		<xsl:with-param name="s" select="$node/@name"/>
	</xsl:call-template>
</xsl:when>
<xsl:when test="/hibernate-mapping/@package">	
	<xsl:value-of select="/hibernate-mapping/@package"/>
</xsl:when>
<xsl:when test="$package">	
	<xsl:value-of select="$package"/>
</xsl:when>
<xsl:otherwise>test</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="property2class">
<xsl:param name="node"/>
	<xsl:choose>
		<xsl:when test="$node/one-to-many/@Class">TOTOTO</xsl:when>
		<xsl:when test="$node/@type"><xsl:value-of select="$node/@type"/></xsl:when>
		<xsl:when test="local-name($node)=&apos;id&apos;">java.lang.Long</xsl:when>
		<xsl:otherwise>java.lang.String</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>