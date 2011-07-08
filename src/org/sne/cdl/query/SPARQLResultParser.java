package org.sne.cdl.query;

import java.util.Vector;

/**
 * For some reason Sesame refuses to give me back JSON, I am going to parse the XML into Vector of HashMaps
 * To make it rather interesting I am not going to use libraries or any complicated regex. It's all simple string game.
 * @author wibisono
 * 
 */
public class SPARQLResultParser {
	
	// Encapsulating Map<String,String> just in case I wanted to use something else
	enum State {
		RESULT, BINDING, OUT
	};
	
	String XMLString;
	
	Vector<ResultRow> results = new Vector<ResultRow>();
	
	public SPARQLResultParser(String XMLString) {
		
		this.XMLString = XMLString;
		
		parseXMLString();
		
	}
	
	/**
	 * After supercomputing I'll try to fix this by using proper XML parser libraries
	 * but for the time being this poor man's parser is working. 
	 * Assuming it is only used for parsing Sesame's SPARQL-RESULT XML
	 */
	private void parseXMLString() {
			
		State currentState = State.OUT;
		String currentKey = "";
		String currentValue = "";
		int currentPos = 0, idx = -1, idx1;
		ResultRow currentData=null;
		
		while(currentPos >= 0){
			switch(currentState){
				case OUT : {
					//find result, enter RESULT
					idx = XMLString.indexOf("<result>", currentPos);
					if(idx >= 0){
						currentState = State.RESULT;
						currentPos = idx;
						currentData = new ResultRow();
						results.add(currentData);					
					} else {
						// We're done
						currentPos = -1;
					}
				}; break;
				
				case RESULT : {
					
					//find binding, enter BINDING
					idx = XMLString.indexOf("<binding", currentPos);
					idx1 = XMLString.indexOf("</result", currentPos);
					if(idx>=0 && idx <= idx1){
						currentState = State.BINDING;
						currentPos   = idx;
					} else {
						currentState = State.OUT;					
					}
				}; break;
				
				case BINDING : {
					idx = XMLString.indexOf("name='", currentPos);
					idx1 = XMLString.indexOf("'>", currentPos);
					currentKey = XMLString.substring(idx+6,idx1);
					
					currentPos = idx1;
					
					idx = XMLString.indexOf("<", currentPos);
					idx1 = XMLString.indexOf("</binding>", currentPos);
					
					if(idx >=0 && idx < idx1){
						currentPos = idx+4;
						idx = XMLString.indexOf(">", currentPos);
						idx1 = XMLString.indexOf("</", currentPos);
						currentValue = XMLString.substring(idx+1,idx1);					
					} 
					currentPos = idx1;
					currentState = State.RESULT;
					currentData.put(currentKey.trim(),currentValue.trim());				
				}
			}
		
		}
	}
	
	public Vector<ResultRow> getResults(){
		return results;
	}
	
	public static void main(String[] args) {
		String dataProperties 		=	new SesameConnector().getOWLDataPropertyDomainRangeComments();
		String objectProperties 	=	new SesameConnector().getOWLObjectPropertyDomainRangeComments();
		
		SPARQLResultParser parser = new SPARQLResultParser(dataProperties);
		SPARQLResultParser parser1 = new SPARQLResultParser(objectProperties);
		
		for(ResultRow row : parser.getResults()){
			System.out.println(row);
		}
		System.out.println();
		for(ResultRow row : parser1.getResults()){
			System.out.println(row);
		}
	}
}
