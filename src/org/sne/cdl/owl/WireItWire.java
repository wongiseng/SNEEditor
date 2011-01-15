package org.sne.cdl.owl;

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
			
	}
	
	class Port {
			public Port(JSONObject jsonObject, Vector<WireItModule> modules) throws JSONException {
					moduleId = new Integer(jsonObject.getInt("moduleId"));
					terminal = jsonObject.getString("terminal");
					className = modules.get(moduleId).getClassName();
					individuName = modules.get(moduleId).getInstanceName();
			}
			// Either source or destination module Index
			int moduleId;
			// Object properties string
			String terminal;
			
			String className;
			String individuName;
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
}
