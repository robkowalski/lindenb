/**
 * IsA 
 */
function IsA()
        {
        }

IsA.URI=function(uri)
        {
        try
                {
                var  nsURI = Components
                .classes["@mozilla.org/network/simple-uri;1"]
                .getService(Components.interfaces.nsIURI);
                nsURI.spec = uri;
                return true;
                }
        catch(err2)
                {
                return false;
                }
        }




 