<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>
<!--

Author:
	Pierre Lindenbaum
	http://plindenbaum.blogspot.com
Motivation:
	transform friendfeed xml to mysql
Usage :

	xsltproc ff2sql.xsl "http://friendfeed-api.com/v2/feed/the-life-scientists?start=0&num=500&format=xml"   

paramerters:
	"temporary" set as the empty string if you don't want to use temporary tables

-->
<xsl:output method="text" ident="yes"/>
<xsl:param name="temporary">temporary</xsl:param>

<xsl:template match="/">
	<xsl:apply-templates select="feed"/>
</xsl:template>

<xsl:template match="feed">
create <xsl:value-of select="$temporary"/> table ff_user
	(
	id int primary key auto_increment,
	ff_id varchar(50) not null unique,
	ff_name varchar(255) not null
	);

create <xsl:value-of select="$temporary"/> table ff_entry
	(
	id int primary key auto_increment,
	ff_id varchar(255) not null unique,
	ff_date varchar(25),
	ff_url varchar(255),
	ff_body mediumtext,
	user_id int unsigned references ff_user(id)
	);

create <xsl:value-of select="$temporary"/> table ff_comment
	(
	entry_id int  references ff_entry(id),
	user_id int  references ff_user(id),
	ff_id varchar(255) not null unique,
	ff_body mediumtext,
	ff_date varchar(25)
	);

create <xsl:value-of select="$temporary"/> table ff_like
	(
	entry_id int  references ff_entry(id),
	user_id int  references user(id),
	ff_date datetime
	);

	
<xsl:apply-templates select="entry"/>

</xsl:template>


<xsl:template match="entry[from/type='user']">

<xsl:apply-templates select="from"/>
insert ignore into ff_entry(user_id,ff_id,ff_date,ff_body,ff_url) values
	(
	@user_id,
	<xsl:apply-templates select="id"/>,
	<xsl:apply-templates select="date"/>,
	<xsl:apply-templates select="body"/>,
	<xsl:apply-templates select="url"/>
	);

select @entry_id:=id from ff_entry where ff_id=<xsl:apply-templates select='id'/>;

<xsl:apply-templates select="comment"/>

<xsl:apply-templates select="like"/>

</xsl:template>

<xsl:template match="from">

<xsl:choose>
	<xsl:when test="type='user'">
		insert ignore into ff_user(ff_id,ff_name)
		values
		(
		<xsl:apply-templates select="id"/>,
		<xsl:apply-templates select="name"/>
		);
	select @user_id:=id from ff_user where ff_id=<xsl:apply-templates select='id'/>;
	</xsl:when>
	<xsl:otherwise>
		<xsl:message terminate="yes">Uhh ? <xsl:value-of select="type"/></xsl:message>
	</xsl:otherwise>
</xsl:choose>

</xsl:template>

<xsl:template match="comment">
<xsl:if test="from/type!='user'">
	<xsl:message terminate="yes">Uhh ? <xsl:value-of select="from/type"/></xsl:message>
</xsl:if>
<xsl:apply-templates select="from"/>
insert ignore into ff_comment(entry_id,user_id,ff_id,ff_date,ff_body) values
	(
	@entry_id,
	@user_id,
	<xsl:apply-templates select="id"/>,
	<xsl:apply-templates select="date"/>,
	<xsl:apply-templates select="body"/>
	);

</xsl:template>

<xsl:template match="like">
<xsl:if test="from/type!='user'">
	<xsl:message terminate="yes">Uhh ? <xsl:value-of select="from/type"/></xsl:message>
</xsl:if>
<xsl:apply-templates select="from"/>
insert into ff_like(entry_id,user_id,ff_date) values
	(
	@entry_id,
	@user_id,
	<xsl:apply-templates select="date"/>
	);

</xsl:template>



<xsl:template match="id|name|body|url|date">
<xsl:call-template name="quote">
	<xsl:with-param name="s" select="."/>
</xsl:call-template>
</xsl:template>

<xsl:template name="quote">
        <xsl:param name="s"/>
        
        
        <xsl:variable  name="v0">
        <xsl:call-template name="escape">
        	<xsl:with-param name="pat">\</xsl:with-param>
                <xsl:with-param name="rep">\\</xsl:with-param>
                <xsl:with-param name="s" select="$s"/>
        </xsl:call-template>
        </xsl:variable>
        
         
        
        <xsl:variable  name="v1">
        <xsl:call-template name="escape">
        	<xsl:with-param name="pat">'</xsl:with-param>
                <xsl:with-param name="rep">\'</xsl:with-param>
                <xsl:with-param name="s" select="$v0"/>
        </xsl:call-template>
        </xsl:variable>
       
        
        <xsl:variable  name="v2">
        <xsl:call-template name="escape">
        	<xsl:with-param name="pat">"</xsl:with-param>
                <xsl:with-param name="rep">\"</xsl:with-param>
                <xsl:with-param name="s" select="$v1"/>
        </xsl:call-template>
        </xsl:variable>
        
        
         <!--
        <xsl:variable  name="v3">
        <xsl:call-template name="escape">
        	<xsl:with-param name="pat">&#x0D;</xsl:with-param>
                <xsl:with-param name="rep">\n</xsl:with-param>
                <xsl:with-param name="s" select="$v2"/>
        </xsl:call-template>
        </xsl:variable>
        
         <xsl:message>v3 <xsl:value-of select="$v3"/></xsl:message>
        
        <xsl:variable  name="v4">
        <xsl:call-template name="escape">
        	<xsl:with-param name="pat">&#x09;</xsl:with-param>
                <xsl:with-param name="rep">\t</xsl:with-param>
                <xsl:with-param name="s" select="$v3"/>
        </xsl:call-template>
        </xsl:variable>
        
        <xsl:message>v4 <xsl:value-of select="$v4"/></xsl:message>
        
        
        <xsl:variable  name="v5">
        <xsl:call-template name="escape">
        	<xsl:with-param name="pat">&#x0A;</xsl:with-param>
                <xsl:with-param name="rep">\r</xsl:with-param>
                <xsl:with-param name="s" select="$v4"/>
        </xsl:call-template>
        </xsl:variable>
        
        <xsl:message>v5 <xsl:value-of select="$v5"/></xsl:message> -->
       
        <xsl:text>&quot;</xsl:text>
        <xsl:value-of select="$v2"/>
        <xsl:text>&quot;</xsl:text>
</xsl:template>



<xsl:template name="escape">
 <xsl:param name="s"/>
 <xsl:param name="pat"/>
 <xsl:param name="rep"/>
        <xsl:choose>
        <xsl:when test="contains($s,$pat)">
                <xsl:value-of select="substring-before($s,$pat)"/>
                <xsl:value-of select='$rep'/>
                <xsl:call-template name="escape">
                        <xsl:with-param name="s" select="substring-after($s,$pat)"/>
                        <xsl:with-param name="rep" select="$rep"/>
			<xsl:with-param name="pat" select="$pat"/>
                </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
                <xsl:value-of select='$s'/>
        </xsl:otherwise>
        </xsl:choose>
</xsl:template>



</xsl:stylesheet>


