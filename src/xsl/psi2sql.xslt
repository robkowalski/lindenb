<?xml version='1.0' encoding="UTF-8"?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
 	xmlns:psi="net:sf:psidev:mi"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	version='1.0'
	>
<!--

psi2sql.xslt
Author:
	Pierre Lindenbaum PhD
Mail:
	plindenbaum@yahoo.fr

transform a psi/xml description of protein/protein interactions to
mysql statements to load it into a mysql database.

I tested this input with http://string.embl.de/api/psi-mi/interactions?identifier=ROXAN

Usage:

	xsltproc psi2sql.xslt interactions.xml | mysql -u {login} -p -D ${db} -N    

This is a really basic implementation: e.g: all experiment must 
have one pmid, there must be one and only one 'names' for each entity,
I did't much escaped sql strings, etc...

Pierre

-->


<xsl:param name="temporary">temporary</xsl:param>


<xsl:output method="text" encoding="UTF-8"/>

<xsl:template match="/">


create <xsl:value-of select="$temporary"/> table if not exists interactor
	(
	id integer not null primary key auto_increment,
	pk varchar(50) not null unique,
	local_id integer unique,
	shortLabel varchar(255),
	fullName text
	);

create <xsl:value-of select="$temporary"/> table if not exists experiment
	(
	id integer not null primary key auto_increment,
	local_id integer unique,
	shortLabel varchar(255) not null,
	fullName text,
	pmid integer unique,
	ncbiTaxId integer,
	interactionMethod varchar(255),
	interactorMethod varchar(255)
	);

create <xsl:value-of select="$temporary"/> table if not exists xref
	(
	id integer not null primary key auto_increment,
	interactor_id int not null,
	CONSTRAINT foreign key (interactor_id) references interactor(id),
	xrefType enum('primaryRef','secondaryRef') not null,
	db varchar(50),
	dbAc varchar(150), 
	pk varchar(50),
	refType varchar(50),
	refTypeAc varchar(50),

	constraint xrefUnique unique (db,dbAc,pk)
	);



create <xsl:value-of select="$temporary"/> table  if not exists interaction
	(
	id integer not null primary key auto_increment,
	interactor1_id int not null,
	CONSTRAINT foreign key (interactor1_id) references interactor(id),
	interactor2_id int not null,
	CONSTRAINT foreign key (interactor2_id) references interactor(id),
	unitLabel varchar(50),
	unitFullName varchar(100),
	confidence float,
	experiment_id int not null,
	CONSTRAINT foreign key (experiment_id) references experiment(id)
	);


update interactor set local_id=NULL;
update experiment set local_id=NULL;
<xsl:apply-templates/>

update interactor set local_id=NULL;
update experiment set local_id=NULL;
</xsl:template>


<xsl:template match="psi:entrySet">
<xsl:apply-templates select="psi:entry"/>
</xsl:template>

<xsl:template match="psi:entry">
<xsl:apply-templates select="psi:experimentList"/>
<xsl:apply-templates select="psi:_______________interactorList"/>
<xsl:apply-templates select="psi:_______________interactionList"/>
</xsl:template>


<xsl:template match="psi:experimentList">
<xsl:apply-templates select="psi:experimentDescription"/>
</xsl:template>

<xsl:template match="psi:interactorList">
<xsl:apply-templates select="psi:interactor"/>
</xsl:template>


<xsl:template match="psi:interactionList">
<xsl:apply-templates select="psi:interaction"/>
</xsl:template>


<xsl:template match="psi:experimentDescription">

insert ignore into experiment(id) values(<xsl:apply-templates select="@id"/>);
select @expid=_id from experiment where id=<xsl:apply-templates select="@id"/>;
<xsl:if test="">
</xsl:if>
</xsl:template>


<xsl:template match="psi:interactor">
insert ignore into interactor(pk,shortLabel,fullName)
values
("<xsl:value-of select="psi:xref/psi:primaryRef/@id"/>","<xsl:value-of select="psi:names[1]/psi:shortLabel"/>","<xsl:value-of select="translate(psi:names[1]/psi:fullName,&apos;&quot;&apos;,&quot;&apos;&quot;)"/>");

update interactor set local_id=<xsl:value-of select="@id"/> where pk="<xsl:value-of select="psi:xref/psi:primaryRef/@id"/>";

select @id:=id from interactor where local_id=<xsl:value-of select="@id"/>;

<xsl:for-each select="psi:xref/psi:primaryRef|psi:xref/psi:secondaryRef">

insert ignore into xref(interactor_id,xrefType,db,dbAc,pk,refType,refTypeAc)
values
(@id,"<xsl:value-of select="name()"/>","<xsl:value-of select="@db"/>","<xsl:value-of select="@dbAc"/>","<xsl:value-of select="translate(@id,&apos;&quot;&apos;,&quot;&apos;&quot;)"/>","<xsl:value-of select="@refType"/>","<xsl:value-of select="@refTypeAc"/>")
;


</xsl:for-each>
</xsl:template>


<xsl:template match="psi:interaction">
<xsl:if test="count(psi:participantList/psi:participant)!=2">
This stylesheet expect only binary interaction but found <xsl:value-of select="count(psi:participant)"/>
in interaction od=<xsl:value-of select="@id"/>;
</xsl:if>

select @AID:=id from interactor where local_id=<xsl:apply-templates select="psi:participantList/psi:participant[1]/psi:interactorRef"/>;
select @BID:=id from interactor where local_id=<xsl:apply-templates select="psi:participantList/psi:participant[2]/psi:interactorRef"/>;
select @EID:=id from experiment where local_id=<xsl:apply-templates select="psi:experimentList/psi:experimentRef"/>;

insert ignore into interaction(interactor1_id, interactor2_id,unitLabel,unitFullName,confidence,experiment_id)
select
	IF(@AID&lt;@BID,@AID,@BID),
	IF(@AID&lt;@BID,@BID,@AID),
	<xsl:apply-templates select="psi:confidenceList/psi:confidence[1]/psi:unit/psi:names[1]/psi:shortLabel"/>,
	<xsl:apply-templates select="psi:confidenceList/psi:confidence[1]/psi:unit/psi:names[1]/psi:fullName"/>,
	<xsl:apply-templates select="psi:confidenceList/psi:confidence[1]/psi:value"/>,
	@EID
	;
</xsl:template>


<xsl:template match="*">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="@*">
<xsl:text>&quot;</xsl:text>
<xsl:call-template name="escape">
	<xsl:with-param name="s" select="."/>
</xsl:call-template>
<xsl:text>&quot;</xsl:text>
</xsl:template>

<xsl:template match="text()">
<xsl:text>&quot;</xsl:text>
<xsl:call-template name="escape">
	<xsl:with-param name="s" select="."/>
</xsl:call-template>
<xsl:text>&quot;</xsl:text>
</xsl:template>

<xsl:template name="escape">
 <xsl:param name="s"/>
        <xsl:choose>
        <xsl:when test="contains($s,'&quot;')">
                <xsl:value-of select="substring-before($s,'&quot;')"/>
                <xsl:text>\&quot;</xsl:text>
                <xsl:call-template name="escape">
                        <xsl:with-param name="s" select="substring-after($s,'&quot;')"/>
                </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
                <xsl:value-of select='$s'/>
        </xsl:otherwise>
        </xsl:choose>
</xsl:template>


</xsl:stylesheet>
