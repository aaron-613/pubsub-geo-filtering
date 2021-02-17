package com.solace.aaron.geo.submgr;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.opencsv.CSVReaderHeaderAware;
import com.solace.aaron.geo.api.Geo2dSearch;
import com.solace.aaron.geo.api.Geo2dSearchResult;
import com.solace.aaron.geo.api.Geo2dSearchResult.IncrementalSearchResult;

public class JsonTests {

    private static class DoubleReverseComparator implements Comparator<Double> {

        @Override
        public int compare(Double d1, Double d2) {
//            return d1 < d2 ? 1 : d1 > d2 ? -1 : 0;
            return d1 > d2 ? 1 : d1 < d2 ? -1 : 0;
        }
    }
    
    
    
    
    public static void main(String... args) throws InterruptedException, ParseException, FileNotFoundException, IOException {

        double lat = 1.23456;
        double lon = 103.678901;
        System.out.printf("no decimal point: %09d/%010d%n",(int)(lat*1000000),(int)(lon*1000000));
        System.out.printf("w/decimal point:  %010.6f/%011.6f%n",lat,lon);
//        System.exit(-01);
        
     
        //System.exit(-1);
        System.out.println(Math.pow(10,1));
        System.out.println(Math.pow(10,2));
        System.out.println(Math.pow(4,1));
        System.out.println(Math.pow(4,2));
        System.out.println(Math.pow(4,3));
        System.out.println(Math.pow(4,4));
        
        Double[] da = new Double[5];
        da[0] = 1.0/2;
        da[1] = 1.0/1;
        da[2] = 1.0/0;
        da[3] = 1.0/3;
        da[4] = 1.0/4;
        Arrays.sort(da);
        System.out.println(Arrays.toString(da));
//        System.exit(0);
        
        
        
        double area = 0.0001;
        double gridArea = 1000000;
        double overC = gridArea - area;
        for (int i=6;i>-4;i--) {
            overC = overC - Math.pow(10,i) + Math.pow(10,i-1);
            System.out.println("i:"+i+" = "+overC);
        }
//        System.exit(1);
        
        SortedMap<Double,Integer> asdf = new TreeMap<>(new DoubleReverseComparator());
        asdf.put(0.3,0);
        asdf.put(0.4,1);
        asdf.put(0.2,2);
        System.out.println(asdf);
//        System.exit(0);
        
        List<List<Double>> test = new ArrayList<>();
        List<Double> index1 = new ArrayList<>();
        index1.add(23.0);
        index1.add(43.0);
        test.add(0,index1);
        JsonArrayBuilder jjjj = Json.createArrayBuilder(test);
        
        
        JsonReader reader = Json.createReader(new StringReader("{ \"hello\":123 }"));
        JsonObject msgJsonObject = reader.readObject();
        
        JsonWriter writer = Json.createWriter(System.out);
        writer.write(jjjj.build());
        
        writer = Json.createWriter(System.out);
        List<Double> coords = new ArrayList<>();
        coords.add(23.4);
        coords.add(123.4);
        coords.add(23423.4);
        coords.add(24443.4);
        JsonArrayBuilder ja = Json.createArrayBuilder(coords);
        writer.writeArray(ja.build());
        
/*         CSVReaderHeaderAware csvReader = new CSVReaderHeaderAware(new FileReader("oz_fullrez.csv"));
        List<Map<String,String>> values = new ArrayList<>();
        Map<String, String> line;
        while ((line = csvReader.readMap()) != null) {
            values.add(line);
        }
        csvReader.close();
        WKTReader r = new WKTReader();
        List<Geometry> geos = new ArrayList<>();
        for (int i=0;i<values.size();i++) {
            Geometry g = r.read(values.get(i).get("WKT"));
            if (g != null) geos.add(g);
            //geos.add(r.read(values.get(i).get("WKT")));
        }
 */        
        
        Geo2dSearch search = new Geo2dSearch(4,7,12,-180,11,-90);
        search = new Geo2dSearch(4,3,5,-4,5,-4);
//        search = new Geo2dSearch(5,7,11,-180,10,-90);
//        search = new Geo2dSearch(10,5,9,-180,8,-90);
//        search = new Geo2dSearch(10,5,9,0,8,0);
//        search = Geo2dSearch.buildDecimalGeo2dSearch(2, 5, 4);
        // search = new Geo2dSearch(2,13,22,-180,21,-90);
//        search = new Geo2dSearch(16,4,7,-180,6,-90);
        
//       ) Coordinate[] coords2 = {
//                new Coordinate(0.89,0),
//                new Coordinate(0,0.179),
//                new Coordinate(0.58,0.9),
//                new Coordinate(0.89,0.179),
//                new Coordinate(0.89,0),
//        };

        // my standard 5 testing squares
         Coordinate[] coords00 = {
                new Coordinate(-1,0),  // x,y coordinaets, not lat/lon
                new Coordinate(-1,4),
                new Coordinate(-4,4),
                new Coordinate(-2,3),
                new Coordinate(-1,0),
        };
        
        Coordinate[] coords0 = {
                new Coordinate(0,0),
                new Coordinate(0,4),
                new Coordinate(3,4),
                new Coordinate(1,3),
                new Coordinate(0,0),
        };
        Coordinate[] coords1 = {
                new Coordinate(0,0),
                new Coordinate(1,3),
                new Coordinate(3,4),
                new Coordinate(1,0),
                new Coordinate(0,0),
        };
        Coordinate[] coords2 = {
                new Coordinate(1,0),
                new Coordinate(3,4),
                new Coordinate(4,4),
                new Coordinate(3.75,0.05),
                new Coordinate(3,2),
                new Coordinate(1,0),
        };
        Coordinate[] coords3 = {
                new Coordinate(1,0),
                new Coordinate(3,1),
                new Coordinate(3.75,0.05),
                new Coordinate(1,0),
        };

        
        
// tl 1.254353, 103.608061
// br 1.244484, 103.645109
 // lat mid = 1.2494185
  // lon mid = 103.626585
   
        // TUAS port
/*        Coordinate[] coords0 = {
                new Coordinate(103.606,1.255),
                new Coordinate(103.626,1.255),
                new Coordinate(103.626,1.249),
                new Coordinate(103.606,1.249),
                new Coordinate(103.606,1.255),
        };
        Coordinate[] coords1 = {
                new Coordinate(103.626,1.255),
                new Coordinate(103.645,1.255),
                new Coordinate(103.645,1.249),
                new Coordinate(103.626,1.249),
                new Coordinate(103.626,1.255),
        };
        Coordinate[] coords2 = {
                new Coordinate(103.606,1.249),
                new Coordinate(103.626,1.249),
                new Coordinate(103.626,1.244),
                new Coordinate(103.606,1.244),
                new Coordinate(103.606,1.249),
        };
        Coordinate[] coords3 = {
                new Coordinate(103.626,1.249),
                new Coordinate(103.645,1.249),
                new Coordinate(103.645,1.244),
                new Coordinate(103.626,1.244),
                new Coordinate(103.626,1.249),
        };
 */        
        
        
//        Coordinate[] coords0 = {
//                new Coordinate(4,0),
//                new Coordinate(0,4),
//                new Coordinate(0,0),
//                new Coordinate(4,0),
//        };
//        Coordinate[] coords1 = {
//                new Coordinate(4,0),
//                new Coordinate(4,4),
//                new Coordinate(0,4),
//                new Coordinate(4,0),
//        };
//        Coordinate[] coords1 = {
//                new Coordinate(2,2),
//                new Coordinate(2,2.5),
//                new Coordinate(1.5,2.5),
//                new Coordinate(2,2),
//        };

        
        // two big diagonal blocks, with 2 small insets
/*        Coordinate[] coords0 = {
                new Coordinate(4,0),
//                new Coordinate(2.125,1.875),
//                new Coordinate(2,1.75),
//                new Coordinate(1.75,2),
//                new Coordinate(1.875,2.125),
                new Coordinate(2.1,1.9),
                new Coordinate(2,1.8),
                new Coordinate(1.8,2),
                new Coordinate(1.9,2.1),
                new Coordinate(0,4),
                new Coordinate(-4,0),
//                new Coordinate(0,0),
                new Coordinate(-0.1,-3.9),
                new Coordinate(0,-3.1),
                new Coordinate(0.1,-3.9),
//                new Coordinate(-4,0),
                new Coordinate(4,0),
        };
        Coordinate[] coords1 = {
                new Coordinate(4,0),
//                new Coordinate(4,4),
                new Coordinate(8,4),
                new Coordinate(4,8),
                new Coordinate(0,4),
//                new Coordinate(1.875,2.125),
//                new Coordinate(2,2.25),
//                new Coordinate(2.25,2),
//                new Coordinate(2.125,1.875),
                new Coordinate(1.9,2.1),
                new Coordinate(2,2.2),
                new Coordinate(2.2,2),
                new Coordinate(2.1,1.9),
                new Coordinate(4,0),
        };

        Coordinate[] coords2 = {
//                new Coordinate(2,2.25),
//                new Coordinate(2.25,2),
//                new Coordinate(2,1.75),
//                new Coordinate(1.75,2),
//                new Coordinate(2,2.25),
                new Coordinate(2,2.2),
                new Coordinate(2.2,2),
                new Coordinate(2,1.8),
                new Coordinate(1.8,2),
                new Coordinate(2,2.2),
        };
        Coordinate[] coords3 = {
              new Coordinate(0,-4),
              new Coordinate(-0.1,-3.9),
              new Coordinate(0,-3.1),
              new Coordinate(0.1,-3.9),
              new Coordinate(0,-4),
        };
*/
        
        

        
        
        Geometry target0 = new GeometryFactory().createPolygon(coords0);
        Geometry target1 = new GeometryFactory().createPolygon(coords1);
        Geometry target2 = new GeometryFactory().createPolygon(coords2);
        Geometry target3 = new GeometryFactory().createPolygon(coords3);
//        Geometry target00 = new GeometryFactory().createPolygon(coords00);

        List<Geometry> targets = new ArrayList<>();
        targets.add(target0);
        targets.add(target1);
        targets.add(target2);
        targets.add(target3);
//        targets.add(target00);
//        targets.add(r.read("POLYGON ((103.950165 1.332927,103.952024 1.330182,103.951004 1.329362,103.950528 1.328932,103.949791 1.328421,103.948431 1.327937,103.948401 1.327927,103.948382 1.32792,103.946241 1.327143,103.946207 1.327131,103.945412 1.326843,103.944137 1.326374,103.943379 1.326107,103.942655 1.325848,103.941876 1.325608,103.941197 1.325424,103.940845 1.325355,103.940732 1.325332,103.940542 1.325294,103.940472 1.325284,103.940162 1.325239,103.935644 1.32463,103.93351 1.32436,103.933343 1.324343,103.933336 1.324343,103.932023 1.32421,103.930932 1.32411,103.929188 1.323861,103.928096 1.323671,103.927302 1.323527,103.926994 1.325839,103.926885 1.326626,103.926884 1.326639,103.926686 1.328284,103.926511 1.329617,103.926509 1.329638,103.925726 1.329406,103.925256 1.329314,103.924833 1.32928,103.924352 1.329285,103.923811 1.329361,103.923445 1.329442,103.923039 1.32958,103.922632 1.329759,103.922254 1.329966,103.921825 1.330259,103.921178 1.33072,103.920191 1.331411,103.920058 1.33153,103.919928 1.331675,103.919773 1.331927,103.91961 1.332301,103.919568 1.332399,103.919559 1.332419,103.918828 1.332082,103.918641 1.331993,103.918295 1.331828,103.917341 1.331372,103.916601 1.331026,103.916362 1.330914,103.915543 1.33053,103.914053 1.329832,103.912193 1.328951,103.911666 1.328686,103.911116 1.328387,103.91028 1.327898,103.910174 1.327836,103.90932 1.327348,103.90903 1.327215,103.908851 1.327134,103.908217 1.326902,103.908195 1.326894,103.908173 1.326888,103.907264 1.326629,103.906288 1.32634,103.905798 1.326188,103.905854 1.326809,103.905856 1.326826,103.905858 1.326847,103.905857 1.326863,103.905852 1.327003,103.905849 1.327063,103.905848 1.327095,103.905845 1.327181,103.905785 1.327587,103.905781 1.327602,103.905767 1.327659,103.905712 1.327887,103.905627 1.328127,103.905616 1.328159,103.905599 1.328208,103.905479 1.328515,103.905342 1.328733,103.905306 1.328789,103.905217 1.328937,103.9051 1.32913,103.904978 1.329287,103.904961 1.32931,103.904888 1.329404,103.904811 1.329475,103.904728 1.329551,103.904207 1.330044,103.902819 1.331407,103.902323 1.331884,103.903456 1.333077,103.903825 1.333384,103.904163 1.333603,103.904541 1.333798,103.904962 1.334001,103.905956 1.334486,103.906217 1.334577,103.906591 1.334652,103.908041 1.334911,103.908928 1.335103,103.909092 1.335144,103.910425 1.335475,103.910518 1.335506,103.911168 1.335719,103.911185 1.335724,103.911386 1.335815,103.912074 1.336137,103.912906 1.336548,103.913508 1.336846,103.914102 1.33713,103.914297 1.337224,103.915005 1.337569,103.916008 1.337852,103.916584 1.33798,103.916641 1.337993,103.91686 1.338025,103.917073 1.338057,103.917471 1.338116,103.917528 1.338125,103.917936 1.338218,103.917942 1.338221,103.917923 1.338439,103.917927 1.338649,103.917933 1.338703,103.917968 1.338989,103.918035 1.33933,103.918077 1.339544,103.918295 1.340343,103.91851 1.34101,103.918577 1.341219,103.918586 1.341276,103.918608 1.341422,103.918632 1.341712,103.918639 1.343742,103.91864 1.344103,103.91871 1.344565,103.918803 1.344948,103.918959 1.345269,103.919231 1.345637,103.919558 1.34595,103.9199 1.346169,103.920118 1.346234,103.921086 1.346633,103.923017 1.347439,103.92421 1.348027,103.924714 1.348363,103.924991 1.34864,103.925327 1.348254,103.926133 1.347515,103.926855 1.347004,103.927433 1.346684,103.929306 1.345518,103.930216 1.344947,103.930465 1.344737,103.930915 1.344323,103.93216 1.343131,103.933435 1.341844,103.935471 1.339809,103.935582 1.339779,103.935708 1.339655,103.935712 1.339648,103.935727 1.339618,103.935788 1.339498,103.936613 1.338691,103.938008 1.337311,103.938511 1.336807,103.938847 1.336529,103.939036 1.336426,103.93926 1.336358,103.940217 1.336177,103.940483 1.336144,103.940492 1.336143,103.943624 1.335547,103.946956 1.334897,103.949084 1.334439,103.949384 1.334066,103.950165 1.332927))"));
//        targets.add(r.read("POLYGON ((103.854739 1.375828,103.85541 1.375815,103.855932 1.375851,103.856354 1.375928,103.857171 1.376144,103.85852 1.37654,103.858672 1.375811,103.858696 1.375721,103.858847 1.375156,103.858854 1.375129,103.85897 1.374736,103.859389 1.373637,103.85993 1.372362,103.860239 1.371675,103.860401 1.371337,103.860515 1.371098,103.860675 1.370699,103.860796 1.370278,103.860874 1.369843,103.8609 1.369413,103.860903 1.369365,103.860918 1.369138,103.860917 1.369125,103.860917 1.369108,103.860902 1.36841,103.860902 1.368369,103.860901 1.368347,103.860897 1.368299,103.860851 1.36776,103.860841 1.367699,103.860835 1.367668,103.860818 1.367571,103.860806 1.367527,103.860768 1.367392,103.860758 1.367355,103.860701 1.367193,103.860664 1.367088,103.860174 1.365821,103.859544 1.36419,103.8594 1.3638,103.858716 1.361945,103.857503 1.358575,103.857452 1.358434,103.857346 1.358125,103.857335 1.358091,103.857324 1.358052,103.857252 1.35778,103.857244 1.357747,103.857201 1.357556,103.856985 1.356589,103.856962 1.356389,103.856867 1.355529,103.856861 1.355528,103.856573 1.355601,103.856202 1.355695,103.856126 1.355736,103.855806 1.35591,103.855428 1.356146,103.855234 1.356326,103.855004 1.356534,103.854794 1.356809,103.854628 1.357078,103.854525 1.357244,103.854327 1.357615,103.854311 1.357648,103.853939 1.358402,103.853921 1.358439,103.853594 1.358981,103.853534 1.359082,103.853422 1.359248,103.853191 1.359531,103.853155 1.359563,103.853078 1.359632,103.853017 1.359687,103.852758 1.359924,103.852555 1.36011,103.852386 1.360246,103.852265 1.360345,103.851804 1.360725,103.85096 1.361422,103.850378 1.361903,103.850128 1.362104,103.849594 1.362535,103.84957 1.36255,103.84948 1.362607,103.849355 1.362686,103.849311 1.362714,103.849194 1.362779,103.849017 1.362878,103.84897 1.362904,103.848834 1.362972,103.848804 1.362987,103.848648 1.363066,103.848179 1.363297,103.848171 1.363301,103.848106 1.363333,103.848034 1.363365,103.8477 1.363516,103.84769 1.36352,103.847328 1.363666,103.847299 1.363678,103.847086 1.363764,103.846926 1.363811,103.846843 1.363835,103.846812 1.363844,103.846537 1.363919,103.846434 1.363947,103.846249 1.363986,103.846171 1.364002,103.845985 1.364041,103.845887 1.364059,103.845727 1.364088,103.845376 1.364136,103.844255 1.364235,103.844131 1.364246,103.843962 1.36426,103.843146 1.364321,103.842987 1.364334,103.843234 1.364892,103.84351 1.365484,103.843766 1.365905,103.84433 1.36669,103.844843 1.367388,103.845143 1.367973,103.845312 1.368477,103.845381 1.368778,103.845424 1.369085,103.845438 1.369179,103.845513 1.370016,103.845513 1.370381,103.8455 1.370815,103.845337 1.372072,103.84506 1.374336,103.844697 1.377333,103.845064 1.377306,103.845728 1.377187,103.846386 1.377034,103.84697 1.376901,103.848915 1.37647,103.849454 1.37638,103.849911 1.376338,103.850821 1.376232,103.853667 1.37594,103.854739 1.375828))"));
//        targets.add(r.read("POLYGON ((103.904994 1.319366,103.904981 1.319132,103.90508 1.317613,103.904894 1.317575,103.902545 1.317128,103.90059 1.316807,103.899533 1.316588,103.896844 1.316014,103.901484 1.309538,103.901386 1.309531,103.900024 1.309471,103.899777 1.309468,103.897554 1.309508,103.894569 1.309578,103.894474 1.30956,103.89439 1.309544,103.894339 1.309534,103.892626 1.309176,103.890928 1.308835,103.889892 1.308624,103.890189 1.307515,103.890236 1.307285,103.890256 1.307133,103.890253 1.306987,103.890209 1.3068,103.890164 1.306701,103.890069 1.306527,103.889962 1.306372,103.8898 1.306217,103.88964 1.306113,103.889474 1.306039,103.889285 1.305985,103.889092 1.305953,103.887836 1.305767,103.886551 1.305573,103.885055 1.305373,103.884809 1.305299,103.884451 1.305105,103.88427 1.304986,103.884114 1.304838,103.883992 1.304698,103.883892 1.304445,103.883604 1.304662,103.883543 1.304781,103.882182 1.305993,103.880821 1.306908,103.878613 1.307743,103.878076 1.307857,103.877698 1.307823,103.877378 1.307674,103.877081 1.307354,103.876532 1.307,103.876108 1.306851,103.875731 1.306805,103.875125 1.306794,103.873455 1.306759,103.872792 1.306679,103.87214 1.306473,103.871659 1.306256,103.87035 1.305387,103.869283 1.304669,103.869216 1.304609,103.868703 1.304274,103.868631 1.304418,103.868615 1.304449,103.868294 1.305094,103.87007 1.30753,103.870252 1.308362,103.870222 1.308513,103.870173 1.308753,103.870013 1.309143,103.869989 1.309364,103.869956 1.309671,103.869967 1.310027,103.870056 1.310135,103.87035 1.310491,103.870341 1.31053,103.870335 1.310559,103.870327 1.310595,103.870275 1.310804,103.8702 1.310898,103.870155 1.310956,103.869551 1.311674,103.869307 1.312101,103.869239 1.312221,103.869088 1.312676,103.869037 1.313031,103.869017 1.313294,103.869098 1.313597,103.869481 1.314614,103.869541 1.314771,103.869542 1.314785,103.869581 1.315196,103.869551 1.31548,103.870611 1.316118,103.870622 1.316125,103.87214 1.317089,103.872165 1.31713,103.872307 1.317369,103.872312 1.317376,103.872321 1.317392,103.872372 1.317585,103.872387 1.317774,103.872442 1.318466,103.872463 1.318678,103.872593 1.31881,103.872795 1.318992,103.872814 1.319002,103.873207 1.319205,103.873221 1.319208,103.87335 1.319238,103.873469 1.319265,103.873577 1.319262,103.873791 1.319255,103.874133 1.319255,103.874536 1.319326,103.874797 1.319387,103.874979 1.319488,103.87502 1.319527,103.875258 1.319752,103.875468 1.320012,103.875535 1.320091,103.875631 1.320205,103.875646 1.320234,103.875657 1.320256,103.875691 1.320327,103.875657 1.320349,103.875391 1.320515,103.875175 1.32067,103.875045 1.320763,103.87503 1.320776,103.874905 1.320882,103.874814 1.320958,103.874742 1.321018,103.874367 1.321412,103.874339 1.321441,103.874294 1.321489,103.874242 1.321563,103.873654 1.322403,103.872984 1.323378,103.872159 1.324514,103.872053 1.324665,103.871887 1.3249,103.871741 1.325089,103.87159 1.325284,103.871325 1.325604,103.871055 1.325877,103.87045 1.326512,103.869895 1.327067,103.869818 1.327129,103.869654 1.327261,103.869446 1.327429,103.869301 1.327533,103.86929 1.327541,103.86919 1.327613,103.869173 1.327624,103.868804 1.327862,103.869046 1.327942,103.869802 1.32819,103.869967 1.328244,103.87109 1.328613,103.872812 1.329204,103.873237 1.329346,103.873291 1.329364,103.873955 1.329585,103.874364 1.329726,103.874967 1.329937,103.875142 1.329995,103.876938 1.330592,103.87731 1.330715,103.87807 1.330958,103.878639 1.331142,103.87866 1.331149,103.879504 1.331422,103.879585 1.331448,103.880163 1.331586,103.880278 1.331611,103.881376 1.331851,103.882349 1.332064,103.884203 1.332483,103.884823 1.33262,103.88645 1.332981,103.888125 1.333345,103.888572 1.333461,103.888711 1.333494,103.888841 1.333524,103.888895 1.33354,103.889084 1.333596,103.889224 1.333638,103.889755 1.333797,103.89044 1.334013,103.89065 1.334079,103.891446 1.334376,103.891585 1.334438,103.89187 1.334564,103.891901 1.334578,103.892012 1.334635,103.892233 1.334747,103.892642 1.334954,103.892798 1.335033,103.892943 1.335119,103.893175 1.335256,103.893409 1.335393,103.893538 1.335481,103.893684 1.33558,103.893931 1.335747,103.894061 1.335846,103.894329 1.336049,103.894701 1.336334,103.895083 1.336627,103.89573 1.337239,103.89592 1.337441,103.896089 1.33762,103.896101 1.337632,103.896202 1.337746,103.896207 1.337742,103.896475 1.337492,103.897121 1.336888,103.89825 1.335779,103.898871 1.335183,103.899726 1.334363,103.900517 1.333603,103.901481 1.332694,103.902323 1.331884,103.902819 1.331407,103.904207 1.330044,103.904728 1.329551,103.904811 1.329475,103.904888 1.329404,103.904961 1.32931,103.904978 1.329287,103.9051 1.32913,103.905217 1.328937,103.905306 1.328789,103.905342 1.328733,103.905479 1.328515,103.905599 1.328208,103.905616 1.328159,103.905627 1.328127,103.905712 1.327887,103.905767 1.327659,103.905781 1.327602,103.905785 1.327587,103.905845 1.327181,103.905848 1.327095,103.905849 1.327063,103.905852 1.327003,103.905857 1.326863,103.905858 1.326847,103.905856 1.326826,103.905854 1.326809,103.905793 1.326186,103.90579 1.326152,103.905786 1.326113,103.905695 1.325258,103.905667 1.324997,103.905645 1.324789,103.905506 1.323769,103.905247 1.321871,103.904994 1.319366))"));
//        targets.add(r.read("POLYGON ((103.950165 1.332927,103.952024 1.330182,103.951004 1.329362,103.950528 1.328932,103.949791 1.328421,103.948431 1.327937,103.948401 1.327927,103.948382 1.32792,103.946241 1.327143,103.946207 1.327131,103.945412 1.326843,103.944137 1.326374,103.943379 1.326107,103.942655 1.325848,103.941876 1.325608,103.941197 1.325424,103.940845 1.325355,103.940732 1.325332,103.940542 1.325294,103.940472 1.325284,103.940162 1.325239,103.935644 1.32463,103.93351 1.32436,103.933343 1.324343,103.933336 1.324343,103.932023 1.32421,103.930932 1.32411,103.929188 1.323861,103.928096 1.323671,103.927302 1.323527,103.926994 1.325839,103.926885 1.326626,103.926884 1.326639,103.926686 1.328284,103.926511 1.329617,103.926509 1.329638,103.925726 1.329406,103.925256 1.329314,103.924833 1.32928,103.924352 1.329285,103.923811 1.329361,103.923445 1.329442,103.923039 1.32958,103.922632 1.329759,103.922254 1.329966,103.921825 1.330259,103.921178 1.33072,103.920191 1.331411,103.920058 1.33153,103.919928 1.331675,103.919773 1.331927,103.91961 1.332301,103.919568 1.332399,103.919559 1.332419,103.918828 1.332082,103.918641 1.331993,103.918295 1.331828,103.917341 1.331372,103.916601 1.331026,103.916362 1.330914,103.915543 1.33053,103.914053 1.329832,103.912193 1.328951,103.911666 1.328686,103.911116 1.328387,103.91028 1.327898,103.910174 1.327836,103.90932 1.327348,103.90903 1.327215,103.908851 1.327134,103.908217 1.326902,103.908195 1.326894,103.908173 1.326888,103.907264 1.326629,103.906288 1.32634,103.905798 1.326188,103.905854 1.326809,103.905856 1.326826,103.905858 1.326847,103.905857 1.326863,103.905852 1.327003,103.905849 1.327063,103.905848 1.327095,103.905845 1.327181,103.905785 1.327587,103.905781 1.327602,103.905767 1.327659,103.905712 1.327887,103.905627 1.328127,103.905616 1.328159,103.905599 1.328208,103.905479 1.328515,103.905342 1.328733,103.905306 1.328789,103.905217 1.328937,103.9051 1.32913,103.904978 1.329287,103.904961 1.32931,103.904888 1.329404,103.904811 1.329475,103.904728 1.329551,103.904207 1.330044,103.902819 1.331407,103.902323 1.331884,103.903456 1.333077,103.903825 1.333384,103.904163 1.333603,103.904541 1.333798,103.904962 1.334001,103.905956 1.334486,103.906217 1.334577,103.906591 1.334652,103.908041 1.334911,103.908928 1.335103,103.909092 1.335144,103.910425 1.335475,103.910518 1.335506,103.911168 1.335719,103.911185 1.335724,103.911386 1.335815,103.912074 1.336137,103.912906 1.336548,103.913508 1.336846,103.914102 1.33713,103.914297 1.337224,103.915005 1.337569,103.916008 1.337852,103.916584 1.33798,103.916641 1.337993,103.91686 1.338025,103.917073 1.338057,103.917471 1.338116,103.917528 1.338125,103.917936 1.338218,103.917942 1.338221,103.917923 1.338439,103.917927 1.338649,103.917933 1.338703,103.917968 1.338989,103.918035 1.33933,103.918077 1.339544,103.918295 1.340343,103.91851 1.34101,103.918577 1.341219,103.918586 1.341276,103.918608 1.341422,103.918632 1.341712,103.918639 1.343742,103.91864 1.344103,103.91871 1.344565,103.918803 1.344948,103.918959 1.345269,103.919231 1.345637,103.919558 1.34595,103.9199 1.346169,103.920118 1.346234,103.921086 1.346633,103.923017 1.347439,103.92421 1.348027,103.924714 1.348363,103.924991 1.34864,103.925327 1.348254,103.926133 1.347515,103.926855 1.347004,103.927433 1.346684,103.929306 1.345518,103.930216 1.344947,103.930465 1.344737,103.930915 1.344323,103.93216 1.343131,103.933435 1.341844,103.935471 1.339809,103.935582 1.339779,103.935708 1.339655,103.935712 1.339648,103.935727 1.339618,103.935788 1.339498,103.936613 1.338691,103.938008 1.337311,103.938511 1.336807,103.938847 1.336529,103.939036 1.336426,103.93926 1.336358,103.940217 1.336177,103.940483 1.336144,103.940492 1.336143,103.943624 1.335547,103.946956 1.334897,103.949084 1.334439,103.949384 1.334066,103.950165 1.332927))"));
//        Geo2dSearchResult result = search.splitToRatio(geos,0.90,4000);
        Geo2dSearchResult result = search.splitToRatio(targets,0.85,2000);
        
//        result = search.splitToRatio(targets,0.98,1000);
        writer = Json.createWriter(System.out);
        IncrementalSearchResult incremental = result.getInc();
        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonArrayBuilder jab = Json.createArrayBuilder(incremental.added);
        job.add("added",jab);
        jab = Json.createArrayBuilder(incremental.removed);
        job.add("removed",jab);
        
        JsonObject jo = job.build();
        System.out.println("before");
        System.out.println(jo.toString().length());
        
//        writer.writeObject(jo);
        
        
        System.out.println("splitArray = JSON.parse('"+jo.toString()+"');");
        Thread.sleep(100);
        System.out.println("The end!");
        Thread.sleep(100);

        
//        System.out.println(result.getSubs().get(0));
//        System.out.println(result.getSubs().get(1));
//        System.out.println(result.getSubs().get(2));
//        System.out.println(result.getSubs().get(3));
    }
}
