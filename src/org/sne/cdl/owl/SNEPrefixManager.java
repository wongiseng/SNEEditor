package org.sne.cdl.owl;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * This class is created to override default prefix manager from the OWL API.
 * The original behaviour of Default prefix manager is to always search for prefix whenever an identifier contains ":" colon
 * Original default manager will throw exception if prefix is not defined.
 * 
 * 
 * In this version of prefix manager, instead of throwing exception, getIRI will use default prefix.
 * 
 * @author wibisono
 *
 */
public class SNEPrefixManager extends DefaultPrefixManager
{
	
	public SNEPrefixManager(String string)
	{
		// TODO Auto-generated constructor stub
		super(string);
	}

	@Override
    public IRI getIRI(String curie) {
        if(curie.startsWith("<")) {
            return IRI.create(curie.substring(1, curie.length() - 1));
        }
        int sep = curie.indexOf(':');
        if(sep == -1) {
            if (getDefaultPrefix() != null) {
                return IRI.create(getDefaultPrefix() + curie);
            }
            else {
                return IRI.create(curie);
            }
        }
        else {
            String prefixName = curie.substring(0, sep + 1);
            if(!containsPrefixMapping(prefixName)) {
            	// Original Behaviour would throw exception.
                //throw new RuntimeException("Prefix not registered for prefix name: " + prefixName);
            	if (getDefaultPrefix() != null) {
                    return IRI.create(getDefaultPrefix() + curie);
                }
                else {
                    return IRI.create(curie);
                }
            }
            String prefix = getPrefix(prefixName);
            String localName = curie.substring(sep + 1);
            return IRI.create(prefix + localName);
        }
    }
}
