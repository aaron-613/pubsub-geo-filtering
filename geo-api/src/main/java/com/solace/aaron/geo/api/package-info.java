/**

<h1>Welcome!</h1>
Welcome to Aaron's Geo[spatial|metric] filtering API for Solace PubSub+.

This has been an ongoing personal project for years, I hope that someone does something cool with it.

Most likely, you're looking to use this to take your regular decimal degree lat/lon coordinates, and filter
on those.  No problem.  But this API can also work with different bases besides decimal, from binary base2
to base36.  The smaller the base, the longer the coordinate/number representation strings become, but the fewer
subscriptions are needed to accurately match an input shape. This is due the fact in base10 decimal, when a 
Grid (aka subscription) is split either horizontally or vertically, it splits into 10 "children".  In binary,
each split results in 2 children, so an accurate approximation converges more rapidly.  ANYHOW!


<h1>Just plain lat / lon please!</h1>
If you want your topic string and subscriptions to be human-readable (which shouldn't be the main thing since the
broker is doing the filtering and coordinates should also be inside the payload), then use the following.

Check out {@link GeoStringFormatter}.  
<ul>
  <li><b>latitude:</b>  [-90,90]  GeoStringFormatter.buildRegularDecimalFormatter(8,5);</li>
  <li><b>longitude:</b> [-180,180] GeoStringFormatter.buildRegularDecimalFormatter(9,5);</li>
  <li>** or ** <b>longitude:</b> [0,360] GeoStringFormatter.buildRegularDecimalFormatter(8,5);</li>
</ul>   

  


*/

package com.solace.aaron.geo.api;