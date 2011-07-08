package org.sne.cdl.owl;

import java.util.HashMap;
import java.util.Vector;

import org.sne.cdl.query.ResultRow;
import org.sne.cdl.query.SPARQLResultParser;
import org.sne.cdl.query.SesameConnector;

public class OWLRangeUnionOfInference
{
	/**
	 * This class is to be used separately for performing updates of triples stored in Sesame 
	 * which contains translation of UnionOf into rdf Collection.
	 * 
	 * This class will provide a way to perform repeated query to resolve and flatten 
	 * UnionOf currently 
	 */
	String PREFIX = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" +
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

	// Getting all the problematic range
	String QUERY_RANGE_HEAD = 	"SELECT ?Range 	?Head 		WHERE {"+
								"		?Range 	rdfs:range ?fakeNode . "+
								"		?fakeNode 	owl:unionOf ?Head " +
								"} ";
	
	// Getting head rdf:rest tail relationship.
	String QUERY_HEAD_REST 	= 	"SELECT ?Head ?Rest WHERE {" +
							"		?Head rdf:rest	?Rest " +
							"} ";
	
	// Getting head rdf:first Value relationship.
	String QUERY_HEAD_FIRST	= 	"SELECT ?Head ?First WHERE {" +
							"		?Head rdf:first	?First " +
							"} ";
	
	
	String SESAME_LOCATION = "http://dev.adaptivedisclosure.org";
	String REPOSITORY_NAME = "NOVI-IM";
	
	SesameConnector connector = new SesameConnector();
	
	
	HashMap<String, String> rangeHeadMap 	= new HashMap<String, String>();
	HashMap<String, String> headRestMap 	= new HashMap<String, String>();
	HashMap<String, String> headFirstMap 	= new HashMap<String, String>();
	
	public OWLRangeUnionOfInference()
	{
		connector = new SesameConnector(SESAME_LOCATION+"/openrdf-workbench", REPOSITORY_NAME);
		initialize();
	}
	
	public OWLRangeUnionOfInference(String serverLocation, String repositoryName)
	{
		SESAME_LOCATION = serverLocation;
		REPOSITORY_NAME = repositoryName;
		connector = new SesameConnector(serverLocation+"/openrdf-workbench",repositoryName);
		initialize();
	}
	
	private void initialize()
	{
		getAllrangeHead();
		getQueryHeadRest();
		getQueryHeadFirst();
	}

	public void addInferenceTripleStatements()
	{
		String N3Statements = "";
		for(String range : rangeHeadMap.keySet()){
			String head 	= 	rangeHeadMap.get(range);
			while(headRestMap.containsKey(head)){
				N3Statements 	+=	"<"+range +">"+ " <http://www.w3.org/2000/01/rdf-schema#range> <"+headFirstMap.get(head)+ "> . \n";
				head = headRestMap.get(head);
			}
		}
		SesameConnector.addN3Statements(N3Statements, SESAME_LOCATION+"/openrdf-sesame/repositories/"+REPOSITORY_NAME+"/statements");
	}
		

	private void getQueryHeadRest()
	{
		Vector<ResultRow> headRestResult 	  = new Vector<ResultRow>();
		headRestResult = new SPARQLResultParser(connector.doQueryString(PREFIX + QUERY_HEAD_REST)).getResults();
		for(ResultRow r : headRestResult){
			headRestMap.put(r.get("Head"),r.get("Rest"));
		}
	}
	private void getQueryHeadFirst()
	{
		Vector<ResultRow> headFirstResult 	  = new Vector<ResultRow>();
		headFirstResult = new SPARQLResultParser(connector.doQueryString(PREFIX + QUERY_HEAD_FIRST)).getResults();
		for(ResultRow r : headFirstResult){
			headFirstMap.put(r.get("Head"),r.get("First"));
		}
	}
	
	private void getAllrangeHead()
	{
		Vector<ResultRow> rangeAndHeadResult = new Vector<ResultRow>();
		rangeAndHeadResult = new SPARQLResultParser(connector.doQueryString(PREFIX + QUERY_RANGE_HEAD)).getResults();
		for(ResultRow r : rangeAndHeadResult)
		{
			rangeHeadMap.put(r.get("Range"), r.get("Head"));
		}
	}

	
	public static void main(String[] args)
	{
		OWLRangeUnionOfInference resolver = new OWLRangeUnionOfInference();
		resolver.addInferenceTripleStatements();
	}
}	
