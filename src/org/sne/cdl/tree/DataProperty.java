package org.sne.cdl.tree;

import org.sne.cdl.query.ResultRow;

public class DataProperty extends Property {
	public DataProperty(ResultRow r) {
		super(r);
		setID(r);
	}
	public void setID(ResultRow data){
		id 		= data.get("DataProperty");
	}
}
