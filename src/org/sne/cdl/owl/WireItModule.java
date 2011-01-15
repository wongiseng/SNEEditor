package org.sne.cdl.owl;

import java.util.HashMap;
import java.util.Iterator;

import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

/**
 * Handling results from WireIt JSONString
 * @author Wibisono
 *
 */
public class WireItModule {

	// Class Name
	String className;
	
	// Class ID
	String classID;
	
	// Instance Name
	String instanceName;

	
	// Maps containing by default Name as key, and DataProperties if the module has fields
	// Missing information of type of this values. Probably should have been obtained from other part of WireIt result (original language)
	HashMap<String, String> valuesMap = new HashMap<String, String>();
	
	// Maps containing field type information for each datatype properties within this module.
	HashMap<String, String> fieldsMap = new HashMap<String,String>();
	
	/**
	 * Constructor based on JSONObject which contains name and map of DataProperties
	 * @param object
	 * @throws JSONException
	 */
	public WireItModule(JSONObject object) throws JSONException {
		// Parse and update this thing
		className = object.getString("name");
		classID	  = object.getString("id");
		
		JSONObject jsonValues = object.getJSONObject("value");
		@SuppressWarnings("rawtypes")
		Iterator vals = jsonValues.keys();
		
		while(vals.hasNext()){
			String key = vals.next().toString();
			
			// Make sure values only contains DataProperties
			if(key.equals("Name")){
				instanceName = jsonValues.getString(key);
			} else 
				valuesMap.put(key, jsonValues.getString(key));
		}
		
		try {
    		JSONObject fieldsObj = object.getJSONObject("fields");
    		@SuppressWarnings("rawtypes")
    		Iterator fieldKeys = fieldsObj.keys();
    		
    		while(fieldKeys.hasNext()){
    			String key = fieldKeys.next().toString();
    			fieldsMap.put(key, fieldsObj.getString(key));
    		}
		} catch(JSONException e){
			
		}
	}
	
	public String getClassID(){
		return classID;
	}
	
	public String getPrefixString(){
		int hashIndex = classID.indexOf('#');
		if(hashIndex < 0) return classID;
		return classID.substring(0, hashIndex+1);
	}
	public String getClassName(){
		return className;
	}
	public String getInstanceName(){
		return instanceName;
	}
	
	public HashMap <String,String> getDataPropertiesMap(){
			return valuesMap;
	}
	
	public String getTypeOf(String fieldName){
		return fieldsMap.get(fieldName);
	}
}
