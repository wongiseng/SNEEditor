package org.sne.cdl.owl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;



import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class WireItGraphConstructor
{
	String fileData=null;
	String wireItResult = null;
	OWLOntologyManager manager= null;
	OWLDataFactory factory = null;
	OWLOntology theOntology = null;
	
	public WireItGraphConstructor(String filedata) throws OWLOntologyCreationException
	{
		fileData = filedata;
		wireItResult = fileData;
		
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		//Convert string into input stream to be used by ontology manager
		InputStream inputS = new ByteArrayInputStream(filedata.getBytes());
		theOntology = manager.loadOntologyFromOntologyDocument(inputS);		
	}
	
	public String getWireItGraph()
	{			
		
		Set<OWLClass> classes = theOntology.getClassesInSignature();
		StringBuffer result = new StringBuffer();
		// In the beginning there were light
		appendLine(result, "{");
		
		String ontologyName = theOntology.toString();
		
		
		//Ontology toString looks like this :  Ontology(<http://cinegrid.uvalight.nl/owl/cdl-amsterdam.owl> [Axioms: 95] [Logical axioms: 94])
		//With this assumption, let's parse it the interesting name part using this fragile code
		if(ontologyName.lastIndexOf("/")>=0 && ontologyName.lastIndexOf(">") > ontologyName.lastIndexOf("/"))
		ontologyName = ontologyName.substring(ontologyName.lastIndexOf("/")+1, ontologyName.lastIndexOf(">"));
		
		//appendLine(result, keyValue("name", ontologyName)+",");
		
		appendLine(result, quote("modules") + ": [");
		
		HashMap<String, Integer> modIds = new HashMap<String,Integer>();
		Vector<OWLIndividual> vectorIndividual = new Vector<OWLIndividual>();
		int index = 0;
		int posX=0, posY=0;
		
		for(OWLClass owlClass : classes){

			// Seems like this is thte only one that matters
			Set<OWLIndividual> individuals = owlClass.getIndividuals(theOntology);

			// From the class we can get module ID and module name, (which in OWL terms this is ID/Name of the OWLClass;
			// This will determine which SNEContainer GUI will be used when rendering the graph 
			String moduleID 	= owlClass.toStringID();
			String moduleName 	= lastPart(moduleID);
			
			for(OWLIndividual individu : individuals){
				
				String individuID	= individu.toStringID();
				String individuName = lastPart(individuID);
				
				// This index is needed for generating wires
				modIds.put(individuID, index++);
				vectorIndividual.add(individu);
				
				//Start defining this module
				appendLine(1, result, "{");
    				appendLine(2, result, quote("config")+": {");
    	            	appendLine(3, result, quote("position")+" : ["+posX+","+ posY+"],");
    	            	appendLine(3, result, keyValue("xtype","SNE.Container"));
    	            appendLine(2, result, "},");
    	            
    				appendLine(2,result, keyValue("id", moduleID) + "," );
    				appendLine(2,result, keyValue("name", moduleName)  + ",");
    
    				// Started to write values, which basically is obtained from data properties.
    				// Except for the individuName
    				appendLine(2,result, quote("value")+":  {");
    					
    					
    					appendLine(4,result, keyValue("Name", individuName)  + ",");
    				
        				//Need to get the name or rdf:ID of this individu
        				Set<OWLDataPropertyAssertionAxiom> dpAxioms = theOntology.getDataPropertyAssertionAxioms(individu);
        				for(OWLDataPropertyAssertionAxiom dpa : dpAxioms){
        					
        					String propertyKey 		= lastPart(dpa.getProperty().toString());
        					String propertyValue 	= dpa.getObject().getLiteral(); 
        					
        					appendLine(4,result, keyValue( propertyKey, propertyValue )+",");            					
        				}
        				
        				if(dpAxioms.size() > 0)
        						result.deleteCharAt(result.length()-1);
        				
        			//End of processing values/data properties
        			appendLine(2,result, "}");	
        			
            	appendLine(1,result, "},");
            	
            	posX += 300;
            	if(posX >= 1000){
            		posX = 0;
            		posY += 300;
            	}
			}
		}
		
		if(vectorIndividual.size() > 0) 
			result.deleteCharAt(result.length()-1);
		//End of modules array
		appendLine(result,"],");
		
		//Started to fill in properties of ontology
		appendLine(result, quote("properties") + " : {");
		appendLine(1, result, keyValue("description",theOntology.toString())+",");
		appendLine(1, result, keyValue("name",ontologyName));
		appendLine(result, "},");
		
		// Now it is time for constructing wires.
		appendLine(result, quote("wires")+ ": [");
	
    		for(OWLIndividual individu : vectorIndividual){
    				
        			Set<OWLObjectPropertyAssertionAxiom> opAxioms = theOntology.getObjectPropertyAssertionAxioms(individu);
        			for(OWLObjectPropertyAssertionAxiom opa : opAxioms){
        				String predicateID	= opa.getProperty().toString();
        				
        				appendLine(1, result, "{");
        				
            				String subjectID 	= opa.getSubject().toStringID();
            				appendLine(2, result, quote("src")+":"+ "{"+keyValue("moduleId", modIds.get(subjectID).toString())+","+
            															keyValue("terminal", lastPart(predicateID))+"},");
                    			
            				String objectID   	= opa.getObject().toStringID();		
            				appendLine(2, result, quote("tgt")+":"+ "{"+keyValue("moduleId", modIds.get(objectID).toString())+","+
        																keyValue("terminal", lastPart(predicateID))+"},");
                			
            				appendLine(2, result, keyValue("xtype","WireIt.BezierWire"));
            				
            			appendLine(1, result, "},");	
        			}
    		}
    		// remove comma
    		if(vectorIndividual.size() > 0) 
    			result.deleteCharAt(result.length()-1);
    		
		//End of wires array
		appendLine(result,"]");
		
		//In the end there were nothing left
		appendLine(result, "}");
		return result.toString();
	}
	
	// I hate to see \" within string concatenation so make it as function
	String quote(String x){
		return "\""+x+"\"";
	}
	String keyValue(String key, String value){
		return quote(key)+ "  :  " + quote(value);
	}
	String lastPart(String URI){
		String result= URI;
		if(URI.lastIndexOf("#")<0) return result; 
		result =result.substring(URI.lastIndexOf("#")+1);
		if(result.endsWith(">"))
			return result.substring(0,result.length()-1);
		return result;
	}
	void appendLine(StringBuffer result, String strAppend){
		result.append("\n"+strAppend);
	}
	void appendLine(int pad, StringBuffer result, String strAppend){
		String pads = "";for(int i=0;i<4*pad;i++)pads += " ";
		result.append("\n"+pads+strAppend);
	}
}
