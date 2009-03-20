

/** when the xul page is loaded, register for events from the contextual popupmenu */
function onloadIMGDB()
	{
	document.addEventListener("popupshowing",function(evt){preparePopup4ImgDB(evt);},true);
	}
	
function isImgNode(img)
	{
	if(img==null || "img"!=img.localName.toLowerCase() ) return false;
	//   !(img.namespaceURI="http://www.w3.org/1999/xhtml" || img.namespaceURI==null)
	var src= img.getAttribute("src");
	if(src==null || src=="")
		{
		return;
		}
	return true;
	}
/* prepare the contextual menu just before it is showing on screen: hide or show our menu */
function preparePopup4ImgDB(evt)
	{
	var element = document.getElementById("popup-imgdb");
	var menu_add = document.getElementById("imgdb-quickadd");
	var img= document.popupNode;
	if(!isImgNode(img))
		{
		element.hidden=true;
		return;
		}
	element.hidden=false;
	}

function insertImage(db)
	{
	var img=document.popupNode;
	if( !isImgNode(img))
		{
		return;
		}
	var title = img.getAttribute("alt");
	if(title==null || title=="") title=content.document.title;
	var src = img.src;
	if(src==null || src=="") return;
	
	insertSQLite(db,
		src,
		title,
		img.width,
		img.height
		);
	}

function openimgdb(db)
	{
	var w=window.open("chrome://imgdb/content/imgdbwin.xul",db);
	}

function insertSQLite(db,url,title,width,height,comment)
	{
	try	{
		var connection=openConnection();
		
		var insert="insert or ignore into img(url,alt,docurl,doctitle,width,height,db_id) values (\'"+
					escapeSql(url)+"\',\'"+
					escapeSql(title)+"\',\'"+
					escapeSql(content.document.location.href)+"\',\'"+
					escapeSql(content.document.title)+"\',"+
					width +","+
					height+","+db+")";
		connection.executeSimpleSQL(insert);
		window.status="Image was Inserted.";
		}
        catch(e)
		{
		dumpjs(e.message);
		}
	}

	
	
window.addEventListener("load",onloadIMGDB, false);
