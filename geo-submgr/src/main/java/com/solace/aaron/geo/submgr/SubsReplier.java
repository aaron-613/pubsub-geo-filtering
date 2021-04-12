/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.aaron.geo.submgr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import com.solace.aaron.geo.api.Geo2dSearch;
import com.solace.aaron.geo.api.Geo2dSearchResult;
import com.solace.aaron.geo.api.LatLonHelper;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.JCSMPTransportException;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.SessionEventArgs;
import com.solacesystems.jcsmp.SessionEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class SubsReplier {

    private static final String APP_NAME = SubsReplier.class.getSimpleName();
    private static final String TOPIC_PREFIX = "solace/samples";  // used as the topic "root"

    private static volatile boolean isShutdown = false;
    private static Geo2dSearch search;

    public static Map<String, List<String>> splitQuery(String paramStr) {
        if (paramStr.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(paramStr.split("&"))
                .map(SubsReplier::splitQueryParameter)
                .collect(Collectors.groupingBy(SimpleImmutableEntry::getKey, LinkedHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
    
    public static SimpleImmutableEntry<String, String> splitQueryParameter(String it) {
        final int idx = it.indexOf('=');
        final String key = idx > 0 ? it.substring(0, idx) : it;
        final String value = idx > 0 && it.length() > idx + 1 ? it.substring(idx + 1) : null;
        try {
            return new SimpleImmutableEntry<>(
                URLDecoder.decode(key, "UTF-8"),
                URLDecoder.decode(value, "UTF-8")
            );
        } catch (UnsupportedEncodingException e) {
            // won't happen
            throw new RuntimeException(e);
        }
    }

    
    /** Main method. */
    public static void main(String... args) throws JCSMPException, IOException {
        if (args.length < 3) {   // Check command line arguments
            System.out.printf("Usage: %s <host:port> <message-vpn> <client-username> [password]%n%n", APP_NAME);
            System.exit(-1);
        }
        System.out.println(APP_NAME + " initializing...");
        
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, args[0]);          // host:port
        properties.setProperty(JCSMPProperties.VPN_NAME,  args[1]);     // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, args[2]);      // client-username
        if (args.length > 3) {
            properties.setProperty(JCSMPProperties.PASSWORD, args[3]);  // client-password
        }
        properties.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);  // re-subscribe after reconnect
        JCSMPChannelProperties channelProps = new JCSMPChannelProperties();
        channelProps.setReconnectRetries(20);      // recommended settings
        channelProps.setConnectRetriesPerHost(5);  // recommended settings
        // https://docs.solace.com/Solace-PubSub-Messaging-APIs/API-Developer-Guide/Configuring-Connection-T.htm
        properties.setProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES, channelProps);
        final JCSMPSession session;
        session = JCSMPFactory.onlyInstance().createSession(properties, null, new SessionEventHandler() {
            @Override
            public void handleEvent(SessionEventArgs event) {  // could be reconnecting, connection lost, etc.
                System.out.printf("### Received a Session event: %s%n", event);
            }
        });
        session.connect();

        // Anonymous inner-class for handling publishing events 
        final XMLMessageProducer producer;
        producer = session.getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {
            // unused in Direct Messaging application, only for Guaranteed/Persistent publishing application
            @Override public void responseReceivedEx(Object key) {
            }
            
            // can be called for ACL violations, connection loss, and Persistent NACKs
            @Override
            public void handleErrorEx(Object key, JCSMPException cause, long timestamp) {
                System.out.printf("### Producer handleErrorEx() callback: %s%n",cause);
                if (cause instanceof JCSMPTransportException) {  // unrecoverable
                    isShutdown = true;
                }
            }

        });
        // Anonymous inner-class for request handling
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage msg) {
                System.out.println(msg.dump());
                if (msg.getDestination().getName().startsWith("GET") && msg.getDestination().getName().contains("circle") && msg.getReplyTo() != null) {
                    System.out.printf("Received request on '%s', generating response.%n", msg.getDestination());
                    try {
                        String paramStr;
                        if (msg.getDestination().getName().startsWith("GET")) {
                            paramStr = msg.getProperties().getString("JMS_Solace_HTTP_target_path_query_verbatim");
                        } else {
                            paramStr = msg.getDestination().getName();
                        }
                        System.out.println(paramStr);
                        paramStr = paramStr.substring(paramStr.indexOf('?')+1);
                        System.out.println(paramStr);
                        Map<String, List<String>> params = splitQuery(paramStr);
                        System.out.println(params.toString());
                        // 

                        double lat = Double.parseDouble(params.get("lat").get(0));
                        if (Math.abs(lat) > 90) throw new NumberFormatException("lat "+lat+" is out of range");
                        double lon = Double.parseDouble(params.get("lon").get(0));
                        if (Math.abs(lon) > 180) throw new NumberFormatException("lon "+lon+" is out of range");
                        double radiusMetres = Double.parseDouble(params.get("radiusMetres").get(0));
                        if (radiusMetres <= 0) throw new NumberFormatException("radiusMetres "+radiusMetres+" must be greater than 0");
                        int accuracy = 90;
                        try {
                            accuracy = Integer.parseInt(params.get("accuracy").get(0));
                        } catch (RuntimeException e) { }
                        if (accuracy < 30 || accuracy > 99) throw new NumberFormatException("accuracy "+accuracy+" must be in [30,99]");
                        int maxSubs = 1000;
                        try {
                            maxSubs = Integer.parseInt(params.get("maxSubs").get(0));
                        } catch (RuntimeException e) { }
                        if (maxSubs < 10) throw new NumberFormatException("maxSubs "+maxSubs+" must be greater than 10");
                        if (maxSubs > 2000) throw new NumberFormatException("maxSubs "+maxSubs+" must be less than 2000");

                        // all checks pass
                        Geometry circle = LatLonHelper.buildLatLonCircleGeometry(lat, lon, radiusMetres);
                        Geo2dSearchResult result = search.splitToRatio(Collections.singletonList(circle), accuracy / 100.0, maxSubs);
                        


                        TextMessage replyMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);  // reply with a Text
                        // the following has been fixed in SolOS 9.8
                        if (msg.getApplicationMessageId() != null) {
                            replyMsg.setApplicationMessageId(msg.getApplicationMessageId());  // populate for traceability
                        }


                        //JsonWriter writer = Json.createWriter(System.out);
                        JsonArrayBuilder jab = Json.createArrayBuilder(result.getSubs().get(0));
                        JsonObjectBuilder job = Json.createObjectBuilder();
                        job.add("subs",jab);
                        //jab = Json.createArrayBuilder(result.getSquares().get(0));
                        //job.add("squares",jab);
                        job.add("perimiter",result.getUnion().get(0).toString());
                        //System.out.println(jo.toString().length());
                

                        replyMsg.setText(job.build().toString());
                        producer.sendReply(msg, replyMsg);  // convenience method: copies in reply-to, correlationId, etc.
                    // } catch (NumberFormatException | NullPointerException e) {
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        if (msg.getDestination().getName().startsWith("GET")) {  // need to send an error response
                            try {
                                BytesMessage replyMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);  // reply with a Text
                                SDTMap userProps = JCSMPFactory.onlyInstance().createMap();
                                userProps.putInteger("JMS_Solace_HTTP_status_code",200);
                                userProps.putString("JMS_Solace_HTTP_reason_phrase","Missing/invalid arguments: lat [-90,90], lon [-180,180], radiusMetres > 0; "+e.toString());
                                replyMsg.setProperties(userProps);
                                replyMsg.setData(e.toString().getBytes());
                                producer.sendReply(msg, replyMsg);  // convenience method: copies in reply-to, correlationId, etc.
                            } catch (JCSMPException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    } catch (JCSMPException e) {
                        System.out.printf("### Caught while trying to producer.sendReply(): %s%n", e);
                        if (e instanceof JCSMPTransportException) {  // unrecoverable
                            isShutdown = true;
                        }
                    }
                    
                } else if (msg.getDestination().getName().contains("rect") && msg.getReplyTo() != null) {
                    System.out.printf("Received request on '%s', generating response.%n", msg.getDestination());
                    try {
                        String paramStr = msg.getProperties().getString("JMS_Solace_HTTP_target_path_query_verbatim");
                        System.out.println(paramStr);
                        paramStr = paramStr.substring(paramStr.indexOf('?')+1);
                        System.out.println(paramStr);
                        Map<String, List<String>> params = splitQuery(paramStr);
                        System.out.println(params.toString());

                        double lat1 = Double.parseDouble(params.get("lat1").get(0));
                        if (Math.abs(lat1) > 90) throw new NumberFormatException("lat1 "+lat1+" is out of range");
                        double lat2 = Double.parseDouble(params.get("lat2").get(0));
                        if (Math.abs(lat2) > 90) throw new NumberFormatException("lat2 "+lat2+" is out of range");
                        double lon1 = Double.parseDouble(params.get("lon1").get(0));
                        if (Math.abs(lon1) > 90) throw new NumberFormatException("lon1 "+lon1+" is out of range");
                        double lon2 = Double.parseDouble(params.get("lon2").get(0));
                        if (Math.abs(lon2) > 90) throw new NumberFormatException("lon2 "+lon2+" is out of range");
                        Coordinate[] coords = {
                            new Coordinate(lon1,lat1),
                            new Coordinate(lon2,lat1),
                            new Coordinate(lon2,lat2),
                            new Coordinate(lon1,lat2),
                            new Coordinate(lon1,lat1),
                        };
                        Geometry rect = new GeometryFactory().createPolygon(coords);
                        Geo2dSearchResult result = search.splitToRatio(Collections.singletonList(rect), 0.75, 500);
                        


                        TextMessage replyMsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);  // reply with a Text
                        // the following has been fixed in SolOS 9.8
                        // if (msg.getApplicationMessageId() != null) {
                        //     replyMsg.setApplicationMessageId(msg.getApplicationMessageId());  // populate for traceability
                        // }
                        final String text = "Hello! Here is a response to your message on topic '" + msg.getDestination() + "'.";
                        replyMsg.setText(String.format("subs: %s%nboundary: %s%n",result.getSubs().get(0).toString(),result.getUnion().get(0).toString()));
                        // only allowed to publish messages from API-owned (callback) thread when JCSMPProperties.MESSAGE_CALLBACK_ON_REACTOR == false
                        producer.sendReply(msg, replyMsg);  // convenience method: copies in reply-to, correlationId, etc.
                    } catch (NumberFormatException | NullPointerException e) {
                        if (msg.getDestination().getName().startsWith("GET")) {  // need to send an error response
                            try {
                                BytesMessage replyMsg = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);  // reply with a Text
                                SDTMap userProps = JCSMPFactory.onlyInstance().createMap();
                                userProps.putInteger("JMS_Solace_HTTP_status_code",400);
                                userProps.putString("JMS_Solace_HTTP_reason_phrase","Missing/invalid arguments: lat [-90,90], lon [-180,180], radiusMetres > 0; "+e.toString());
                                replyMsg.setProperties(userProps);
                                producer.sendReply(msg, replyMsg);  // convenience method: copies in reply-to, correlationId, etc.
                            } catch (JCSMPException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    } catch (JCSMPException e) {
                        System.out.printf("### Caught while trying to producer.sendReply(): %s%n", e);
                        if (e instanceof JCSMPTransportException) {  // unrecoverable
                            isShutdown = true;
                        }
                    }
                    
                } else {
                    System.out.println("Received message without reply-to field");
                }

            }

            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n", e);
            }
        });

        search = Geo2dSearch.buildDecimalGeo2dSearch(5,9,8);


        // topic to listen to incoming (messaging) requests, using a special wildcard borrowed from MQTT:
        // https://docs.solace.com/Open-APIs-Protocols/MQTT/MQTT-Topics.htm#Using
        // will match "solace/samples/direct/request" as well as "solace/samples/direct/request/anything/else"
        session.addSubscription(JCSMPFactory.onlyInstance().createTopic("geo/subs/circle*"));
        session.addSubscription(JCSMPFactory.onlyInstance().createTopic("GET/geo/subs/circle*"));
        session.addSubscription(JCSMPFactory.onlyInstance().createTopic("GET/geo/subs/rect*"));
        // for use with HTTP MicroGateway feature, will respond to REST GET request on same URI
        // try doing: curl -u default:default http://localhost:9000/solace/samples/direct/request/hello
        // session.addSubscription(JCSMPFactory.onlyInstance().createTopic("GET/" + TOPIC_PREFIX + "/direct/request/\u0003"));
        // session.addSubscription(JCSMPFactory.onlyInstance().createTopic(TOPIC_PREFIX + "/control/>"));
        cons.start();

        System.out.println(APP_NAME + " connected, and running. Press [ENTER] to quit.");
        while (System.in.available() == 0 && !isShutdown) {
            try {
                Thread.sleep(1000);  // wait 1 second
            } catch (InterruptedException e) {
                // Thread.sleep() interrupted... probably getting shut down
            }
        }
        isShutdown = true;
        session.closeSession();  // will also close producer and consumer objects
        System.out.println("Main thread quitting.");
    }
}
