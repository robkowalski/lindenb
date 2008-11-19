package org.lindenb.swapp;

import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;

public interface ModelSource {
public Model getModel();
public void save() throws IOException;
}
