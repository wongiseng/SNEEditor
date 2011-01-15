package org.sne.cdl.backend;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.sne.cdl.owl.WireItGraphConstructor;

/**
 * 
 * @author wibisono
 *
 */
@Path("/upload")
public class SNE_OWLUploader
{
	/**
	 * This is the backend method responsible for processing owlFile and generating WireItWiring formats representing this current OWL File.
	 * @param owlFile
	 * @return
	 * @throws OWLOntologyCreationException 
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	@Path("/owl")
	public String uploadOWL(@FormParam("Filedata") String Filedata) throws OWLOntologyCreationException{
		
		WireItGraphConstructor constructor = new WireItGraphConstructor(Filedata);
		return constructor.getWireItGraph();
	}	
	
	
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Path("/rdf")
	public String uploadRDF(@QueryParam("Filedata") String Filedata){
		return Filedata;
	}
	

}
