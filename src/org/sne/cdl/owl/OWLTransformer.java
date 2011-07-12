package org.sne.cdl.owl;

import java.util.Map;

import org.apache.geronimo.mail.util.StringBufferOutputStream;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Transforming WireItModule Objects representation into OWL using OWL API
 * @author Wibisono
 *
 */
public class OWLTransformer {
	
	OWLOntologyManager owlManager;
	PrefixManager prefixManager; 
	OWLDataFactory owlFactory;
	OWLOntology ontology;
	
	public OWLTransformer() throws OWLOntologyCreationException {
		/**
		 * FIXME: This should be configurable I suppose, don't do it this way for everything.
		 */
		IRI iri = IRI.create("http://fp7-novi.eu/im.owl");
		owlManager=OWLManager.createOWLOntologyManager();
		// Default when prefix are not defined
		prefixManager = new DefaultPrefixManager("http://fp7-novi.eu/im.owl#");
		owlFactory = owlManager.getOWLDataFactory();
		//ontology = owlManager.loadOntologyFromOntologyDocument(iri);
		ontology = owlManager.createOntology(iri);
		
	}

	public void declareClassAndIndividu(WireItModule m) {
		
		PrefixManager currentPrefix = new DefaultPrefixManager(m.getDataPropertiesMap().get("BaseAddress"));
		
		// Instantiate OWL Class according to the type m.name
		OWLClass owlClass = owlFactory.getOWLClass(m.getClassName(),currentPrefix);		
		
		// Instantiate OWL Individual according to instance name
		
		OWLNamedIndividual owlIndividual = owlFactory.getOWLNamedIndividual(m.getInstanceName(), currentPrefix);
		
		// Assert that this OWL Individual is instance of OWL Class
		OWLClassAssertionAxiom classAxiom = owlFactory.getOWLClassAssertionAxiom(owlClass, owlIndividual);
		
		// Assert this axiom into ontology
		owlManager.addAxiom(ontology, classAxiom);
		
		// Position is stored as an annotation within OWL/RDF
		OWLAnnotation positionAnnotation = owlFactory.getOWLAnnotation(
				owlFactory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
				owlFactory.getOWLLiteral("Position : "+m.positionStr, "en"));
		
		// Specify that the pizza class has an annotation - to do this we attach an entity annotation using
		// an entity annotation axiom (remember, classes are entities)
		OWLAxiom positionAxiom = owlFactory.getOWLAnnotationAssertionAxiom(owlIndividual.getIRI(), positionAnnotation);
		owlManager.addAxiom(ontology, positionAxiom);
		
		// Create dataProperty for each values/field.
		Map<String, String> dataPropertyMap = m.getDataPropertiesMap();
		
		
		if(dataPropertyMap != null)
		for(String dataPropertyName : dataPropertyMap.keySet()){
			if(dataPropertyName.equals("BaseAddress")) continue;
			String currentType = m.getTypeOf(dataPropertyName);
			
			// Instantiate data property for this data property name 
			// CHECK: Not quite sure if currentPrefix is the right one for this, since it is from the class
			
			OWLDataProperty dataProperty = owlFactory.getOWLDataProperty(dataPropertyName, currentPrefix);
			
			// By default use literal/string
			OWLLiteral dataPropertyValue = owlFactory.getOWLLiteral(dataPropertyMap.get(dataPropertyName));
		
			// GEMBLUNGISME ?
			if(currentType != null){
    			if (currentType.endsWith("float")) {
    				dataPropertyValue = owlFactory.getOWLLiteral(new Float(dataPropertyMap.get(dataPropertyName)));
    			} else if (currentType.endsWith("double")) {
    				dataPropertyValue = owlFactory.getOWLLiteral(new Double(dataPropertyMap.get(dataPropertyName)));
    			} else if (currentType.endsWith("int")) {
    				dataPropertyValue = owlFactory.getOWLLiteral(new Integer(dataPropertyMap.get(dataPropertyName)));
    			}
			}
			
			// Dodol
			OWLDataPropertyAssertionAxiom dataPropertyAssertion = owlFactory.getOWLDataPropertyAssertionAxiom(dataProperty, owlIndividual, dataPropertyValue);
			
			// Assert this axiom into ontology
			owlManager.addAxiom(ontology, dataPropertyAssertion);
			
		}
	}

	public void declareObjectProperty(WireItWire w) {
		//System.out.println("Check WireItWire domain "+ w.getDomainClass()+" "+w.getDomainIndividu());
		//System.out.println("Check WireItWire range "+ w.getRangeClass()+" "+w.getRangeIndividu());
		//System.out.println("Check WireItWire OP "+ w.getObjectProperty());
			
		// Instantiate Domain Class
		OWLClass domainClass = owlFactory.getOWLClass(w.getDomainClass(),prefixManager);
		// Instantiate Domain Individual
		OWLNamedIndividual domainIndividu = owlFactory.getOWLNamedIndividual(w.getDomainIndividu(), prefixManager);
		// Assert that this Domain Individual is instance of Domain Class
		OWLClassAssertionAxiom domainClassAxiom = owlFactory.getOWLClassAssertionAxiom(domainClass, domainIndividu);
		
		// Instantiate Range Class
		OWLClass rangeClass = owlFactory.getOWLClass(w.getRangeClass(),prefixManager);
		// Instantiate Range Individual
		OWLNamedIndividual rangeIndividu = owlFactory.getOWLNamedIndividual(w.getRangeIndividu(), prefixManager);
		// Assert that this Range Individual is instance of rangeClass
		OWLClassAssertionAxiom rangeClassAxiom  = owlFactory.getOWLClassAssertionAxiom(rangeClass, rangeIndividu);
		
		//Instantiate Object Property
		OWLObjectProperty objectProperty = owlFactory.getOWLObjectProperty(w.getObjectProperty(), prefixManager);
		
		// Assert that domain Individu and Range Individu is associated with this ObjectProperty
		OWLObjectPropertyAssertionAxiom 
		objectPropertyAssertion = owlFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, domainIndividu, rangeIndividu);

		
		// So far we are just instantiating classes without adding them to the ontology.
		// Now use ontology manager to include all axioms that have been asserted.
		owlManager.addAxiom(ontology, domainClassAxiom);
		owlManager.addAxiom(ontology, rangeClassAxiom);
		owlManager.addAxiom(ontology, objectPropertyAssertion);
		
		
	}
	

	public StringBuffer getResult() throws OWLOntologyStorageException {
		StringBuffer result = new StringBuffer();
		StringBufferOutputStream stringBufferOutputStream = new StringBufferOutputStream(result);
		owlManager.saveOntology(ontology, stringBufferOutputStream);
		return result;
	}

}
