package com.mihaelisaev.dnsshop;

import java.util.Comparator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class ValuesComparator implements Comparator {

	@SuppressWarnings("unchecked")
	public int compare(Object arg0, Object arg1) {
		Map.Entry first = (Map.Entry)arg0;
		Map.Entry second = (Map.Entry)arg1;
		Comparable comparableFirst = (Comparable)first.getValue();
		Comparable comparableSecond = (Comparable)second.getValue();		
		return comparableFirst.compareTo(comparableSecond);
	}

}