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
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Transforming WireItModule Objects representation into OWL using OWL API
 * Before using this class WireItModules and WireItWires are assumed to be already extracted.
 * In detail, see SNE_OWL_Generator.java on how this class is used.
 * 
 * @author Wibisono
 *
 */
public class OWLTransformer {
	
	OWLOntologyManager owlManager;
	PrefixManager defaultPrefixManager; 
	OWLDataFactory owlFactory;
	OWLOntology ontology;
	/* FIXME: These should be configurable */
	static final String DEFAULT_BASE_ADDRESS	= "http://fp7-novi.eu/im.owl";
	static final String DEFAULT_PREFIX			= "http://fp7-novi.eu/im.owl#";
	
	public OWLTransformer() throws OWLOntologyCreationException {
	
		// Default base address
		IRI defaultBaseAddress = IRI.create(DEFAULT_BASE_ADDRESS);

		// Default when prefix are not defined
		defaultPrefixManager = new SNEPrefixManager(DEFAULT_PREFIX);

		owlManager	=	OWLManager.createOWLOntologyManager();
		owlFactory 	= 	owlManager.getOWLDataFactory();
		
		ontology 	= 	owlManager.createOntology(defaultBaseAddress);
		
		
	}

	public void declareClassAndIndividu(WireItModule m) {
		
		PrefixManager currentPrefix = new SNEPrefixManager(m.getDataPropertiesMap().get("BaseAddress"));
		
		// Instantiate OWL Class according to the type m.name the correct prefix to use here is the default prefix manager.
		OWLClass owlClass = owlFactory.getOWLClass(m.getClassName(),defaultPrefixManager);		
		
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
			
			OWLDataProperty dataProperty = owlFactory.getOWLDataProperty(dataPropertyName, defaultPrefixManager);
			
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
		
		SNEPrefixManager domainPrefix = new SNEPrefixManager(w.getDomainBaseAddress());
		SNEPrefixManager rangePrefix = new SNEPrefixManager(w.getRangeBaseAddress());
		// Instantiate Domain Class, Instantiating class is always with defaultPrefix Manager !!!
		OWLClass domainClass = owlFactory.getOWLClass(w.getDomainClass(),defaultPrefixManager);
		// Instantiate Domain Individual
		OWLNamedIndividual domainIndividu = owlFactory.getOWLNamedIndividual(w.getDomainIndividu(), domainPrefix);
		// Assert that this Domain Individual is instance of Domain Class
		OWLClassAssertionAxiom domainClassAxiom = owlFactory.getOWLClassAssertionAxiom(domainClass, domainIndividu);
		
		// Instantiate Range Class Instantiating class is always with defaultPrefix Manager !!!
		OWLClass rangeClass = owlFactory.getOWLClass(w.getRangeClass(),defaultPrefixManager);
		// Instantiate Range Individual
		OWLNamedIndividual rangeIndividu = owlFactory.getOWLNamedIndividual(w.getRangeIndividu(), rangePrefix);
		// Assert that this Range Individual is instance of rangeClass
		OWLClassAssertionAxiom rangeClassAxiom  = owlFactory.getOWLClassAssertionAxiom(rangeClass, rangeIndividu);
		
		//Instantiate Object Property, now I am not sure which prefix manager to use, the range ? the domain? or the default?
		OWLObjectProperty objectProperty = owlFactory.getOWLObjectProperty(w.getObjectProperty(), defaultPrefixManager);
		
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
