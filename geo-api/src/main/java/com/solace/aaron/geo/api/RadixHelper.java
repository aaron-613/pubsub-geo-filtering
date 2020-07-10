package com.solace.aaron.geo.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class will help decide what values you may want to use for the width, scale, etc. for a particular range
 * of numbers and given radix.
 *
 */
public class RadixHelper {
    
    
    
    public static void higherAcc(double max, double finest) {
        finest = Math.abs(finest);
        System.out.printf("Table for max val=%.4f, resolution=%.4f:%n",max,finest);
        for (int radix=2;radix<=36;radix++) {
            if (radix == 2 || radix == 17) {
                System.out.printf("%n%6s %8s %6s %13s %13s %8s %6s %13s %13s%n", "Radix", "Width", "Scale", "Max Reso", "Largest", "Width", "Scale", "Max Reso", "Largest");
                System.out.println("------------------------------------------------------------------------------------------");
            }
            int hScale = -(int)Math.floor(Math.log10(finest)/Math.log10(radix));
            int lScale = -(int)Math.ceil(Math.log10(finest)/Math.log10(radix));  // =-FLOOR.MATH(LOG10(finestdd)/LOG10(A17))
            int hWidth = (int)Math.ceil(Math.log10(max * Math.pow(radix,hScale)) / Math.log10(radix));
            int lWidth = (int)Math.ceil(Math.log10(max * Math.pow(radix,lScale)) / Math.log10(radix));
            // =CEILING.MATH(LOG10(max * POWER(radix,C17)) / LOG10(radix))
            double hResolution = 1.0/Math.pow(radix,hScale);
            double lResolution = 1.0/Math.pow(radix,lScale);  // =1 / POWER(radix,C17)
            double hLargest = (Math.pow(radix,hWidth)-0)/(Math.pow(radix,hScale))-1;
            double lLargest = (Math.pow(radix,lWidth)-0)/(Math.pow(radix,lScale))-1;
            
            System.out.printf("%6d %8d %6d %13.4f %13.4f %8d %6d %13.4f %13.4f%n", radix, hWidth, hScale, hResolution, hLargest, lWidth, lScale, lResolution, lLargest);
            
        }
    }

    public static void printTableMetres(double max, double finestResInMetres) {
        finestResInMetres = Math.abs(finestResInMetres);
        System.out.printf("Table for max val=%.4f, resolution=%.2f metres:%n",max,finestResInMetres);
        finestResInMetres = LatLonHelper.convertMetresToDecimalDegree(finestResInMetres);
        for (int radix=2;radix<=36;radix++) {
            if (radix == 2 || radix == 17) {
                System.out.printf("%n%6s %8s %6s %11s %11s %8s %6s %11s %11s%n", "Radix", "Width", "Scale", "Max Reso", "Largest", "Width", "Scale", "Max Reso", "Largest");
                System.out.println("------------------------------------------------------------------------------------------");
            }
            int hScale = -(int)Math.floor(Math.log10(finestResInMetres)/Math.log10(radix));
            int lScale = -(int)Math.ceil(Math.log10(finestResInMetres)/Math.log10(radix));
            int hWidth = (int)Math.ceil(Math.log10((max * Math.pow(radix,hScale))) / Math.log10(radix));
            int lWidth = (int)Math.ceil(Math.log10((max * Math.pow(radix,lScale))) / Math.log10(radix));
            double hResolution = 1.0/Math.pow(radix,hScale);
            double lResolution = 1.0/Math.pow(radix,lScale);
            hResolution = LatLonHelper.convertDecimalDegreeToMetres(hResolution);
            lResolution = LatLonHelper.convertDecimalDegreeToMetres(lResolution);
            double hLargest = (Math.pow(radix,hWidth)-0)/(Math.pow(radix,hScale))-1;
            double lLargest = (Math.pow(radix,lWidth)-0)/(Math.pow(radix,lScale))-1;
            
            System.out.printf("%6d %8d %6d %9.2f m %11.2f %8d %6d %9.2f m %11.2f%n", radix, hWidth, hScale, hResolution, hLargest, lWidth, lScale, lResolution, lLargest);
            
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
        radix = 4;
        min = -180;
        max = 180;
        finest = 0.001;
        
        // altitude in feet for commercial flights
        // https://en.wikipedia.org/wiki/Bar_Yehuda_Airfield
        // Located at 1,240 ft (378 m) below mean sea level, Bar Yehuda Airfield is the lowest airport in the world.
//        radix = 4;
//        min = -2000;
//        max = 50000;
//        finest = 100;

//        radix = 4;
//        min = -50;
//        max = 200;
//        finest = 1;

        // heading in radians
//        radix = 2;
//        min = 0;
//        max = 2*Math.PI;
//        finest = Math.PI/8;

        // outdoor temperature in Celsius
//        radix = 7;
//        min = -100;  // https://en.wikipedia.org/wiki/Lowest_temperature_recorded_on_Earth
//        max = 80; // https://en.wikipedia.org/wiki/Highest_temperature_recorded_on_Earth
//        finest = 0.5;

        // something really small
//        radix = 10;
//        min = 0.00001;
//        max = 0.0001;
//        finest = 0.000001;

        double range = max - min;
        
        int approxScale = -(int)Math.floor(Math.log10(Math.abs(finest))/Math.log10(radix));
        
        System.out.printf("Radix: %d, Range: %f to %f%n",radix,min,max);
        for (int scale=approxScale-2;scale<=approxScale+2;scale++) {
            double reso = 1.0/Math.pow(radix,scale);
            //System.out.println((Math.log10((range*Math.pow(radix,scale)))/Math.log10(radix)));
            int width = (int)Math.ceil(Math.log10((range*Math.pow(radix,scale)))/Math.log10(radix));
            if (width <= 0) continue;
            System.out.printf("With scale=%d, width=%d:%n",scale,width);
            System.out.printf(" * this will provide a maximum resolution of %f units (base 10)%n",reso);
            System.out.printf("    * or ~%.1f metres if this is lat/lon decimal degreees%n",LatLonHelper.convertDecimalDegreeToMetres(reso));
            System.out.printf(" * said another way, this will allow a maximum of %d 'slices' of the range%n",(int)Math.ceil(range/reso));
            double largest = (Math.pow(radix,width)-0)/(Math.pow(radix,scale))-1;
            System.out.printf(" * these values would allow a maximum representable value of 0..%f units%n",largest);
            if (min != 0) {  // means there is an offset
                double divisor = Math.pow(radix,-scale);
                System.out.println("Divisor: radix^(-scale) = "+radix+"/^("+(-1*scale)+") = "+divisor);
                System.out.println("min/Divisor: "+(min/divisor));
                System.out.printf("floor(min/Divisor): %f%n",Math.floor(min/divisor));
                System.out.printf("floor(min/Divisor)*divisor: %f%n",(Math.floor(min/divisor)*divisor));
                double offset = (-Math.floor(min/divisor)*divisor);
                System.out.printf(" * The offset to use: %f%n", offset);
                System.out.printf("    * Which means the maximum possible value would be %f%n",(largest-offset));
                System.out.println(" * Unless you want the range centered in the total space available?");

                // let's try to centre it
                double sideOffset = (largest + 1 - range) / 2;
                double offset2 = (-Math.floor((sideOffset-min)/divisor)*divisor);
                System.out.printf(" * Alternatively, the offset to use: %f%n", offset2);

                
                
            }
            System.out.println("----------------------------------------------------------------------");
            System.out.println();
        }
        
//        System.exit(0);
        
//        higherAcc(102000, 100);
//        System.out.println();
//        System.out.println();
//        higherAcc(359, 1.1);
//        System.out.println();
//        System.out.println();
//        printTableMetres(360, 50);
    }

    
    
    
    
}
