package org.sne.cdl.owl;

import java.util.HashMap;
import java.util.Vector;


import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class WireItWire {

	// Source and target port
	Port src, tgt;
		
	// Currently unforced Assertion here is that src and tgt terminals are the same since ObjectProperty must match.
	public WireItWire(JSONObject jsonObject, Vector<WireItModule> modules) throws JSONException {
			src = new Port(jsonObject.getJSONObject("src"), modules);
			tgt = new Port(jsonObject.getJSONObject("tgt"), modules);
			//System.out.println("Check wire, src, tgt"+src+" "+tgt);
	}
	static final String DEFAULT_PREFIX			= "http://fp7-novi.eu/im.owl#";
	
	class Port {
			public Port(JSONObject jsonObject, Vector<WireItModule> modules) throws JSONException {
					moduleId = new Integer(jsonObject.getInt("moduleId"));
					terminal = jsonObject.getString("terminal");
					className = modules.get(moduleId).getClassName();
					individuName = modules.get(moduleId).getInstanceName();
					HashMap<String,String> dataMap =modules.get(moduleId).getDataPropertiesMap();
					if( dataMap!= null){
						if(dataMap.get("BaseAddress")!=null)
							baseAddress = dataMap.get("BaseAddress"); 
					}
			}
			// Either source or destination module Index
			int moduleId;
			// Object properties string
			String terminal;
			
			String className;
			String individuName;
			
			// Optional base address, by default it will be defaultBaseAddress
			String  baseAddress=DEFAULT_PREFIX;
			
			public String toString(){
				return "\n\nModule ID : "+moduleId+"\nTerminal: "+terminal+"\nClassName :"+className+"\nIndividuName :"+individuName;
			}
	}
	
	public String getObjectProperty (){ 
		return src.terminal;
	}
	public String getDomainClass(){
		return src.className;
	}
	public String getRangeClass(){
		return tgt.className;
	}
	public String getDomainIndividu() {
		return src.individuName;
	}
	public String getRangeIndividu() {
		return tgt.individuName;
	}
	public String getRangeBaseAddress(){
		return tgt.baseAddress;
	}
	public String getDomainBaseAddress(){
		return src.baseAddress;
	}
}
