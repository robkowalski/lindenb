/**************************
 * 
 * RDF library in Javascript
 * Author: Pierre Lindenbaum
 * plindenbaum@yahoo.fr
 * http://plindenbaum.blogspot.com
 */

/**
 * A RDF Resource
 */
function Resource(uri)
	{
	this.uri=uri;
	}

Resource.prototype.isLiteral=function()
	{
	return false;
	};

Resource.prototype.isResource=function()
	{
	return true;
	};

Resource.prototype.compareTo=function(other)
	{
	if(!other.isResource()) return -1;
	return this.uri.localeCompare(other.uri);
	};

Resource.prototype.toString=function()
	{
	if(this.isAnonId()) return this.uri;
	return "<"+this.uri+">";
	};

Resource.prototype.isAnonId=function()
	{
	return this.uri.substr(0,2)=="_:";
	};

/**
 * A RDF Literal
 */
function Literal(content,type,lang)
	{
	this.content=content;
	this.dataType=null;
	this.lang=null;
	}

Literal.prototype.isLiteral=function()
	{
	return true;
	};

Literal.prototype.isResource=function()
	{
	return false;
	};

Literal.prototype.toString=function()
	{
	return "\""+this.content+"\"";//TODO escape this !
	};

Literal.prototype.compareTo=function(other)
	{
	if(!other.isLiteral()) return 1;
	var i= this.content.localeCompare(other.content);
	if(i!=0) return i;
	
	};


/**
 * A RDF Statement (S,P,V)
 */
function Statement( subject, predicate,value)
	{
	this.subject=subject;
	this.predicate=predicate;
	this.value=value;
	}

Statement.prototype.isLiteral=function()
	{
	return this.value.isLiteral();
	};

Statement.prototype.isResource=function()
	{
	return this.value.isResource();
	};

Statement.prototype.compareTo=function(other)
	{
	var i= this.subject.compareTo(other.subject);
	if(i!=0) return i;
	i= this.predicate.compareTo(other.predicate);
	if(i!=0) return i;
	return this.value.compareTo(other.value);
	};

Statement.prototype.toString=function()
	{
	return ""+this.subject+" "+this.predicate+" "+this.value+" .";
	};

/**
 * A RDF store
 * statements are stored in a sorted array
 */
function RDFStore()
	{
	this.statements= new Array();	
	}

RDFStore.prototype.clear= function()
	{
	return this.statements.slice(0,this.statements.length);
	};


RDFStore.prototype.size= function()
	{
	return this.statements.length;
	};

RDFStore.prototype.lowerBound= function(stmt)
	{
	var first=0;
	var len = this.size();
	while (len > 0)
		    {
		    var half = (len / 2) | 0; //hack , cast to int
		    var middle = first + half;
		
		    var x= this.at(middle);
		    if(x.compareTo(stmt)<0)
			    {
			    first = middle + 1;
			    len -= half + 1;
			    }
		    else
			    {
			    len = half;
			    }
		    }
	return first;
	};


RDFStore.prototype.at= function(index)
	{
	return this.statements[index];
	};


RDFStore.prototype.add= function(stmt)
	{
	var i= this.lowerBound(stmt);
	if(i<this.size())
		{
		if( this.at(i).compareTo(stmt) == 0) return false;
		}
	this.statements.splice(i,0,stmt);
	return true;
	}

RDFStore.prototype.contains= function(stmt)
	{
	var i= this.lowerBound(stmt);
	return i<this.size() && this.at(i).compareTo(stmt) == 0;
	};

RDFStore.prototype.remove= function(stmt)
	{
	var i= this.lowerBound(stmt);
	if(i<this.size())
		{
		if( this.at(i).compareTo(stmt) != 0) return;
		}
	this.statements.splice(i,1);
	};

/**
 * find a given statement, arguments can be null to allow all the nodes
 * this method should be improved as all statements are sorted, access
 * should be straightforward
 */
RDFStore.prototype.filter= function(s,p,v)
	{
	var store= new RDFStore();
	for(var i=0;i< this.size();++i)
		{
		var stmt= this.at(i);
		if(s!=null && s.compareTo(stmt.subject)!=0) continue;
		if(p!=null && p.compareTo(stmt.predicate)!=0) continue;
		if(v!=null && v.compareTo(stmt.value)!=0) continue;
		store.add(stmt);
		}
	return store;
	};

RDFStore.prototype.parse=function(dom)
	{
	
	}


/**
 * parse a DOM document and extract the Statements
 * parseType="Literal" not implemented
 */
function DOM4RDF()
	{
	
	}

var ANONID=0;
	
DOM4RDF.prototype.parse=function(dom,store)
	{
	this.store = (typeof(store) != 'undefined' ? store : null );
	var tmp= this.parseRDF(dom.documentElement);
	this.store=null;
	return tmp;
	};

	
/** creates an anonymous ID */
DOM4RDF.prototype.createAnonId=function()
	{
	return new Resource("_:"+(++ ANONID));
	};
	
/** parse a rdf:RDF element */
DOM4RDF.prototype.parseRDF=function(root)
	{
	if(root==null) throw "null root";
	if(!this.isA(root, RDF.NS, "RDF")) throw  "Root is not rdf:RDF";

	//loop over children of rdf:RDF
	for(var n1= root.firstChild;
		n1!=null;n1=n1.nextSibling)
		{
		
		switch(n1.nodeType)
			{
			case 1:
				{
				this.parseResource(n1);
				break;
				}
			case 3:
			case 4:
				{
				this.checkNodeIsEmpty(n1);
				break;
				}
			case 7:
				{
				this.warning(n1, "Found Processing instruction under "+root.nodeName);
				break;
				}
			case 8:break;
			default: throw "Node type not handled : "+n1.nodeType;
			}
		}
	return this.store;
	};
	

	
/** return wether this node a a rdf:(abou|ID|nodeId) */
DOM4RDF.prototype.isAnonymousResource=function(rsrc)
	{
	var att= rsrc.getAttributeNodeNS(RDF.NS, "about");
	if(att!=null) return false;
	att= rsrc.getAttributeNodeNS(RDF.NS, "ID");
	if(att!=null) return false;
	att= rsrc.getAttributeNodeNS(RDF.NS, "nodeID");
	if(att!=null) return false;
	return true;
	};
	
/** returns a URI for an Resource element */
DOM4RDF.prototype.getResourceURI=function( root)
	{
	var subject=null;
	if(root.hasAttributes())
		{
		for(var i=0;i< root.attributes.length;++i)
			{
			var att=root.attributes[i];
			if( RDF.NS != att.namespaceURI ) continue;
			if(att.localName == "about")
				{
				if(subject!=null) throw "subject id defined twice";
				subject= new Resource(att.value);
				}
			else if(att.localName=="ID")
				{
				if(subject!=null) throw "subject id defined twice";
				var val= att.value;
				if(!val.startsWith("#")) val="#"+val;
				subject= new Resource(this.getBase(root)+val);
				}
			else if(att.localName == "nodeID" )
				{
				if(subject!=null) throw "subject id defined twice";
				subject= new Resource("_:"+att.value);
				//uri= URI.create(getBase(root)+att.value);
				}
			}
		}
	if(subject==null) subject= this.createAnonId();
	return subject;
	};
	

/** parse everything under rdf:RDF
 * @return the URI of the resource
 */
DOM4RDF.prototype.parseResource=function(root)
	{
	var subject= this.getResourceURI(root);
	
	if(root.hasAttributes())
		{
		for(var i=0;i< root.attributes.length;++i)
			{
			var att=root.attributes[i];
			if(RDF.NS == att.namespaceURI)
				{
				if(att.localName=="about" ||
				   att.localName=="nodeID" ||
				   att.localName=="ID"
					)
					{
					continue;
					}
				else  if(att.localName=="resource")
					{
					throw "should not contains rdf:resource";
					}
				else
					{
					throw "rdf:* node supported";
					}
				}
			else if(att.prefix=="xmlns")
				{
				//ignore
				}
			else
				{
				if(att.namespaceURI==null)
					{
					throw "No NamespaceURI associated with "+att.nodeName;
					}
				this.foundStatement(
					subject,
					new Resource(att.namespaceURI+att.localName),
					new Literal(att.value)
					);
				}
			}
		}
	if(!this.isA(root, RDF.NS, "Resource"))
		{
		if(root.namespaceURI==null)
			{
			throw "No NamespaceURI associated with "+root.nodeName;
			}
		this.foundStatement(
			subject,
			new Resource(RDF.NS+"type"),
			new Resource(root.namespaceURI+root.localName)
			);
		}
	this.parseResourceChildren(root, subject);
	return subject;
	};


DOM4RDF.prototype.isA=function(node,ns,localName)
	{
	return node.namespaceURI==ns && node.localName==localName;
	};

	
/** check a node contains only a blank stuff */
DOM4RDF.prototype.checkNodeIsEmpty=function( n1)
	{
	if(n1.nodeValue.replace(/^\s+|\s+$/g, "").length!=0)
		{
		throw "Found not whitespace content  under "+n1.parentNode.nodeName;
		}
	return true;
	};
	
	
	
/** parse everything under a resource element */
DOM4RDF.prototype.parseResourceChildren=function(root, subjectURI) 
	{
	for(var n1= root.firstChild;
		n1!=null;
		n1=n1.nextSibling)
		{
		switch(n1.nodeType)
			{
			case 1:
				{
				this.parseProperty(n1,subjectURI);
				break;
				}
			case 3:case 4:
				{
				this.checkNodeIsEmpty(n1);
				break;
				}
			case 7:
				{
				this.warning(n1, "Found Processing instruction under "+root.nodeName);
				break;
				}
			case 8:break;
			default:throw "invalid node type";
			}
		}
	};
	
/**  return the xml:lang of the node or null */
DOM4RDF.prototype.getLang=function( root)
	{
	if(root==null) return null;
	if( root.nodeType==1 && root.hasAttributes())
		{
		var att= root.getAttributeNodeNS(XML.NS, "lang");
		if(att!=null) return att.value;
		}
	return this.getLang(root.parentNode);
	};
	
/** parse everything under rdf:RDF */
DOM4RDF.prototype.parseProperty=function( property, subject)
		{
		var parseTypeNode= property.getAttributeNodeNS(RDF.NS, "parseType");
		var dataTypeNode =  property.getAttributeNodeNS(RDF.NS, "dataType");
		var dataType= (dataTypeNode==null?null:dataTypeNode.value);
		var parseType = parseTypeNode!=null?parseTypeNode.value:null;
		
		if(property.namespaceURI==null)
			{
			throw "no namespaceURI for "+property.tagName;
			}
		
		var predicate= new Resource(property.namespaceURI+property.localName);
		
		if(predicate==null)
			{
			throw "Cannot parse URI of this predicate";
			}
		/** default parse type */
		if(parseType==null)
			{
			var rsrc= property.getAttributeNodeNS(RDF.NS, "resource");
			if(!property.hasChildNodes())
				{
				if(rsrc==null)
					{
					//strange behavior of DOM parser. &lt;tag&gt;&lt;/tag&gt; is same as &lt;tag/&gt; ??!
					this.foundStatement(subject, predicate,new Literal(""));
					//throw new InvalidXMLException(property,"missing rdf:resource");
					}
				else
					{
					this.foundStatement(subject, predicate, new Resource(rsrc.value));
					}
				}
			else
				{
				if(rsrc!=null) throw "rdf:resource is present and element has children";
				if(predicate.uri == (RDF.NS+"type")) throw "rdf:type expected in an empty element"; 
				var count = 0;
				var firstChild=null;
				for(var n1=property.firstChild;n1!=null;n1=n1.nextSibling)
					{
					if(n1.nodeType!=1) continue;	
					count++;
					if(firstChild==null) firstChild=n1;
					}		

				switch(count)
					{
					case 0:  var L= new Literal( property.textContent);
						L.dataType= dataType;
						L.lang= this.getLang(property);
						this.foundStatement(subject, predicate,L );
						break;
					case 1: var value= this.parseResource(firstChild);
							this.foundStatement(subject, predicate,value);
							break;
					default: throw "illegal number of element under.";
					}
				}
			}
		else if( parseType == "Literal" )
			{
			var buff= "";
			for(var n1=property.firstChild;n1!=null;n1=n1.nextSibling)
				{
				switch(n1.nodeType)
					{
					case 3:
					case 4:
						{
						buff += n1.textContent;
						break;	
						}
					case 1:
						{
						//TODO
						break;
						}
					default: throw "node type unsupported "+n1.nodeType;
					}
				}
			
			
			if(subject!=null && predicate!=null && buff!=null)
				{
				var L2= new Literal(buff);
				L2.dataType= RDF.NS+"XMLLiteral";
				L2.lang= this.getLang(property);
				this.foundStatement(subject,predicate,L2);
				}
			}
		else if(parseType=="Resource")
			{
			var rsrc= this.createAnonId();
			if(subject!=null && predicate!=null)
				{
				this.foundStatement(subject,predicate,rsrc);
				}
			
			for(var n1=property.firstChild;n1!=null;n1=n1.nextSibling)
				{
				switch(n1.nodeType)
					{
					case 3:
						{
						this.checkNodeIsEmpty(n1);
						break;	
						}
					case 1:
						{
						this.parseProperty(n1, rsrc);
						break;
						}
					case 8:break;
					default: this.warning(n1, "unsupported node type");break;
					}
				}
			
			}
		else if(parseType=="Collection")
			{
			var list= new Array();
			for(var n1=property.firstChild;n1!=null;n1=n1.nextSibling)
				{
				switch(n1.nodeType)
					{
					case 8:break;
					case 3:
						{
						this.checkNodeIsEmpty(n1);
						break;	
						}
					case 1:
						{
						var r=	 this.parseResource(n1);
						list.push(r);
						break;
						}
					default: this.warning(n1, "unsupported node type");break;
					}
				}
			
			
			if(list.length==0)
				{
				this.warning(property,"Empty list");
				}
			else
				{
				var prevURI= this.createAnonId();
				
				if(subject!=null && predicate!=null)
					{
					this.foundStatement(subject, predicate,prevURI);
					}
				
				for(var i=0;i< list.length;++i)
					{
					if(i+1==list.length)
						{
						this.foundStatement(prevURI,new Resource(RDF.NS+"first"), list[i]);
						this.foundStatement(prevURI,new Resource(RDF.NS+"rest"), new Resource(RDF.NS+"nil"));
						}
					else
						{
						var newURI= this.createAnonId();
						this.foundStatement(prevURI,new Resource(RDF.NS+"first"), list[i]);
						this.foundStatement(prevURI,new Resource(RDF.NS+"rest"), newURI);
						prevURI=newURI;
						}
					}
				} 
			
			}
		else
			{
			throw "illegal rdf:parseType:"+parseType;
			}
		};
	
	
/** get the BASE url of the document */
DOM4RDF.prototype.getBase=function( n) 
	{
	var s="TODO";//n.ownerDocument.getBaseURI();
	if(s==null) throw "document has not xml:base";
	return new Resource(s);
	};

	
/**
 * Called when a Statement was found. User can override this method.
 * Default: does nothing
 * @param subject
 * @param property
 * @param value
 * @param dataType
 * @param lang
 */
DOM4RDF.prototype.foundStatement=function(subject,property,value)
	{
	if(this.store!=null) this.store.add(new Statement(subject,property,value));
	};
	
DOM4RDF.prototype.warning=function(node,message)
	{
	//logMsg("Warning " +message);
	};
	
