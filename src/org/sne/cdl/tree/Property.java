package org.sne.cdl.tree;

import org.sne.cdl.query.ResultRow;

public abstract class Property {
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	// The URI of this property;
	String id;
	// The domain of this property;
	String domain;
	// The range of this property;
	String range;
	// The comment of this property
	String comment; 
	public Property(){
	}
	public Property(ResultRow data) {
		domain 	= data.get("Domain");
		range	= data.get("Range");
		comment = data.get("Comment");
		setID(data);
	}
	
	public abstract void setID(ResultRow data);
	
	public String toString(){
		return "";
	}
	// return last part of ID
	public String getName(){
		int hashIdx = id.indexOf("#");
		return hashIdx < 0 ? "" : id.substring(hashIdx+1);
	}
}
