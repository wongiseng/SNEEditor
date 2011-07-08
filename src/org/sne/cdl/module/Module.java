package org.sne.cdl.module;

import java.io.Serializable;
import java.util.Vector;

import org.sne.cdl.tree.DataProperty;
import org.sne.cdl.tree.ObjectProperty;



public class Module implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3474411004979434321L;
	String name="";
	Container container= new Container();
	
	public String getId() {
		return id;
	}

	String id=""; // This will be the URI. Not in the original WireIt module JSON description but I need it.
	
	public Module(){
		container = new Container();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		container.setTitle(name);
		container.setLegend("Data properties of "+name);
	}
	public Container getContainer() {
		return container;
	}
	public void setContainer(Container container) {
		this.container = container;
	}

	public void setId(String ID){
		id = ID;
		if(id == null) return ;
		int hashIdx = id.indexOf("#");
		if(hashIdx>=0) setName(id.substring(hashIdx+1));
		else setName(id); 
	}
	
	/**
	 * Add dataType property into this module. In module terminology this is a field.
	 * @param dataProperty
	 */
	public void addDataProperty(DataProperty dataProperty) {
		Field newField = new Field();
		newField.setName(dataProperty.getName());
		newField.setLabel(dataProperty.getName());
		
		String rangeType = dataProperty.getFirstRange();		
		newField.setType(getFieldTypeFromRange(rangeType));
			
		newField.setTooltip(dataProperty.getComment());
		String dpID = dataProperty.getId();
		if(dpID.lastIndexOf('#')>=0) dpID= dpID.substring(0,dpID.lastIndexOf("#")+1);
		newField.setID(dpID);
		container.addField(newField);
	}
	
	
	/*
	 * Conversion from the data type property range which is defined in XMLSchema#type into
	 * type that is understood by InputEx llibrary to generate the forms.
	 */
	private String getFieldTypeFromRange(String rangeType)
	{
		if(rangeType == null) return "string";
		rangeType = rangeType.toLowerCase();
		if(rangeType.endsWith("int") || rangeType.endsWith("integer") )
			return "integer";
		if(rangeType.endsWith("float"))
			return "float";
		if(rangeType.endsWith("double"))
			return "double";		
		return "string";
	}

	// Adding this object property as an output terminal
	public void addObjectPropertyDomain(ObjectProperty objectProperty) {
		Terminal newTerminal = new Terminal();
		newTerminal.setDirection(new int[]{0,1});
		newTerminal.setName(objectProperty.getName());
		
		// The domain will be output port, DD config sets the type
		newTerminal.setDdConfig(new DDConfig(objectProperty.getName(),"Output"));
		
		container.addTerminal(newTerminal, false);
	}

	public Vector<Terminal> getOutTerminals() {
		return container.outTerminals;
	}

	public Vector<Terminal> getInpTerminals() {
		return container.inpTerminals;
	}

	public void addObjectPropertyRange(ObjectProperty objectProperty) {
		Terminal newTerminal = new Terminal();
		newTerminal.setDirection(new int[]{0,-1});		
		newTerminal.setName(objectProperty.getName());
		
		// The range will be input port, DD config sets the type
		newTerminal.setDdConfig(new DDConfig(objectProperty.getName(),"Input"));
		
		container.addTerminal(newTerminal, true);
	}
	
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("{");
		result.append("\n  \"name\" : \""+name+"\", ");
		result.append("\n  \"id\"   : \""+id+"\", ");
		result.append("\n  \"container\" : "+container.toString()+",");		
		result.deleteCharAt(result.length()-1);
		result.append("\n}");

		return result.toString();
	}
	
}
