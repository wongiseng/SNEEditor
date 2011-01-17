package org.sne.cdl.module;

import java.io.Serializable;
import java.util.Vector;

public class Container implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2581691148904019786L;

	String title = "input";
	String legend = "";
	String xtype = "SNE.Container";
	boolean collapsible=true;
	Vector<Field> fields ;
	public Vector<Terminal> terminals;
	public Vector<Terminal> outTerminals ;
	public Vector<Terminal> inpTerminals ;

	public Container() {
		terminals = new Vector<Terminal>();
		inpTerminals = new Vector<Terminal>();
		outTerminals = new Vector<Terminal>();
		fields = new Vector<Field>();
		Field newField = new Field();
		newField.setLabel("Name");
		newField.setName("Name");
		newField.setTooltip("Default name");
		newField.setType("string");
		fields.add(newField);
	}
	public Vector<Terminal> getTerminals() {
		return terminals;
	}

	public void setTerminals(Vector<Terminal> terminals) {
		this.terminals = terminals;
	}
	public void setCollasible(boolean val){
		collapsible = val;
	}
	
	public void setLegend(String legend) {
		this.legend = legend;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Vector<Field> getFields() {
		return fields;
	}

	public void setFields(Vector<Field> fields) {
		this.fields = fields;
	}

	public void addField(Field f) {
		fields.add(f);
	}

	public void addTerminal(Terminal t, boolean input){
		terminals.add(t);
		if(input) inpTerminals.add(t);
		else outTerminals.add(t);
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("{");
		result.append("\n     \"xtype\" : \"" 		+ xtype 		+ "\",");
		result.append("\n     \"title\" : \"" 		+ title 		+ "\",");
		result.append("\n     \"collapsible\" : " 	+ collapsible	+ ",");
		result.append("\n     \"legend\" : \"" 		+ legend 		+ "\"");
		if (fields.size() > 0) {
			result.append(",\n     \"fields\" : [ ");
			for (Field f : fields)
				result.append(f.toString() + ",");
			result.deleteCharAt(result.length() - 1);
			result.append("\n     ]");
		}
		if(terminals.size() > 0){
			result.append(",\n  \"terminals\"  : [");
			for(Terminal t : terminals){
				result.append("\n     {");
				result.append(t.toString());
				result.append("\n     },");
			}
			result.deleteCharAt(result.length()-1);
			result.append("\n   ]");
		}
		result.append("\n  }");
		return result.toString();
	}

}
