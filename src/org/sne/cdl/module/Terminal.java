package org.sne.cdl.module;

import java.io.Serializable;

public class Terminal implements Serializable {
	
	private static final long serialVersionUID = 3407204426595142592L;

	String name;
	int [] direction = new int[]{0,1};
	OffsetPosition offsetPosition = new OffsetPosition();
	DDConfig	ddConfig = new DDConfig();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int[] getDirection() {
		return direction;
	}
	public void setDirection(int[] direction) {
		this.direction = direction;
	}
	public OffsetPosition getOffsetPosition() {
		return offsetPosition;
	}
	public void setOffsetPosition(OffsetPosition offsetPosition) {
		this.offsetPosition = offsetPosition;
	}
	public DDConfig getDdConfig() {
		return ddConfig;
	}
	public void setDdConfig(DDConfig ddConfig) {
		this.ddConfig = ddConfig;
	}
	public String toString(){
		StringBuffer result = new StringBuffer();
		result.append("\n       \"name\" : \""+name+"\",");
		result.append("\n       \"direction\" : ["+direction[0]+","+direction[1]+"], ");
		result.append("\n       \"offsetPosition\" : {"+offsetPosition .toString()+"},");
		result.append("\n       \"ddConfig\" : { "+ddConfig.toString()+"}");
		return result.toString();
	}
	
}
