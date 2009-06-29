package org.lindenb.bloggerdf;

import java.util.Set;



import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface RDFTypeHandler
{
public String getNamespaceURI();
public void setElement(Element node);
public Set<String> getTags();
public String getTitle();
public Document getDocument();
}
