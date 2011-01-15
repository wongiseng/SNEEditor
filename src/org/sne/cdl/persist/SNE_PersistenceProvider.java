package org.sne.cdl.persist;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class SNE_PersistenceProvider {

	private static final PersistenceManagerFactory pmfInstance = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");

	private SNE_PersistenceProvider() {
	
	}

	public static PersistenceManagerFactory get() {
		return pmfInstance;
	}
	
	public static PersistenceManager getPM(){
		
		return get().getPersistenceManager();
	}
}