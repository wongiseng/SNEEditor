package org.sne.cdl.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import org.sne.cdl.query.ResultRow;
import org.sne.cdl.query.SPARQLResultParser;
import org.sne.cdl.query.SesameConnector;
import org.sne.cdl.tree.DataProperty;
import org.sne.cdl.tree.ObjectProperty;

/**
 * This class should be the one responsible for generating WireIT modules. 
 * Modules definition will be generated from an OWL/RDF file stored in a Sesame Repository
 * 
 * @author wibisono
 * 
 */


public class ModuleGenerator {

	// Number of modules we have
	private int NModule = 0;
	
	// Module URI's
	String[] moduleUris;

	// Map from concept URI into integer ID of a module
	HashMap<String, Integer> moduleIdMap = new HashMap<String, Integer>();

	// Map from data property URI into it's object representation
	HashMap<String, DataProperty> dataPropertiesMap;

	// Map from object property URI into it's object representation
	HashMap<String, ObjectProperty> objectPropertiesMap;

	// Parent matrix relation, according to module ID's
	boolean[][] parentMatrix;

	// Vectors of modules, this is what we finally wanted.
	Vector<Module> modules = new Vector<Module>();

	public static final Logger log = Logger.getLogger(ModuleGenerator.class.getName());
	
	private SesameConnector connector = null;
	
	// Either OWL Class or RDFS Classes is used for getting tree nodes. 
	// Child of these nodes are obtained always using rdfs:subClassOf for both cases.
	public enum RepositoryType {
			OWLClasses,
			RDFSClasses
	};
	
	RepositoryType repType = RepositoryType.OWLClasses;
	
	/**
	 * Main function to get all generated modules within a repository
	 * @return
	 */
	public Vector<Module> getAllModules(){
		return modules;
	}
	
	public ModuleGenerator(String sesameURL, String repositoryName, RepositoryType type) {
		repType = type;
		connector = new SesameConnector(sesameURL, repositoryName);
		initialize();
	}
	
	public ModuleGenerator(String sesameURL, String repositoryName) {
		connector = new SesameConnector(sesameURL, repositoryName);
		initialize();
	}
	
	public void initialize() {
		initializeAllSPARQLResults();
		initializeModuleURIs();
		initializeDataProperties();
		initializeObjectProperties();
		initializeParentMatrix();

		generateModules();
	}

	/**
	 * Main module generating function
	 */
	private void generateModules() {
		// Processing all module URIs
		// Complexity: (NModules * Max(NObjectProperties, NDataProperties))
		
		for (String moduleURI : moduleUris) {
			Module newModule = new Module();
			newModule.setId(moduleURI);

			// Check if this modules is a domain for a DataProperty
			for (String dataPropertyURI : dataPropertiesMap.keySet()) {
				if (isDataPropertyOf(dataPropertyURI, moduleURI)) {
					// Add this data property as a filed within moduleURI
					newModule.addDataProperty(dataPropertiesMap.get(dataPropertyURI));
				}
			}
			//System.out.println(moduleURI);
			// Check if this modules is a domain/range for an ObjectProperty
			for (String objectPropertyURI : objectPropertiesMap.keySet()) {
				if (isDomainOfObjectProperty(objectPropertyURI, moduleURI)) {
					// Add an output port from this module with the type of this object property
					//System.out.println("	Adding output port : "+objectPropertyURI+ " because current module/Class is a domain of this property");
					newModule.addObjectPropertyDomain(objectPropertiesMap.get(objectPropertyURI));
					
				}
				if (isRangeOfObjectProperty(objectPropertyURI, moduleURI)) {
					// Add an input port to this module with the type of this
					// object property
					// System.out.println("	Adding input port : "+objectPropertyURI+ " because current module/Class is a range of this property");
					newModule.addObjectPropertyRange(objectPropertiesMap.get(objectPropertyURI));
				}
			}

			// Setting up positions of input ports
			Vector<Terminal> inputs = newModule.getInpTerminals();
			if (inputs != null) {
				int nInput = inputs.size();

				for (int i = 0; i < nInput; i++) {
					inputs.get(i).getOffsetPosition()
							.setInputPosition(i, nInput);
				}
			}
			// Setting up positions of output ports
			Vector<Terminal> outputs = newModule.getOutTerminals();
			if (outputs != null) {
				int nOutput = outputs.size();

				for (int i = 0; i < nOutput; i++) {
					outputs.get(i).getOffsetPosition()
							.setOutputPosition(i, nOutput);
				}
			}
			modules.add(newModule);
		}
	}

	/**
	 * Checking if @param dataPropertyURI is data property of @param moduleURi
	 * By looking at whether or not : - @param dataPropertyURI's domain is equal
	 * to @param moduleURI - Or @param dataPropertyURI's domain is a parent of @param
	 * moduleURI
	 * 
	 * @param dataPropertyURI
	 * @param moduleURI
	 * @return
	 */
	public boolean isDataPropertyOf(String dataPropertyURI, String moduleURI) {
		String propertyDomain = dataPropertiesMap.get(dataPropertyURI).getDomain();
		if (propertyDomain == null)
			return false;
		return propertyDomain.equals(moduleURI)
				|| isParent(propertyDomain, moduleURI);
	}

	/**
	 * Checking if @param objectPropertyURI's domain is of type @param moduleURi
	 * By looking at whether or not : - @param objectPropertyURI's domain is
	 * equal to @param moduleURI - Or @param objectPropertyURI's domain is a
	 * parent of @param moduleURI
	 * 
	 * @param objectPropertyURI
	 * @param moduleURI
	 * @return
	 */
	public boolean isDomainOfObjectProperty(String objectPropertyURI,
			String moduleURI) {
		String propertyDomain = objectPropertiesMap.get(objectPropertyURI).getDomain();
		if (propertyDomain == null)
			return false;
		// System.out.println("> Check > "+propertyDomain+ " "+ moduleURI + "  "+ isParent(propertyDomain,moduleURI));
		return propertyDomain.equals(moduleURI)
				|| isParent(propertyDomain, moduleURI);
	}

	/**
	 * Checking if @param objectPropertyURI's range is of type @param moduleURi
	 * By looking at whether or not : - @param objectPropertyURI's range is
	 * equal to @param moduleURI - Or @param objectPropertyURI's range is a
	 * parent of @param moduleURI
	 * 
	 * @param objectPropertyURI
	 * @param moduleURI
	 * @return
	 */

	public boolean isRangeOfObjectProperty(String objectPropertyURI,
			String moduleURI) {
		String propertyRange = objectPropertiesMap.get(objectPropertyURI).getRange();
		if (propertyRange == null)
			return false;
		//System.out.println("< Check < "+propertyRange+ " "+ moduleURI + "  "+ isParent(propertyRange,moduleURI));
		return propertyRange.equals(moduleURI)
				|| isParent(propertyRange, moduleURI);
	}
	/**
	 * Initializes the array moduleUris which keeps URI's of Modules. (Which is actually either owl:Class or rdfs:Class
	 * The reverse map from URI String into integer is also initialized i.e moduleIdMap
	 */
	private void initializeModuleURIs() {
		NModule = classesSPARQLResults.size();
		moduleUris = new String[NModule];

		for (int i = 0; i < NModule; i++) {
			moduleUris[i] = classesSPARQLResults.get(i).get("Module");
		}

		Arrays.sort(moduleUris);
		for (int i = 0; i < NModule; i++) {
			moduleIdMap.put(moduleUris[i], i);
		}
	}

	private void initializeObjectProperties() {
		objectPropertiesMap = new HashMap<String, ObjectProperty>();
		for (ResultRow r : objectPropertiesSPARQLResults) {
			ObjectProperty newProperty = new ObjectProperty(r);
			objectPropertiesMap.put(newProperty.getId(), newProperty);
		}
	}

	private void initializeDataProperties() {
		dataPropertiesMap = new HashMap<String, DataProperty>();
		for (ResultRow r : dataPropertiesSPARQLResults) {
			DataProperty newProperty = new DataProperty(r);
			dataPropertiesMap.put(newProperty.getId(), newProperty);
		}
	}

	private void initializeParentMatrix() {
		parentMatrix = new boolean[NModule][NModule];

		for (ResultRow pair : parentChildrenSPARQLResults) {
			Integer parentID = moduleIdMap.get(pair.get("Parent"));
			Integer childID = moduleIdMap.get(pair.get("Child"));

			if (parentID == null || childID == null)
				continue;
			parentMatrix[parentID][childID] = true;
		}
		doTransitiveClosure();
	}

	/**
	 * Using Depth First Search to check for transitive closure. Inferring the
	 * fact that A parentOf B and B parentOf C implies A parentOf C
	 */
	private void doTransitiveClosure() {
		for (int i = 0; i < NModule; i++) {
			boolean hasParent = false;
			for (int j = 0; j < NModule; j++)
				hasParent |= parentMatrix[j][i];

			// Started with all modules which has no parent
			if (!hasParent) {
				String term = getLastPart(moduleUris[i]);
				if(term.startsWith("node")) continue;
				rootNodes.add("{ \"id\" : \""+moduleUris[i]+"\", \"term\" : \""+term+"\" }");
				doDFS(i, i);
			}
		}

	}

	private String getLastPart(String uri)
	{
		if(uri == null) return "";
		int hashIdx = uri.indexOf("#");
		if(hashIdx < 0) return uri;
		return uri.substring(hashIdx+1);
	}

	/**
	 * Doing DFS. Branching to any children of curID telling them that they also
	 * have parentID as ancestor
	 * 
	 * @param parentID
	 * @param curID
	 */
	private void doDFS(int parentID, int curID) {
		for (int i = 0; i < NModule; i++)
			if (parentMatrix[curID][i]) {
				// Hey i ! you got parentID as ancestor
				parentMatrix[parentID][i] = true;
				// Let's find more descendant
				doDFS(parentID, i);
			}
	}

	/**
	 * Just checking if the @param parent is parent of @param child. Do I really
	 * need to document this ?
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	public boolean isParent(String parent, String child) {
		Integer idParent	= (moduleIdMap.get(parent));
		Integer idChild 	= (moduleIdMap.get(child));
		if(	parentMatrix != null && idParent != null && idChild != null)
			return parentMatrix[moduleIdMap.get(parent)][moduleIdMap.get(child)];
		return false;
	}

	/**
	 * Retrieving necessary SPARQLResults - list of all data properties
	 * including domain range comments - list of all objects properties
	 * including domain range comments - list of all owl classes which basically
	 * we will turn into modules (except for those intermediate Sesame nodes) -
	 * parent children relations, which are pairs of rdfs:subClassOf
	 */
	private Vector<ResultRow> dataPropertiesSPARQLResults = null;
	private Vector<ResultRow> objectPropertiesSPARQLResults = null;
	private Vector<ResultRow> classesSPARQLResults = null;
	private Vector<ResultRow> parentChildrenSPARQLResults = null;

	
	private  void initializeAllSPARQLResults() {
		
		
		switch(repType){
			case OWLClasses : 
				classesSPARQLResults 			= new SPARQLResultParser(connector.getOWLClasses()).getResults();
				dataPropertiesSPARQLResults 	= new SPARQLResultParser(connector.getOWLDataPropertyDomainRangeComments()).getResults();
				objectPropertiesSPARQLResults 	= new SPARQLResultParser(connector.getOWLObjectPropertyDomainRangeComments()).getResults();
				break;
			case RDFSClasses :
				classesSPARQLResults 			= new SPARQLResultParser(connector.getRDFSClasses()).getResults();
				dataPropertiesSPARQLResults 	= new SPARQLResultParser(connector.getRDFDataPropertyDomainRangeComments()).getResults();
				objectPropertiesSPARQLResults 	= new SPARQLResultParser(connector.getRDFObjectPropertyDomainRangeComments()).getResults();
				break;
			default :
				classesSPARQLResults =  new SPARQLResultParser(connector.getOWLClasses()).getResults();
		}
		// These one use rdfs:subClassOf works for both OWL and RDFS
		parentChildrenSPARQLResults 	= new SPARQLResultParser(connector.getParentChildren()).getResults();
			
	}
	
	/**
	 * These root nodes are initalized during transitive closure, nodes which has no parents will be root nodes.
	 */
	Vector<String> rootNodes = new Vector<String>();
	
	public boolean isEmpty(){
		return rootNodes.size() == 0 || modules.size() == 0;
	}
	
	/**
	 * Getting root nodes of this repository. Originally I rely on the ones implemented in AIDA. 
	 * Without changing sparql queries send there, some of the classes in OWL which has superclass defined on other class would not be shown as root.
	 * For example ndl:Location is subclass of GeoLocation. We wanted to have ndl:Location as root to be used in editor, without changing AIDA.
	 * Therefore this method. 
	 * @return
	 */
	public String getRootNodes()
	{
		String result = "";
		for(String x : rootNodes){
			if(result.length() > 0) result += ", ";
			result += x;
		}
		
		return "["+ result+ "]";
	}
	
	public static void main(String[] args)
	{
		ModuleGenerator gen = new ModuleGenerator("http://dev.adaptivedisclosure.org/openrdf-workbench", "sne_ndl_domain", RepositoryType.RDFSClasses);
		Vector<Module> modules = gen.getAllModules();
		System.out.println("[");
		for(int i=0;i<modules.size();i++){
		 if(i!=0) System.out.println(",");
		 System.out.println(modules.get(i));
		}
		System.out.println("]");
	}
}
