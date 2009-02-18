




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
		this.connection.executeSimpleSQL("insert or ignore into RDFClass(id,label,description,uri,parent) values (1,\"rdfs:Class\",\"rdfs:Class\",\""+ RDFS.NS +"Class\",1)");
		}
	
	}

	

var env={
	xul:null,
	window:null
	};

function buildTree(root, id)
	{
	var stmt=env.xul.connection.createStatement("select label from RDFClass where id=?1");
	stmt.bindInt32Parameter(0,id);
 	if(!stmt.executeStep()) return;
	 	
	var tchildren = document.createElementNS(XUL.NS,"treechildren");
	var titem = document.createElementNS(XUL.NS,"treeitem");
	var trow = document.createElementNS(XUL.NS,"treerow");
	var tcell = document.createElementNS(XUL.NS,"treecell");
	
	tchildren.appendChild(titem);
	titem.appendChild(trow);
	trow.appendChild(tcell);
	titem.setAttribute("container","true");
	titem.setAttribute("open","true");
	tcell.setAttribute("label",stmt.getString(0));
	tcell.setAttribute("class","rdfClass");
	tcell.setAttribute("value",id);
	root.appendChild(tchildren);
	
	/** loop over the children of id */
	var array= new Array();
	stmt=env.xul.connection.createStatement("select id from RDFClass where parent=?1 and id!=1 order by label");
	stmt.bindInt32Parameter(0,id);
	while(stmt.executeStep())
		{
		array.push(stmt.getInt32(0));
		}
	for(var i=0;i < array.length;++i)
		{
		/** recursive call */
		buildTree(titem,array[i]);
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
	}

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
	var sel = (getSelectedTreeCell(tree)==null?"true":"false");
	$("menu-add-subclass").setAttribute("disabled",sel);
	$("menu-add-property").setAttribute("disabled",sel);
	//alert(DOM.serialize(tcell));
	};
	
function addSubClass()
	{
	var tcell =getSelectedTreeCell($("tree-class"));
	if(tcell==null) return;
	var dialog = window.openDialog("addsubclass.xul", "Add subclass to ", "dialog,modal",
		{
		env:env,
		label: tcell.getAttribute("label"),
		value: tcell.getAttribute("value"),
		owner:window
		});
	}