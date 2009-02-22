




function XULDB()
	{
	var file = Components.classes["@mozilla.org/file/directory_service;1"]
                        .getService(Components.interfaces.nsIProperties)
                        .get("ProfD", Components.interfaces.nsIFile);

	file.append("xuldb.sqlite");

	var storageService = Components.classes["@mozilla.org/storage/service;1"]
		.getService(Components.interfaces.mozIStorageService);
	//see https://developer.mozilla.org/en/mozIStorageConnection
	this.connection = storageService.openDatabase(file);
	
	if(!this.connection.tableExists("version"))
		{
		this.connection.createTable("version","version INTEGER PRIMARY KEY AUTOINCREMENT");
		this.connection.executeSimpleSQL("insert or ignore into version(version) values (1)");
		}
	
	if(!this.connection.tableExists("RDFClass"))
		{
		this.connection.createTable("RDFClass",
			"id INTEGER PRIMARY KEY AUTOINCREMENT,"+
			"label TEXT NOT NULL,"+
			"description TEXT NOT NULL,"+
			"uri TEXT UNIQUE NOT NULL,"+
			"parent INTEGER NOT NULL REFERENCES RDFClass(id)"
			);
		this.connection.executeSimpleSQL(
			"insert or ignore into RDFClass(id,label,description,uri,parent) values (1,\"rdfs:Class\",\"rdfs:Class\",\""+ RDFS.NS +"Class\",1)");
		}
	if(!this.connection.tableExists("RDFProperty"))
		{
		this.connection.createTable("RDFProperty",
			"id INTEGER PRIMARY KEY AUTOINCREMENT,"+
			"label TEXT NOT NULL,"+
			"description TEXT NOT NULL,"+
			"uri TEXT UNIQUE NOT NULL,"+
			"js TEXT NOT NULL,"+
			"class_id INTEGER NOT NULL REFERENCES RDFClass(id)"
			);
		var insert="insert or ignore into RDFProperty(id,label,description,uri,js,class_id) values ";
		var js="{type:\"text\",minOcc:1,maxOcc:1,empty:false,defaultValue:\"\"}";
		
		this.connection.executeSimpleSQL(
			insert+ "(1,\'Title\',\'Title for this Resource\',\'"+ DC.NS +"title\',\'"+ js.escapeSqlite()+"\',1)"
			);
		
		this.connection.executeSimpleSQL(
			insert+ "(2,\'Description\',\'Description for this Resource\',\'"+ DC.NS +"description\',\'"+ js.escapeSqlite()+"\',1)"
			);
		
		
		}
	
	}

XULDB.prototype.stmt2property = function(stmt)
	{
	var ppty=  {
		label: stmt.getString(0),
		description: stmt.getString(1),
		uri: stmt.getString(2),
		js: stmt.getString(3),
		class_id: stmt.getInt32(4),
		id: stmt.getInt32(5)
 		};
 	return ppty;
	};

XULDB.prototype.stmt2class = function(stmt)
	{
	var clazz=  {
		label: stmt.getString(0),
		description: stmt.getString(1),
		uri: stmt.getString(2),
		parent_id: stmt.getInt32(3),
		id: stmt.getInt32(4)
 		};
 	return clazz;
	};



XULDB.prototype.getClassById = function(id)
	{
	var stmt=this.connection.createStatement("select label,description,uri,parent,id from RDFClass where id=?1");
	stmt.bindInt32Parameter(0,id);
 	if(!stmt.executeStep()) return null;
 	
 	return this.stmt2class(stmt);
	};

XULDB.prototype.getPropertyById = function(id)
	{
	var stmt=this.connection.createStatement("select label,description,uri,js,class_id,id from RDFProperty where id=?1");
	stmt.bindInt32Parameter(0,id);
 	if(!stmt.executeStep()) return null;
 	try {
 	
 	return this.stmt2property(stmt); } catch(err) { jsdump(err.message); return null;}
	};

XULDB.prototype.getSubClassesIds = function(id)
	{
	var stmt=this.connection.createStatement("select id from RDFClass where parent=?1 and id!=1 order by label");
	stmt.bindInt32Parameter(0,id);
	var array= new Array();
	while(stmt.executeStep())
		{
		array.push(stmt.getInt32(0));
		}
	return array;
	};

XULDB.prototype.getPropertyIdsOfClass = function(id)
	{
	var stmt=this.connection.createStatement("select id from RDFProperty where class_id=?1 order by label");
	stmt.bindInt32Parameter(0,id);
	var array= new Array();
	while(stmt.executeStep())
		{
		array.push(stmt.getInt32(0));
		}
	return array;
	};


XULDB.prototype.getSubClasses = function(id)
	{
	var ids= XULDB.getSubClassesIds(id);
	var array= new Array();
	for(var i=0;i < ids.length;++i)
		{
		array.push(this.getClassById(ids[i]));
		}
	return array;
	};

var env={
	xul:null,
	window:null
	};

function appendProperty(root, id)
	{
	var ppt = env.xul.getPropertyById(id);
	if(ppt==null) return;
	var titem = document.createElementNS(XUL.NS,"treeitem");
	var trow = document.createElementNS(XUL.NS,"treerow");
	var tcell = document.createElementNS(XUL.NS,"treecell");
	root.appendChild(titem);
	
	titem.appendChild(trow);
	titem.setAttribute("container","true");
	titem.setAttribute("open","true");
	trow.appendChild(tcell);
	tcell.setAttribute("label",ppt.label);
	tcell.setAttribute("class","rdfProperty");
	tcell.setAttribute("value",id);
	}
	
function buildTree(root, id)
	{
	var stmt=env.xul.connection.createStatement("select label from RDFClass where id=?1");
	stmt.bindInt32Parameter(0,id);
 	if(!stmt.executeStep()) return;
	
	var tchildren=null;
	var titem = document.createElementNS(XUL.NS,"treeitem");
	var trow = document.createElementNS(XUL.NS,"treerow");
	var tcell = document.createElementNS(XUL.NS,"treecell");
	
	if(root.localName=="treechildren")
		{
		root.appendChild(titem);
		}
	else
		{
		tchildren = document.createElementNS(XUL.NS,"treechildren");
		tchildren.appendChild(titem);
		root.appendChild(tchildren);
		}
	
	titem.appendChild(trow);
	titem.setAttribute("container","true");
	titem.setAttribute("open","true");
	trow.appendChild(tcell);
	tcell.setAttribute("label",stmt.getString(0));
	tcell.setAttribute("class","rdfClass");
	tcell.setAttribute("value",id);
	
	tchildren = document.createElementNS(XUL.NS,"treechildren");
	titem.appendChild(tchildren);
	/** loop over the properties of id */
	var array= env.xul.getPropertyIdsOfClass(id);
	for(var i=0;i < array.length;++i)
		{
		appendProperty(tchildren,array[i]);
		}
	
	/** loop over the children of id */
	array= env.xul.getSubClassesIds(id);
	for(var i=0;i < array.length;++i)
		{
		/** recursive call */
		buildTree(tchildren,array[i]);
		}
		
	};

function reloadTree()
	{
	var root=$("tree-class");
	var n=null;
	while((n=DOM.firstChildNS(root,XUL.NS,"treechildren"))!=null)
		{
		root.removeChild(n);
		}
	buildTree(root,1);
	};

function documentLoaded()
	{
	try
		{
		env.xul= new XULDB();
		env.window= window;
		buildTree($("tree-class"),1);
		}
	catch(e)
		{
		alert(e.message);
		
		}
	
	};

function getSelectedTreeCell(tree)
	{
	var index=tree.currentIndex;
	if(index==-1) return null;
	var titem =tree.view.getItemAtIndex(index);
	if(titem==null) return null;
	var trow = DOM.firstChildNS(titem,XUL.NS,"treerow");
	if(trow==null) return null;
	var tcell = DOM.firstChildNS(trow,XUL.NS,"treecell");
	return tcell;
	}

function treeWasSelected(tree)
	{
	var tcell=getSelectedTreeCell(tree);
	var sel = (tcell==null?"true":"false");
	var isClazz= (tcell!=null && tcell.getAttribute("class")=="rdfClass");
	var isPpty=  (tcell!=null && tcell.getAttribute("class")=="rdfProperty");
	
	var menu=$("menu-add-subclass");
	menu.setAttribute("disabled",!isClazz);
	menu.setAttribute("label","Add Sub-Class to "+tcell.getAttribute("label"));
	
	menu=$("menu-add-property");
	menu.setAttribute("disabled",!isClazz);
	menu.setAttribute("label","Add Property to "+tcell.getAttribute("label"));
	
	menu=$("menu-edit-subclass");
	menu.setAttribute("disabled",!isClazz);
	menu.setAttribute("label","Edit "+tcell.getAttribute("label"));
	
	menu=$("menu-edit-property");
	menu.setAttribute("disabled",!isPpty);
	menu.setAttribute("label","Edit "+tcell.getAttribute("label"));
	//menu.setAttribute("label","A");
	//$("menu-add-property").setAttribute("disabled",sel);
	//alert(DOM.serialize(tcell));
	};


function addProperty()
	{
	var tcell =getSelectedTreeCell($("tree-class"));
	if(tcell==null) return;
	var label= tcell.getAttribute("label");
	var dialog = window.openDialog("addproperty.xul", "Add Property to "+label, "dialog,modal",
		{
		env:env,
		label: label,
		parent_id: tcell.getAttribute("value"),
		owner:window,
		id:null
		});
	}

function addSubClass()
	{
	var tcell =getSelectedTreeCell($("tree-class"));
	if(tcell==null) return;
	var label= tcell.getAttribute("label");
	var dialog = window.openDialog("addsubclass.xul", "Add subclass to "+label, "dialog,modal",
		{
		env:env,
		label: label,
		parent_id: tcell.getAttribute("value"),
		owner:window,
		id:null
		});
	}

function editSubClass()
	{
	var tcell =getSelectedTreeCell($("tree-class"));
	if(tcell==null) return;
	var label= tcell.getAttribute("label");
	var node_id =tcell.getAttribute("value");
	if(node_id=="1")
		{
		alert("You cannot Edit root");
		return;
		}
	var dialog = window.openDialog("addsubclass.xul", "Edit "+label, "dialog,modal",
		{
		env:env,
		label:label,
		parent_id: null,
		owner:window,
		id: node_id
		});
	}

function exportOntology(id,parent_uri,os)
	{
	if(id==null)
		{
		try {
			const nsIFilePicker = Components.interfaces.nsIFilePicker;
		
			var fp = Components.classes["@mozilla.org/filepicker;1"]
				.createInstance(nsIFilePicker);
			fp.init(window, "Save As...", nsIFilePicker.modeSave);
			
		
			var rv = fp.show();
			if (!(rv == nsIFilePicker.returnOK || rv == nsIFilePicker.returnReplace) ) return;
			var file = fp.file;
		
		
			
			// file is nsIFile, data is a string
			var foStream = Components.classes["@mozilla.org/network/file-output-stream;1"].
						createInstance(Components.interfaces.nsIFileOutputStream);
		
			// use 0x02 | 0x10 to open file for appending.
			foStream.init(file, 0x02 | 0x08 | 0x20, 0666, 0); 
			// write, create, truncate
			// In a c file operation, we have no need to set file mode with or operation,
			// directly using "r" or "w" usually.
			
			 os = Components.classes["@mozilla.org/intl/converter-output-stream;1"]
                   		.createInstance(Components.interfaces.nsIConverterOutputStream);
			os.init(foStream, "UTF-8", 0, 0x0000);

			os.writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			os.writeString("<rdf:RDF xmlns:rdf=\""+RDF.NS+"\" xmlns:rdfs=\""+RDFS.NS+"\">\n");
			exportOntology(1,null,os);
			os.writeString("</rdf:RDF>");
			foStream.close();
		
		
			} catch(err){ alert(err.message);}
		return;
		}
	var clazz=env.xul.getClassById(id);
	if(clazz==null) return;
	os.writeString("<rdfs:Class rdf:about=\""+clazz.uri.escapeXML()+"\">\n");
	os.writeString("  <rdfs:label>"+clazz.label.escapeXML()+"</rdfs:label>\n");
	os.writeString("  <rdfs:comment>"+clazz.description.escapeXML()+"</rdfs:comment>\n");
	if(parent_uri!=null)
		{
		os.writeString("  <rdfs:subClassOf rdf:resource=\""+parent_uri.escapeXML()+"\"/>\n");
		}
	os.writeString("</rdfs:Class>\n");
	var child_ids= env.xul.getSubClassesIds(id);
	for(var i=0;i < child_ids.length;++i)
		{
		exportOntology(child_ids[i],clazz.uri,os);
		}
	}
	
