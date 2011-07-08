package org.sne.cdl.tree;

import java.util.HashSet;

import org.sne.cdl.query.ResultRow;

public abstract class Property {
	// Temporary hacks, I am not sure if this should allways be allowed.
	HashSet<String> domains = new HashSet<String>();
	HashSet<String> ranges = new HashSet<String>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public HashSet<String> getDomains() {
		return domains;
	}
	public void addDomain(String domain){
		domains.add(domain);
	}
	public HashSet<String> getRanges()
	{	return ranges;
	}
	public void addRange(String range) {
		ranges.add(range);
	}
	/*
	 * I did not write this following function, I disown them, and claimed that either it wasn't me or I was drunk when I wrote them.
	 */
	public String getFirstRange(){
		String result = "";
		for(String x : ranges){
			result = x;
			break;
		}
		return result;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	// The URI of this property;
	String id;
	// The range of this property;
	String range;
	// The comment of this property
	String comment; 
	public Property(){
	}
	public Property(ResultRow data) {
		domains.add(data.get("Domain"));
		ranges.add(data.get("Range"));
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
