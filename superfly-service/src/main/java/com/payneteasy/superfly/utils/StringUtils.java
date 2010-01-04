package com.payneteasy.superfly.utils;

import java.util.Collection;

public class StringUtils {
   public static String collectionToCommaDelimitedString(Collection collection){
	   if(collection==null||collection.size()==0){
		   return null;
	   }
	return org.springframework.util.StringUtils.collectionToCommaDelimitedString(collection);
	   
   }
}
