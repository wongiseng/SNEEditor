package org.sne.cdl.tree;

import org.sne.cdl.query.ResultRow;

public class ObjectProperty extends Property {
	public ObjectProperty(ResultRow r) {
		super(r);
		setID(r);
	}
	public void setID(ResultRow data){
		id 		= data.get("ObjectProperty");
	}
}
