# pubsub-geo-filtering

Finally getting my geo-filtering code posted up to GitHub.  Will document more later.

![Circular search area](https://github.com/aaron-613/pubsub-geo-filtering/blob/master/stuff/circle.png "Circular search area")

# Basics

* includes both 2D and 1D versions
   * 2D can be used for lat/lon coordinates, or any planar geometric coordinates (e.g. \[x,y\], UTM, MGRS)
   * 1D can be used for any scalar (e.g. altitude, heading, speed)
* Have updated code to support multiple radixes/bases, from binary base 2 to base 36


# Live Demo

https://sg.solace.com/bus

# Papers

https://worldcomp-proceedings.com/proc/p2016/ICM3967.pdf


# The Algorithm

![blah](https://github.com/aaron-613/pubsub-geo-filtering/blob/master/stuff/base4animation22.gif "Base 4 search construction")

Significant changes to the algorithm have been made to greatly simplify it and improve its usability.

## Improvments made during the process

1. Moved from Decimal base 10 to any radix, base n, where n=[2,36].
    1. Issue: floating point representation in base n with a radix point is weird.  E.g. 123.456 = 
1. Something else

