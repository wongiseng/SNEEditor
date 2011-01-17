package org.sne.cdl.query;

import java.util.Collection;
import java.util.HashMap;

/**
 * To help me organize  parsing SPARQL queries results
 * @author Wongiseng
 *
 */
public class ResultRow {
	HashMap<String, String> data = new HashMap<String, String>();

	public void put(String key, String value) {
		data.put(key, value);
	}

	public Collection<String> keys() {
		return data.keySet();
	}

	public Collection<String> values() {
		return data.values();
	}

	public String get(String key) {
		return data.get(key);
	}

	public String toString() {
		StringBuffer res = new StringBuffer();
		for (String key : keys()) {
			res.append(key + "  " + get(key) + "\n");
		}
		return res.toString();
	}
}