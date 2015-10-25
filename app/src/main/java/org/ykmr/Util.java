package org.ykmr;

public class Util {
	public static String join(float[] arr, String sep){
		StringBuffer res = new StringBuffer();
		for(int i=0; i<arr.length; i++){
			res.append(arr[i]);
			if(i<arr.length-1) res.append(sep);
		}
		return res.toString();
	}
	public static String join(float[] arr){
		return join(arr, ",");
	}
}
