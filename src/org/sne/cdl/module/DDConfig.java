package org.sne.cdl.module;

import java.io.Serializable;
/**
 * This is the drag and drop configs to say which one can be connected to which.
 * The allowedType is the opposite of the portNameType.
 * The portNameType is the original portName with suffix which is either -Output or -Input
 * 
 * There is a function called isValidWireTerminal in SNEWireIt.js that performs checking whenever user ar trying to connect terminals(port)
 * In this function, the DDConfig is used, and referred to as terminal.termConfig 
 * There this allowedType will be checked. And we wanted only to allow input port to be connected with output port and vice versa.
 * @author wongiseng
 *
 */

public class DDConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 336070779738151133L;
	

	String portNameType="";
	String allowedType="";
	boolean alwaysSrc=true;
	
	public DDConfig(){
			
	}
	boolean isSource(){
		return alwaysSrc;
	}
	public DDConfig(String portName, String type){
		portNameType = portName+"-"+type;
		
		if(type.endsWith("Output")){
			allowedType = portName+"-Input";
			// Output Port is always a source.
			alwaysSrc = true;
		}
		else{
			alwaysSrc = false;
			allowedType = portName+"-Output";
		}
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer();		
		buff.append(" \"type\" : \""+ portNameType + "\",");
		if(alwaysSrc){
			buff.append(" \"alwaysSrc\" : \"true\",");
		}
		buff.append(" \"allowedTypes\" : [\"");		
		buff.append(allowedType);
		buff.append("\"]");
		return buff.toString();
	}

}
