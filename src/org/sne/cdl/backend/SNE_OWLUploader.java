package org.sne.cdl.backend;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.sne.cdl.owl.OWLDomainUnionOfInference;
import org.sne.cdl.owl.OWLRangeUnionOfInference;
import org.sne.cdl.owl.WireItGraphConstructor;

/**
 * Backend processing responsible for uploading local file and generating WireIT modules from owl:Classes found in 
 * local files.
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
	public String uploadOWL(@FormParam("Filedata") String Filedata, @FormParam("Filename") String Filename) throws OWLOntologyCreationException{
		
		System.out.println("Filename : "+ Filename);
		WireItGraphConstructor constructor = new WireItGraphConstructor(Filedata, Filename);
		
		return constructor.getWireItGraph();
	}	
	
	
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Path("/rdf")
	public String uploadRDF(@QueryParam("Filedata") String Filedata){
		return Filedata;
	}
	
	@GET
	@Path("/inferUnion")
	public String inferUnion(){
		//Hackish, need to refactor this.
		OWLDomainUnionOfInference domainUnionInference = new OWLDomainUnionOfInference("http://dev.adaptivedisclosure.org","NOVI");
		OWLRangeUnionOfInference rangeUnionInference = new OWLRangeUnionOfInference("http://dev.adaptivedisclosure.org","NOVI");
		
		try {
			domainUnionInference.addInferenceTripleStatements();
			rangeUnionInference.addInferenceTripleStatements();
		}catch(Exception e){
			return "Failed to add inference statement "+ e.getMessage();
		}
		return "<html>Inferred union statements inserted for both range and domains </html>";
	}

	
}
