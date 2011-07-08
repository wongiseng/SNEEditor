package org.sne.cdl.backend;

import java.io.IOException;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.sne.cdl.persist.NetworkTopology;
import org.sne.cdl.persist.SNE_PersistenceProvider;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.org.json.JSONException;

@Path("/backend")
public class SNE_PersistentBackend
{
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public String list()
	{
		PersistenceManager pm = SNE_PersistenceProvider.getPM();
		
		
		// We currently ignore language parameter, because it is by default will
		// be SNE Language
		// Probably we need to remove it from client side

		/* This is getting all available persisted NetworkTopology */
		/*
		 * Once we are using login and considering creators, we should do a
		 * query instead
		 */
		Extent<NetworkTopology> extent = pm.getExtent(NetworkTopology.class, false);

		// Turning things into string is not how it ought to be, but I have no clear Idea how to
		// Use GenericEntity from jersey to return List <NetworkTopology>
		// Until I found out how to do that exactly, this conversion to string will do
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (NetworkTopology n : extent){
			buffer.append('{');
			buffer.append("\"name\" : ").append(n.getName()).append(',');
			buffer.append("\"creator\" : ").append(n.getCreator()).append(',');
			buffer.append("\"language\" : \"").append(n.getLanguage()).append("\",");
			buffer.append("\"working\" : ").append(n.getWorking().getValue());
			buffer.append("},");
		}
		if(buffer.length()>1)
			buffer.deleteCharAt(buffer.length()-1);
		buffer.append("]");
		
		
		return buffer.toString();
	}

	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	// YUI Form post set content-type to JSON
	public String saveTopology(@FormParam("name") String name,
			@FormParam("working") String working,
			@FormParam("language") String language) throws JSONException,
			OWLOntologyCreationException, OWLOntologyStorageException,
			IOException
	{
		PersistenceManager pm = SNE_PersistenceProvider.getPM();
		Query queryNetTopologyName = pm.newQuery("select from "
				+ NetworkTopology.class.getName()
				+ " where name == topologyName  parameters String topologyName ");
		
		
		@SuppressWarnings("unchecked")
		List<NetworkTopology> results = (List<NetworkTopology>) queryNetTopologyName.execute(name);
		System.out.println("What are save result "+results);
		// Current topology, either new or updating existing
		NetworkTopology currentTopology = new NetworkTopology();  
		
		// If it is already there we are only updating the first one found 
		if(results != null  && results.size() > 0)
			 currentTopology = results.get(0);	
		
		try {
			pm.currentTransaction().begin();
			currentTopology.setName(name);
			currentTopology.setWorking(new Text(working));
			pm.makePersistent(currentTopology);
			pm.currentTransaction().commit();
		} catch (Exception e) {
			pm.currentTransaction().rollback();
			e.printStackTrace();
			return "{\"fail\" :\"Failed to save "+e.getMessage()+"\"}";
		} finally{ 
			pm.close();
		}
		
		return "{\"success\" : \"OK\"}";
	}
	
	@DELETE
	@Path("/delete")
	@Produces(MediaType.TEXT_PLAIN)
	// YUI Form post set content-type to JSON
	public String deleteTopology(@FormParam("name") String name,
			@FormParam("language") String language) 
	{
		PersistenceManager pm = SNE_PersistenceProvider.getPM();
		Query queryNetTopologyName = pm
				.newQuery("select from "
						+ NetworkTopology.class.getName()
						+ " where name == topologyName  parameters String topologyName ");

		@SuppressWarnings("unchecked")
		List<NetworkTopology> results = (List<NetworkTopology>) queryNetTopologyName.execute(name);

		for (NetworkTopology n : results) {
			System.out.println("Deleting : " + n);
			pm.deletePersistent(n);
		}
		
		return "{ \"ret\" : \"Succeeded deleting "+results.size()+" objects\"}";
	}

	@GET
	@Path("/deleteall")
	@Produces(MediaType.TEXT_PLAIN)
	public String deleteAll() throws JSONException, OWLOntologyCreationException,
			OWLOntologyStorageException, IOException
	{

		PersistenceManager pm = SNE_PersistenceProvider.getPM();
		Extent<NetworkTopology> extent = pm.getExtent(NetworkTopology.class,
				false);

		for (NetworkTopology n : extent) {
			System.out.println("Deleting All : " + n);
			pm.deletePersistent(n);
		}
		
		return "All deleted";

	}

	// This is just experimental not really used.
	@GET
	@Path("/add/{name}")
	@Produces(MediaType.TEXT_HTML)
	public String add(@PathParam("name") String name)
	{

		PersistenceManager pm = SNE_PersistenceProvider.getPM();

		NetworkTopology newTopology = new NetworkTopology();

		newTopology.setName("Bubung");

		newTopology.setCreator(name);

		try {
			pm.makePersistent(newTopology);
		} catch (Exception e) {
			return "Payeeeh";
		}

		return "Goed bleh, hajar aje " + name;
	}

}
