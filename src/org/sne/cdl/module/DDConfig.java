package org.sne.cdl.module;

import java.io.Serializable;
/**
 * This is the drag and drop configs to say which one can be connected to which.
 * @author wongiseng
 *
 */
public class DDConfig implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 336070779738151133L;
	
	String allowedType=null;

	public DDConfig(){
			
	}
		
	public DDConfig(String allowed){
		allowedType = allowed;
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer();		
		buff.append(" \"type\" : \""+ allowedType + "\",");
		buff.append(" \"allowedTypes\" : [\"");
		buff.append(allowedType);
		buff.append("\"]");
		return buff.toString();
	}
}
