package org.sne.cdl.query;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.ext.jaxrs.internal.core.MultivaluedMapImpl;

/**
 * Responsible for calling directly sesame repository and getting necessary
 * informations. TODO : - Use proper way of parsing query results, instead of my
 * own poor parser :)
 * 
 * @author Wibisono
 * 
 */
public class SesameConnector
{
	// private String REPOSITORY = "CinegridOWL";
	private String REPOSITORY	  = "sne_cine_ndldomain";
	private String SERVER_LOCATION = "http://dev.adaptivedisclosure.org/openrdf-workbench";

	public SesameConnector()
	{
		// using default stuffs above;
		REPOSITORY = "sne_cine_ndldomain";
		SERVER_LOCATION = "http://dev.adaptivedisclosure.org/openrdf-workbench";
	}

	public SesameConnector(String serverLocation, String repositoryName)
	{
		SERVER_LOCATION = serverLocation;
		REPOSITORY = repositoryName;
	}
	
	public void setRepository(String repository)
	{
		REPOSITORY = repository;
	}

	private String getSesameLocation()
	{
		return SERVER_LOCATION + "/repositories/" + REPOSITORY + "/query";
	}

	public String getOWLClasses()
	{
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add("queryLn", "SPARQL");
		queryParams
				.add("query",
						"PREFIX   rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
								+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
								+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
								+ "SELECT DISTINCT ?Module "
								+ "		WHERE { ?Module rdf:type owl:Class }");

		return doQuery(queryParams, getSesameLocation());
	}

	public String getRDFSClasses()
	{
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add("queryLn", "SPARQL");
		queryParams
				.add("query",
						"PREFIX   rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
								+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
								+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"							
								+ "SELECT DISTINCT ?Module "
								+ "		WHERE { ?Module rdf:type rdfs:Class }");

		return doQuery(queryParams, getSesameLocation());
	}
	public String getParentChildren()
	{
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add("queryLn", "SPARQL");
		queryParams
				.add("query",
						"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
								+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
								+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
								+ "SELECT DISTINCT ?Parent ?Child "
								+ "		WHERE { ?Child rdfs:subClassOf ?Parent}");

		return doQuery(queryParams, getSesameLocation());
	}

	/*
	 * Getting Data property as string list of DataProperties with associatetd
	 * Domain, Ranges and Comments
	 */
	public String getDataPropertyDomainRangeComments()
	{
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add("queryLn", "SPARQL");
		queryParams
				.add("query",
						"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
								+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
								+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
								+ "SELECT DISTINCT ?DataProperty ?Domain ?Range ?Comment"
								+ "		WHERE { ?DataProperty rdf:type owl:DatatypeProperty . "
								+ "		OPTIONAL {?DataProperty rdfs:domain ?Domain } . "
								+ "      OPTIONAL {?DataProperty rdfs:range ?Range }   . "
								+ "      OPTIONAL {?DataProperty rdfs:comment ?Comment } }");

		return doQuery(queryParams, getSesameLocation());
	}

	public String getRDFPropertyDomainRange()
	{
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add("queryLn", "SPARQL");
		queryParams.add("query", "");

		return "";
	}

	/*
	 * Getting Object property as string list of DataProperties with associatetd
	 * Domain, Ranges No Object properties was containing comments. Skip for
	 * now.
	 */
	public String getObjectPropertyDomainRangeComments()
	{
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add("queryLn", "SPARQL");
		queryParams
				.add("query",
						"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
								+ "PREFIX owl:<http://www.w3.org/2002/07/owl#>"
								+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
								+ "SELECT DISTINCT ?ObjectProperty ?Domain ?Range ?Comment"
								+ "		WHERE { ?ObjectProperty rdf:type owl:ObjectProperty . "
								+ "OPTIONAL { ?ObjectProperty rdfs:domain ?Domain } . "
								+ "OPTIONAL { ?ObjectProperty rdfs:range ?Range   } . "
								+ "OPTIONAL { ?ObjectProperty rdfs:comment ?Comment } "
								+ "}");

		return doQuery(queryParams, getSesameLocation());
	}

	/**
	 * At first I was using jersey-client, but it is more than I need, loading it up on GAE tooks time too long.
	 * @param params
	 * @param endPoint
	 * @return
	 */
	public String doQuery(MultivaluedMap<String, String> params, String endPoint)
	{
		StringBuffer result = new StringBuffer();
		URL url;
		URLConnection urlConn;
		DataOutputStream printout;
		
		try {
			// URL of CGI-Bin script.
			url = new URL(endPoint);
			
			// URL connection channel.
			urlConn = url.openConnection();
			
			// Let the run-time system (RTS) know that we want input.
			urlConn.setDoInput(true);
			
			// Let the RTS know that we want to do output.
			urlConn.setDoOutput(true);
			
			// Specify the content type.
			urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			// Send POST output.
			printout = new DataOutputStream(urlConn.getOutputStream());
			
			StringBuffer paramString = new StringBuffer();
			for(String key : params.keySet()){
				if(paramString.length()>0) paramString.append('&');	
				paramString.append(key);
				paramString.append("=");
				paramString.append(URLEncoder.encode(params.getFirst(key), "utf-8"));
			}
			printout.writeBytes(paramString.toString());
			printout.flush();
			printout.close();
			
			BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
			byte buff[] = new byte[512];
			int countRead=0;
			countRead = bis.read(buff);
			while(countRead > 0){
				for(int i=0;i<countRead;i++)
					result.append((char)buff[i]);
				// Blocking
				countRead = bis.read(buff);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	public static void main(String[] args)
	{
		SesameConnector connector = new SesameConnector(
				"http://dev.adaptivedisclosure.org/openrdf-workbench",
				"ndl_vpn_rdf");
		System.out.println("RDFSClasses\n==========");
		System.out.println(connector.getRDFSClasses());
		System.out.println("DataProperties\n==========");
		System.out.println(connector.getParentChildren());
		System.out.println("ObjectProperties\n==========");
		System.out.println(connector.getObjectPropertyDomainRangeComments());
		System.out.println("ParentChildren\n==========");
		System.out.println(connector.getParentChildren());

	}

}
