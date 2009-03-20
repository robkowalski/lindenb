/** debug function */
function dumpjs(str)
        {
        Components.classes['@mozilla.org/consoleservice;1']
            .getService(Components.interfaces.nsIConsoleService)
            .logStringMessage(str+"\n");
        }
function openConnection()
	{
	var connection=null;
	try
		{
		var file = Components.classes["@mozilla.org/file/directory_service;1"]
				.getService(Components.interfaces.nsIProperties)
				.get("ProfD", Components.interfaces.nsIFile);
	
		file.append("imgdb.sqlite");
	
		var storageService = Components.classes["@mozilla.org/storage/service;1"]
			.getService(Components.interfaces.mozIStorageService);
		//see https://developer.mozilla.org/en/mozIStorageConnection
		var connection = storageService.openDatabase(file);
	
		if(!connection.tableExists("version"))
			{
			connection.createTable("version","version INTEGER PRIMARY KEY AUTOINCREMENT");
			connection.executeSimpleSQL("insert or ignore into version(version) values (1)");
			}
	
		if(!connection.tableExists("imgdb"))
			{
			connection.createTable("imgdb",
				"id INTEGER PRIMARY KEY AUTOINCREMENT,"+
				"name TEXT NOT NULL UNIQUE on CONFLICT IGNORE"
				);
			connection.executeSimpleSQL("insert or ignore into imgdb(id,name) values (\'Default\',1)");
			}
	
		if(!connection.tableExists("img"))
			{
			connection.createTable("img",
				"id INTEGER PRIMARY KEY AUTOINCREMENT,"+
				"db_id TEXT NOT NULL REFERENCES imgdb(id) ON DELETE CASCADE,"+
				"url TEXT NOT NULL UNIQUE on CONFLICT IGNORE,"+
				"alt TEXT NOT NULL,"+
				"docurl TEXT NOT NULL,"+
				"doctitle TEXT NOT NULL,"+
				"width INTEGER NOT NULL,"+
				"height INTEGER NOT NULL"
				);
			}
		return connection;
		}
        catch(e)
		{
		dumpjs(e.message);
		return null;
		}
	
	}

function escapeSql(content)
        {
        var s="";
        for(var i=0;content!=null && i < content.length;++i)
                {
                switch(content.charAt(i))
                        {
                        /* http://www.sqlite.org/faq.html#q14 */
                        case '\'': s+="\'\'"; break;
                        default: s+=content.charAt(i); break;
                        }
                }
        return s;
        }

