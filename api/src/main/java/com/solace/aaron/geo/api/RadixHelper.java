package com.solace.aaron.geo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RadixHelper {
	
	
	
	public static void higherAcc(double max, double finest) {
		System.out.printf("Table for max val=%.4f, resolution=%.4f:%n",max,finest);
		System.out.printf("%6s %8s %6s %11s %11s %8s %6s %11s %11s%n", "Radix", "Padding", "Scale", "Max Reso", "Largest", "Padding", "Scale", "Max Reso", "Largest");
		System.out.println("------------------------------------------------------------------------------------------");
		for (int radix=2;radix<=36;radix++) {
			int hScale = -(int)Math.floor(Math.log10(Math.abs(finest))/Math.log10(radix));
			int lScale = -(int)Math.ceil(Math.log10(Math.abs(finest))/Math.log10(radix));
        	int hPadding = (int)Math.ceil(Math.log10((max * Math.pow(radix,hScale))) / Math.log10(radix));
        	int lPadding = (int)Math.ceil(Math.log10((max * Math.pow(radix,lScale))) / Math.log10(radix));
    		double hResolution = 1.0/Math.pow(radix,hScale);
    		double lResolution = 1.0/Math.pow(radix,lScale);
    		double hLargest = (Math.pow(radix,hPadding)-0)/(Math.pow(radix,hScale))-1;
    		double lLargest = (Math.pow(radix,lPadding)-0)/(Math.pow(radix,lScale))-1;
    		
    		System.out.printf("%6d %8d %6d %11.4f %11.4f %8d %6d %11.4f %11.4f%n", radix, hPadding, hScale, hResolution, hLargest, lPadding, lScale, lResolution, lLargest);
			
		}
	}

	public static void printTableMetres(double max, double finestResInMetres) {
		System.out.printf("Table for max val=%.4f, resolution=%.2f metres:%n",max,finestResInMetres);
		finestResInMetres = LatLonHelper.convertMetresToDd(finestResInMetres);
		System.out.printf("%6s %8s %6s %11s %11s %8s %6s %11s %11s%n", "Radix", "Padding", "Scale", "Max Reso", "Largest", "Padding", "Scale", "Max Reso", "Largest");
		System.out.println("------------------------------------------------------------------------------------------");
		for (int radix=2;radix<=36;radix++) {
			int hScale = -(int)Math.floor(Math.log10(Math.abs(finestResInMetres))/Math.log10(radix));
			int lScale = -(int)Math.ceil(Math.log10(Math.abs(finestResInMetres))/Math.log10(radix));
        	int hPadding = (int)Math.ceil(Math.log10((max * Math.pow(radix,hScale))) / Math.log10(radix));
        	int lPadding = (int)Math.ceil(Math.log10((max * Math.pow(radix,lScale))) / Math.log10(radix));
    		double hResolution = 1.0/Math.pow(radix,hScale);
    		double lResolution = 1.0/Math.pow(radix,lScale);
			hResolution = LatLonHelper.convertDdToMetres(hResolution);
			lResolution = LatLonHelper.convertDdToMetres(lResolution);
    		double hLargest = (Math.pow(radix,hPadding)-0)/(Math.pow(radix,hScale))-1;
    		double lLargest = (Math.pow(radix,lPadding)-0)/(Math.pow(radix,lScale))-1;
    		
    		System.out.printf("%6d %8d %6d %9.2f m %11.2f %8d %6d %9.2f m %11.2f%n", radix, hPadding, hScale, hResolution, hLargest, lPadding, lScale, lResolution, lLargest);
			
		}
	}
	
	public static void main(String... args) throws NumberFormatException, IOException {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int radix;
		double maxX;
		Double maxY = Double.NaN;
		boolean needsNegs;
		double finest;
		
//		System.out.print("What radix (base) do you want to calculate for? [2..36] ");
//		radix = Integer.parseInt(reader.readLine());
//		
//		System.out.print("What is the maximum absolute value of numbers to encode? ");
//		maxX = Double.parseDouble(reader.readLine());
//		System.out.print("Is this a two-dimensional space? If so, enter maximum absolute value for other axis: ");
//		try {
//			maxY = Double.parseDouble(reader.readLine());
//		} catch (Exception e) {
//			// oh well, leave maxY as a NaN
//		}
//		
//		System.out.print("Do you need to be able to encode both positive and negative numbers? [y/n] ");
//		needsNegs = Boolean.parseBoolean(reader.readLine());
//		
//		System.out.println("Approximately what size do you want the finest grain of filtering to be?");
//		System.out.println("This is not necessarily the amount of precision available in your data");
//		finest = Double.parseDouble(reader.readLine());
		
		radix = 3;
		maxX = 359;
		//maxY = Double.val-ueOf(23);
		finest = 10;
		
    	int approxScale = -(int)Math.floor(Math.log10(Math.abs(finest))/Math.log10(radix));

    	for (int scale=approxScale-2;scale<=approxScale+2;scale++) {
    		double reso = 1.0/Math.pow(radix,scale);
        	System.out.println((Math.log10((maxX*Math.pow(radix,scale)))/Math.log10(radix)));
        	int padding = (int)Math.ceil(Math.log10((maxX*Math.pow(radix,scale)))/Math.log10(radix));
    		System.out.printf("With scale=%d, padding=%d:%n",scale,padding);
    		System.out.printf(" * this will provide a maximum resolution of %f decimal%n",reso);
    		System.out.printf("   * or ~ %,.1f metres if this is decimal degreees%n",LatLonHelper.convertDdToMetres(reso));
//    		System.out.printf(" * you will need to use padding=%d to support max=%f%n",padding,maxX);
//    		if (!maxY.isNaN()) {
//            	padding = (int)Math.ceil(Math.log10((maxY*Math.pow(radix,scale))+1)/Math.log10(radix));
//        		System.out.printf(" * you will need to use padding=%d to support Y max=%f%n",padding,maxY);
//    		}
    		double largest = (Math.pow(radix,padding)-0)/(Math.pow(radix,scale))-1;
    		System.out.printf(" * these values would allow a maximum representable value of %f%n",largest);

    		System.out.println();
    	}
    	
    	
    	higherAcc(359, 1.1);
    	System.out.println();
    	printTableMetres(180, 100);
	}

	
	
	
	
}
