package com.solace.aaron.geo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RadixHelper {
    
    
    
    public static void higherAcc(double max, double finest) {
        System.out.printf("Table for max val=%.4f, resolution=%.4f:%n",max,finest);
        for (int radix=2;radix<=36;radix++) {
            if (radix == 2 || radix == 17) {
                System.out.printf("%n%6s %8s %6s %13s %13s %8s %6s %13s %13s%n", "Radix", "Padding", "Scale", "Max Reso", "Largest", "Padding", "Scale", "Max Reso", "Largest");
                System.out.println("------------------------------------------------------------------------------------------");
            }
            int hScale = -(int)Math.floor(Math.log10(Math.abs(finest))/Math.log10(radix));
            int lScale = -(int)Math.ceil(Math.log10(Math.abs(finest))/Math.log10(radix));
            int hPadding = (int)Math.ceil(Math.log10((max * Math.pow(radix,hScale))) / Math.log10(radix));
            int lPadding = (int)Math.ceil(Math.log10((max * Math.pow(radix,lScale))) / Math.log10(radix));
            double hResolution = 1.0/Math.pow(radix,hScale);
            double lResolution = 1.0/Math.pow(radix,lScale);
            double hLargest = (Math.pow(radix,hPadding)-0)/(Math.pow(radix,hScale))-1;
            double lLargest = (Math.pow(radix,lPadding)-0)/(Math.pow(radix,lScale))-1;
            
            System.out.printf("%6d %8d %6d %13.4f %13.4f %8d %6d %13.4f %13.4f%n", radix, hPadding, hScale, hResolution, hLargest, lPadding, lScale, lResolution, lLargest);
            
        }
    }

    public static void printTableMetres(double max, double finestResInMetres) {
        System.out.printf("Table for max val=%.4f, resolution=%.2f metres:%n",max,finestResInMetres);
        finestResInMetres = LatLonHelper.convertMetresToDd(finestResInMetres);
        for (int radix=2;radix<=36;radix++) {
            if (radix == 2 || radix == 17) {
                System.out.printf("%n%6s %8s %6s %11s %11s %8s %6s %11s %11s%n", "Radix", "Padding", "Scale", "Max Reso", "Largest", "Padding", "Scale", "Max Reso", "Largest");
                System.out.println("------------------------------------------------------------------------------------------");
            }
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
        double min;
        double max;
        double finest;
        
/*        System.out.print("What radix (base) do you want to calculate for? [2..36] ");
        radix = Integer.parseInt(reader.readLine());
        
        System.out.print("What is the minimum value of numbers to encode? (e.g. -90 for latitude decimal degrees; 0 for speed) ");
        min = Double.parseDouble(reader.readLine());
        System.out.print("What is the maximum value of numbers to encode? (e.g. 180 for longitude decimal degrees; 60000 for altitude) ");
        max = Double.parseDouble(reader.readLine());
        
        System.out.println("Approximately what size do you want the finest grain of filtering to be?");
        System.out.println("This is not necessarily the amount of precision available in your data");
        finest = Double.parseDouble(reader.readLine());
*/        
        // longitude decimal degrees
        radix = 10;
        min = -180;
        max = 180;
        finest = 0.0005;
        
        // altitude in feet
        // https://en.wikipedia.org/wiki/Bar_Yehuda_Airfield
        radix = 6;
        min = -2000;
        max = 100000;
        finest = 100;

        // heading in radians
        radix = 8;
        min = 0;
        max = 2*Math.PI;
        finest = Math.PI/8;
        
        double range = max - min;
        
        int approxScale = -(int)Math.floor(Math.log10(Math.abs(finest))/Math.log10(radix));
        
        System.out.printf("Range: %f to %f%n",min,max);
        for (int scale=approxScale-2;scale<=approxScale+2;scale++) {
            double reso = 1.0/Math.pow(radix,scale);
            //System.out.println((Math.log10((range*Math.pow(radix,scale)))/Math.log10(radix)));
            int padding = (int)Math.ceil(Math.log10((range*Math.pow(radix,scale)))/Math.log10(radix));
            if (padding <= 0) continue;
            System.out.printf("With scale=%d, padding=%d:%n",scale,padding);
            System.out.printf(" * this will provide a maximum resolution of %f decimal%n",reso);
            System.out.printf("   * or ~%,.1f metres if this is decimal degreees%n",LatLonHelper.convertDdToMetres(reso));
            System.out.printf("   * or this will allow a maximum of %d 'slices' of the range%n",(int)Math.ceil(range/reso));
            double largest = (Math.pow(radix,padding)-0)/(Math.pow(radix,scale))-1;
            System.out.printf(" * these values would allow a maximum representable value of %f%n",largest);
            if (min != 0) {  // means there is an offset
                System.out.println();
                double divisor = Math.pow(radix,-scale);
                System.out.println("Divisor: radix^-scale = "+radix+"/^-"+scale+" = "+divisor);
                System.out.println("min/Divisor: "+(min/divisor));
                System.out.println("floor(min/Divisor): "+Math.floor(min/divisor));
                System.out.println("floor(min/Divisor)*divisor: "+(long)(Math.floor(min/divisor)*divisor));
                System.out.println("The offset to use: "+ (long)(-Math.floor(min/divisor)*divisor));
            }
            System.out.println();
        }
        
        System.exit(0);
        
        higherAcc(101000, 100);
        System.out.println();
        System.out.println();
        higherAcc(359, 1.1);
        System.out.println();
        System.out.println();
        printTableMetres(360, 50);
    }

    
    
    
    
}
