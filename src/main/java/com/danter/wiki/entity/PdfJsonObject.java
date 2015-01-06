package com.danter.wiki.entity;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class PdfJsonObject implements Serializable {
	@XmlTransient
	private static final long serialVersionUID = 1869396423923941443L;
	
	@XmlElement(name="title")
	public String title;
	@XmlElement(name="urls")
	public List<String> urls;
	
	@Override
	public String toString() {
		return "PdfJsonObject [title=" + title + ", urls=" + urls + "]";
	}
	
	

}
