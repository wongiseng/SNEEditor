package org.sne.cdl.backend;

import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import net.sf.jsr107cache.Cache;

import org.sne.cdl.module.Module;
import org.sne.cdl.module.ModuleGenerator;
import org.sne.cdl.module.ModuleGenerator.RepositoryType;

/**
 * Class containing REST endpoints for getting root nodes, generating WireIT Modules from existing ontology stored on Sesame Repository 
 * @author wibisono
 *
 */
@Path("/module")
public class SNE_WireItModuleGenerator
{	
	
	/**
	 * REST end point for getting root nodes (parentless OWL Classes or RDFS Classes) from a Sesame Repository.
	 * The server is located at @sesameURL and the repository name is @repositoryName
	 * 
	 * @param sesameURL
	 * @param repositoryName
	 * @return
	 */
	@GET 
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/root")
	public String getRootNodes(
			@QueryParam("sesameURL") String sesameURL,
			@QueryParam("repositoryName") String repositoryName
	){
		// First attempt to generate modules based on OWL Classes
		ModuleGenerator gen = new ModuleGenerator(sesameURL, repositoryName,RepositoryType.OWLClasses);
		// If we get any root, give this OWL Classes
		if(gen.hasAnyRoot()) return gen.getRootNodes();
		
		// We don't get any OWL Classes, let us try RDFS Classes
		gen = new ModuleGenerator(sesameURL, repositoryName, RepositoryType.RDFSClasses);
		// Hopefully now we get RDFSClasses
		return gen.getRootNodes();
	}	
	
	/**
	 * REST end point for generating all WireIT Modules based on all existing OWL/RDFS Classes from a Sesame Repository
	 * The server is located at @sesameURL and the repository name is @repositoryName
	 * 
	 * @param sesameURL
	 * @param repositoryName
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/get")
	public String getModuleString(
			@QueryParam("sesameURL") String sesameURL,
			@QueryParam("repositoryName") String repositoryName
	) {
		
		Cache cache = SNE_CacheManager.getCache();
		String cacheKey = "Results-"+repositoryName+"-"+sesameURL;
		if(cache.get(cacheKey) != null)
			return cache.get(cacheKey).toString();
		
		ModuleGenerator gen = new ModuleGenerator(sesameURL, repositoryName);
		if(!gen.hasAnyRoot())
			gen = new ModuleGenerator(sesameURL, repositoryName, RepositoryType.RDFSClasses);
		
		Vector<Module> modules = gen.getAllModules();
		
		// FIXME: Should I use Jackson to serialise instead of doing these string concats ?
		StringBuffer result = new StringBuffer();
		result.append("\n{");
		for (Module m : modules) {
			result.append("\n\"" + m.getId() + "\"  :");
			result.append("\n" + m.toString() + ",");
		}
		result.deleteCharAt(result.length() - 1);
		result.append("\n}");
	
		cache.put(cacheKey, result.toString());
		return result.toString();
	}
	
	/**
	 * Nothing really, just to poke and woke up the GAE container
	 * @return
	 */
	@GET
	@Path("/poke")
	@Produces(MediaType.TEXT_PLAIN)
	public String pokingJersey(){
			return "Server is now started";
	}

	
	
}
