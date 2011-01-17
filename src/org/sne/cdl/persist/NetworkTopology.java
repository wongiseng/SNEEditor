package org.sne.cdl.persist;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;


@XmlRootElement
@PersistenceCapable
/**
 * This is how a network topology is persisted in the google appengine backend.
 */
public class NetworkTopology {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@SuppressWarnings("unused")
	private Key id;

	// Name of network topology
	@Persistent
	private String name;

	// Description must be less than 500 chars to be persisted as string,
	// otherwise its text
	@Persistent
	private String description;

	// Main JSON string describing the network topology obtained from WireIt
	@Persistent
	private Text working;

	// User should log in and only sees their own topologies.
	@Persistent
	private String creator;


	@Persistent
	private String language="SNEModules";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Text getWorking() {
		return working;
	}
	
	public void setWorking(Text working) {
		this.working = working;
	}


	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}
	public String toString(){
		return "\n   Name  :"+name+"\n   Working  :"+working + "\n   Wiring  :"+(working == null ? "" : working.getValue());
	}
}
