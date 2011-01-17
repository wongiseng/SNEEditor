package org.sne.cdl.module;

import java.io.Serializable;

public class OffsetPosition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2748559211531076893L;
	
	Integer left=null;
	Integer top=null;
	Integer right=null;
	Integer bottom=null;
		
	public Integer getLeft() {
		return left;
	}
	public void setLeft(Integer left) {
		this.left = left;
	}
	public Integer getTop() {
		return top;
	}
	public void setTop(Integer top) {
		this.top = top;
	}
	public Integer getRight() {
		return right;
	}
	public void setRight(Integer right) {
		this.right = right;
	}
	public Integer getBottom() {
		return bottom;
	}
	public void setBottom(Integer bottom) {
		this.bottom = bottom;
	}
	public String toString(){
		StringBuffer buff = new StringBuffer();		
		if(left != null) buff.append(" \"left\" : " + left+",");
		if(right != null) buff.append(" \"right\" : " + right+",");
		if(top != null) buff.append(" \"top\" : " + top+",");
		if(bottom != null) buff.append(" \"bottom\" : " + bottom+",");
		if(buff.length()>1) buff.deleteCharAt(buff.length()-1);
		return buff.toString();
	}
	public void setInputPosition(int i, int nInput) {
		top = -15;
		if(nInput == 1) {
			left = 110;
			return;
		} 
		if(nInput == 2){
			left = 90 + i*40;
			return;
		}
		if(nInput == 3){
			left = 90 + i*20;
			return;
		}
		left = 80 + i*20;
	}
	public void setOutputPosition(int i, int nOutput) {
		bottom = -15;
		if(nOutput == 1) {
			left = 110;
			return;
		} 
		if(nOutput == 2){
			left = 90 + i*40;
			return;
		}
		if(nOutput == 3){
			left = 90 + i*20;
			return;
		}
		left = 80 + i*20;		
	}
}
