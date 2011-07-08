package org.sne.cdl.module;

import java.io.Serializable;

public class Field implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9067308588972201237L;
	String type="type";
	String label="label";
	String name="name";
	String tooltip = "tooltip";
	String urlid="";
	
	boolean wirable=false;
	Value value;
	
	public Field(){
		type="type";
		label="label";
		name="name";
		tooltip="tooltip";
	}
	public String getTooltip() {
		return tooltip;
	}
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isWirable() {
		return wirable;
	}
	public void setWirable(boolean wirable) {
		this.wirable = wirable;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	
	public String toString(){
		return 			"\n      {"+
			   			"\n        \"type\"  		: \""+type +"\","	+
			   			"\n        \"label\" 		: \""+label+"\","	+
			   			"\n        \"name\"  		: \""+name +"\" "	+
			   			// Making sure that Name is required so validation will be provided by inputEx
			   			(name.equals("Name") ? ",\n        \"required\"		: \"true\"" : "" )+ // FIXME: think of a better way to do this
			   			// Initialize base address
			   			(name.equals("BaseAddress") ? ",\n        \"value\"		: \""+urlid+"\"" : "" )+ 
			   			
			   "\n      }";
	}
	public void setID(String id)
	{
		urlid=id;
		
	}
}
