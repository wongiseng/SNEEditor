package org.sne.cdl.backend;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.sne.cdl.owl.OWLTransformer;
import org.sne.cdl.owl.WireItModule;
import org.sne.cdl.owl.WireItWire;
import org.sne.cdl.persist.NetworkTopology;
import org.sne.cdl.persist.SNE_PersistenceProvider;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

/**
 * This is the class responsible for generating OWL/RDF
 * OWLRDF was generated based on output from WireIT Editor.
 * @author wibisono
 *
 */
@Path("/owl")
public class SNE_OWLGenerator {
	
	@POST
	@Path("/getFormattedOWLRDF")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_JSON)  // YUI Form post set content-type to JSON
	public String getFormattedOWLRDF(@FormParam("objString") String objString) throws JSONException, OWLOntologyCreationException, OWLOntologyStorageException, IOException{
		JSONObject obj = new JSONObject(objString);
		JSONArray modulesArray = obj.getJSONArray("modules");
		JSONArray wiresArray = obj.getJSONArray("wires");

		//TODO: Actually we also receive ob.getJSONObject("properties") which should in turns contains description and name of owl ontology.
		//We need to implement and make use of this in header of OWL.
		
		Vector<WireItModule> modules = extractModuleString(modulesArray);
		Vector<WireItWire> wires = extractWireString(wiresArray, modules);
		
		String result = generateOWL(modules,wires);
		result = XhtmlRendererFactory.getRenderer(XhtmlRendererFactory.XML).highlight("Cinegrid Generated OWL", result,"UTF-8", false);
	
		return  result;
	}

	@POST
	@Path("/getRawOWLRDF")
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_JSON)  // YUI Form post set content-type to JSON
	public String getRawOWLRDF(@FormParam("objString") String objString) throws JSONException, OWLOntologyCreationException, OWLOntologyStorageException, IOException{
	
		JSONObject obj = new JSONObject(objString);
		JSONArray modulesArray = obj.getJSONArray("modules");
		JSONArray wiresArray = obj.getJSONArray("wires");

		//TODO: Actually we also receive ob.getJSONObject("properties") which should in turns contains description and name of owl ontology.
		//We need to implement and make use of this in header of OWL.
		
		Vector<WireItModule> modules = extractModuleString(modulesArray);
		Vector<WireItWire> wires = extractWireString(wiresArray, modules);
		
		String result = generateOWL(modules,wires);
		
		return  result;
	}
	/**
	 * This REST endpoint provides a way for user to view their generated Network Topology in raw OWL format (without synstax highlighting/formatting)
	 * @param name
	 * @return
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 * @throws JSONException
	 * @throws IOException
	 */
	@GET
	@Path("/view/{name}.owl")
	@Produces(MediaType.TEXT_XML)
	public String viewOWL(@PathParam("name")String name) throws OWLOntologyCreationException, OWLOntologyStorageException, JSONException, IOException{
		// Load from persistent backend the network topology stored with the name Name
		PersistenceManager pm = SNE_PersistenceProvider.getPM();
		Query queryNetTopologyName = pm.newQuery("select from "
				+ NetworkTopology.class.getName()
				+ " where name == topologyName  parameters String topologyName ");
		
		//FIXME: The name was stored with additional double quote enclosing it. 
		//Without double quote enclosing name for a key, we can retrieve Network topology 
		name = "\"" +name+"\"";
		@SuppressWarnings("unchecked")
		List<NetworkTopology> results = (List<NetworkTopology>) queryNetTopologyName.execute(name);

		// Current topology, either new or updating existing
		NetworkTopology currentTopology = new NetworkTopology();  
				
		// If it is already there we are only updating the first one found 
		if(results != null  && results.size() > 0){
			 currentTopology = results.get(0);	
		}
		
		JSONObject obj = new JSONObject(currentTopology.getWorking().getValue());
		JSONArray modulesArray = obj.getJSONArray("modules");
		JSONArray wiresArray = obj.getJSONArray("wires");

		//TODO: Actually we also receive ob.getJSONObject("properties") which should in turns contains description and name of owl ontology.
		//We need to implement and make use of this in header of OWL.
		
		Vector<WireItModule> modules = extractModuleString(modulesArray);
		Vector<WireItWire> wires = extractWireString(wiresArray, modules);
		
		// Raw unformatted OWL
		return generateOWL(modules,wires);
		
	}

	@GET
	@Path("/view/{name}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public String viewJSON(@PathParam("name")String name){
		// Load from persistent backend the network topology stored with the name Name
		PersistenceManager pm = SNE_PersistenceProvider.getPM();
		Query queryNetTopologyName = pm.newQuery("select from "
				+ NetworkTopology.class.getName()
				+ " where name == topologyName  parameters String topologyName ");
		
		
		name = "\"" +name+"\"";
		@SuppressWarnings("unchecked")
		List<NetworkTopology> results = (List<NetworkTopology>) queryNetTopologyName.execute(name);

		// Current topology, either new or updating existing
		NetworkTopology currentTopology = new NetworkTopology();  
				
		// If it is already there we are only updating the first one found 
		if(results != null  && results.size() > 0){		
			 currentTopology = results.get(0);	
		}
		
		return currentTopology.toString();
	}
	/**
	 * This is actually where the main action is orchestrated
	 * Each modules are transformed and declared as OWL classes 
	 * and all the wire are transformed and declared as ObjectProperties
	 * 
	 * @param modules
	 * @param wires
	 * @return
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyStorageException
	 */
	private String generateOWL(Vector<WireItModule> modules,
			Vector<WireItWire> wires) throws OWLOntologyCreationException, OWLOntologyStorageException {
		
		OWLTransformer transformer = new OWLTransformer();
		
		for(WireItModule m : modules)
			transformer.declareClassAndIndividu(m);
		
		for(WireItWire w : wires)
			transformer.declareObjectProperty(w);
		
		return transformer.getResult().toString();
	}
	
	/**
	 * Parse modulesJSONString array into individual modules components
	 * @param modulesJSONString
	 * @return
	 * @throws JSONException 
	 */
	private Vector<WireItModule> extractModuleString(JSONArray modules) throws JSONException {
		Vector<WireItModule> result = new Vector<WireItModule>();
				
		for(int i=0;i<modules.length();i++){
			WireItModule newModule = new WireItModule(modules.getJSONObject(i)); 
			result.add(newModule);
		}
		return result;
	}
	
	/**
	 * Parse wireJSONString array into individual WireItWires object
	 * @param wireJSONString
	 * @param modules 
	 * @return
	 * @throws JSONException 
	 */
	private Vector<WireItWire> extractWireString(JSONArray wires , Vector<WireItModule> modules) throws JSONException {
		Vector<WireItWire> result = new Vector<WireItWire>();
				
		for(int i=0;i<wires.length();i++){
			result.add(new WireItWire(wires.getJSONObject(i),modules));
		}
		return result;
	}
	

}
