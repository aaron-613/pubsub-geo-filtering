package com.solace.aaron.geo.submgr;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import com.solace.aaron.geo.api.Geo2dSearch;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ClientName;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPErrorResponseException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.SessionEventArgs;
import com.solacesystems.jcsmp.SessionEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.TopicProperties;
import com.solacesystems.jcsmp.User_Cos;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class GeoRoutingSubscriptionManager implements XMLMessageListener {

    private static final String REQUEST_TOPIC_V3 = "geo/request/submgr/>";

    private static final int NUM_OF_THREADS = 3;
    private ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);
    private LinkedBlockingQueue<BytesXMLMessage> requestQueue = new LinkedBlockingQueue<BytesXMLMessage>();
    
    enum SubAction {
        ADD,
        REMOVE,
    }
    
    class RequestHandler implements Runnable {
        
        final int id;
        
        RequestHandler(int id) {
            this.id = id;
            logger.info("RequestHandler "+id+" has started.");
        }

        @Override
        public void run() {
            try {
                while (true) {
                    BytesXMLMessage origMessage = requestQueue.take();
                    try {
                        logger.info("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
                        logger.info(origMessage.dump());
                        logger.info("Received request message, trying to parse it");
                        String body;
                        if (origMessage instanceof TextMessage) {
                            body = ((TextMessage)origMessage).getText();
                        } else {
                            ByteBuffer bb = origMessage.getAttachmentByteBuffer();
                            byte[] bytes = new byte[origMessage.getAttachmentContentLength()];
                            bb.get(bytes);
                            body = new String(bytes,Charset.forName("UTF-8"));
                        }
                        logger.info("COORDS: "+body);
                        produceResponse(body,origMessage);
                        logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                    } catch (JCSMPException e) {
                        logger.info("Failed to parse the request message:");
                        e.printStackTrace();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    
                }
            } catch (InterruptedException e) {
                logger.info("I got interrupted waiting to take a request off the queue");
            }
        }
    };
    
    
    
    private JCSMPSession session = null;
    private JCSMPProperties properties = null;
    private XMLMessageProducer producer = null;
    private XMLMessageConsumer consumer = null;

    private final String host;
    private final String vpn;
    private final String user;
    private final String password;

    private static final Logger logger = LogManager.getLogger(GeoRoutingSubscriptionManager.class);

    public GeoRoutingSubscriptionManager(String host, String vpn, String user, String password) {
        this.host = host;
        this.vpn = vpn;
        this.user = user;
        this.password = password;
    }
    
    void produceResponse(final String body, final BytesXMLMessage origMsg) throws JCSMPException {
        JsonReader reader = Json.createReader(new StringReader(body));
        JsonObject msgJsonObject = reader.readObject();
        JsonObject previous = msgJsonObject.getJsonObject("previous");
        Search search = null;
        if (previous.isEmpty()) {
        	// all good, means this is the first search
        } else {
        	search = new Search(previous);
        	doSubscriptions(search,SubAction.REMOVE,origMsg);
        }
        search = new Search(msgJsonObject);
    	doSubscriptions(search,SubAction.ADD,origMsg);        
    }
    
    void doSubscriptions(final Search search, final SubAction action, final BytesXMLMessage origMsg) throws JCSMPException {
        ClientName clientName = JCSMPFactory.onlyInstance().createClientName(origMsg.getSenderId());
        Geometry target = search.target;
//        Geometry target = WktTests.getOrchard();
        Geo2dSearch grid = new Geo2dSearch(10,6,7,4,4,100,200,target);  //TODO target is null essentially if doing an 'undo'  
        List<String> subs = grid.getSubs();
        if (!grid.intersects()) {
            // means that no search area was specified, or something completely outside the earth was.  So add/remove everything!
            TopicProperties tp = new TopicProperties();
            tp.setName(search.topicPrefix + "*/*" + search.topicSuffix);
            tp.setRxAllDeliverToOne(false);
            Topic topic = JCSMPFactory.onlyInstance().createTopic(tp);
            logger.info(action.name()+": "+topic.toString());
            try {
                if (action == SubAction.ADD) {
                    session.addSubscription(clientName,topic,0);//JCSMPSession.WAIT_FOR_CONFIRM);
                } else {
                    session.removeSubscription(clientName,topic,0);//JCSMPSession.WAIT_FOR_CONFIRM);
                }
            } catch (JCSMPErrorResponseException e) {
                e.printStackTrace();
            }
            return;
        }
        // this sends messages one at a time during the split so you can see the animation
//        while (!cond.isDone()) {
//            grid.splitOne();
//            System.out.printf("Range: [%.1f : %.1f]%n",grid.getCoarsestFactor(),grid.getFinestFactor());
//            logger.info("inter ratio: "+grid.getCurrentCoverageRatio());
//            subs = grid.getSubs();
//            logger.info("sub count: "+subs.size());
//            if (action == SubAction.ADD) sendMessage(createReplyMessage(search,grid),origMsg.getReplyTo());
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//            }
//        }
        grid.splitToRatio(search.accuracy,search.numSubs);
//        System.out.printf("Range: [%.1f : %.1f]%n",grid.getCoarsestFactor(),grid.getFinestFactor());
        logger.info("inter ratio: "+grid.getCurrentCoverageRatio());
        subs = grid.getSubs();
        logger.info("sub count: "+subs.size());
        // this sends a message with 
        if (action == SubAction.ADD) sendReply(origMsg,createReplyMessage(search,grid));
        subs = grid.getSubs();
        for (String sub : subs) {
            TopicProperties tp = new TopicProperties();
            tp.setName(search.topicPrefix + sub + search.topicSuffix);
            tp.setRxAllDeliverToOne(false);
            Topic topic = JCSMPFactory.onlyInstance().createTopic(tp);
            try {
//                logger.info(action.name()+": "+topic.toString());
                if (action == SubAction.ADD) {
                    session.addSubscription(clientName,topic,0);
                } else {
                    session.removeSubscription(clientName,topic,0);
                }
            } catch (JCSMPErrorResponseException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    //Create a success reply with a result
    private BytesXMLMessage createReplyMessage(Search search, Geo2dSearch grid) {
        BytesXMLMessage replyMessage = JCSMPFactory.onlyInstance().createMessage(BytesXMLMessage.class);
        List<String> subs = grid.getSubs();
        logger.info("A total of "+subs.size()+" subscriptions were generated");
        int subBytes = 0;
        for (String sub : subs) {
        	subBytes += sub.length();
        }
        logger.info("Total # of sub bytes = "+subBytes);
        logger.info("Final area coverage ratio = "+grid.getCurrentCoverageRatio());
        StringBuilder sb = new StringBuilder();
        String result;
        if ("blah".equals("blasbab")) {
            Geometry union = grid.getUnion();
            logger.info("Number of geometries: "+union.getNumGeometries());
            for (int i=0;i<union.getNumGeometries();i++) {
                Geometry single = union.getGeometryN(i);
                // simplify!
                single = DouglasPeuckerSimplifier.simplify(single,0.0005);
                Coordinate[] cs = single.getCoordinates();
                for (Coordinate c : cs) {
                    sb.append(String.format("%.7f,%.7f;",c.x,c.y));
                }
                sb.setLength(sb.length()-1);
                sb.append('|');
            }
            sb.setLength(sb.length()-1);
            result = String.format("Num Subs = %d, Coverage Ratio = %.3f%%|%s",subs.size(),grid.getCurrentCoverageRatio()*100,sb.toString());
        } else if ("rather return".equals("wkt strings")) {

        	result = String.format("Num Subs = %d, Coverage Ratio = %.3f%%|%s",subs.size(),grid.getCurrentCoverageRatio()*100,sb.toString());
        } else {
            if (grid.getSubs().size() > 0) {
                for (String[] coord : grid.getSquares()) {
                    sb.append(coord[0]).append(',').append(coord[1]).append(';');
                    sb.append(coord[2]).append(',').append(coord[3]).append('|');
                }
                sb.setLength(sb.length()-1);
            }
            result = String.format("Num Subs = %d, Coverage Ratio = %.3f%%|%s",subs.size(),grid.getCurrentCoverageRatio()*100,sb.toString());
        }
        replyMessage.writeAttachment(result.getBytes(Charset.forName("UTF-8")));
        System.out.printf("This is the result (%d bytes): '%s'%n",result.length(),result.substring(0, Math.min(result.length(),500)));
        replyMessage.setDeliveryMode(DeliveryMode.DIRECT);
        replyMessage.setCos(User_Cos.USER_COS_2);  // bump up the priority slightly so that it jumps in front of any buffered-up D1 messages
        return replyMessage;
    }
    
    //Reply to a request
    void sendReply(BytesXMLMessage request, final BytesXMLMessage reply) throws JCSMPException {
        producer.sendReply(request, reply);
    }
    
    void sendMessage(BytesXMLMessage message, Destination destination) throws JCSMPException {
        producer.send(message,destination);
    }
    
    public void onReceive(BytesXMLMessage origMessage) {
        try {
            requestQueue.add(origMessage);
        } catch (IllegalStateException e) {
            logger.info("Queue is full???  Cannot add any more requests to queue");
            e.printStackTrace();
        }
    }
    
    public void onException(JCSMPException e) {
        e.printStackTrace();
    }

    
    void run() throws JCSMPException {
        logger.info("About to create session.");
        properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST,host);
        //properties.setProperty(JCSMPProperties.HOST,"172.31.234.60");
        properties.setProperty(JCSMPProperties.VPN_NAME,vpn);
        //properties.setProperty(JCSMPProperties.VPN_NAME,"geo-demo-vmr");
        properties.setProperty(JCSMPProperties.USERNAME,user);
        properties.setProperty(JCSMPProperties.PASSWORD,password);
        properties.setBooleanProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);
        properties.setBooleanProperty(JCSMPProperties.IGNORE_DUPLICATE_SUBSCRIPTION_ERROR,true);
        properties.setBooleanProperty(JCSMPProperties.IGNORE_SUBSCRIPTION_NOT_FOUND_ERROR,true);
        JCSMPChannelProperties channelProps = new JCSMPChannelProperties();
        channelProps.setConnectRetries(3);
        channelProps.setReconnectRetries(-1);  // retry forever
        properties.setProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES,channelProps);
        session = JCSMPFactory.onlyInstance().createSession(properties,JCSMPFactory.onlyInstance().getDefaultContext(),new SessionEventHandler() {
            @Override
            public void handleEvent(SessionEventArgs event) {
                logger.warn(">>>>>>>>>>>>>>>>>>>  "+event.toString());
            }
        });
        session.setProperty(JCSMPProperties.CLIENT_NAME,"GeoSubMgr_"+session.getProperty(JCSMPProperties.CLIENT_NAME));
        session.connect();
        try {
            consumer = session.getMessageConsumer(this);
            producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {
                
                @Override
                public void responseReceived(String messageID) {
                    // not publishing direct messages, so no problem
                }
                
                @Override
                public void handleError(String messageID, JCSMPException cause, long timestamp) {
                    logger.error("##### got this publisher error: "+cause.toString());
                }
            });
            final int NUM_OF_THREADS = 1;
            for (int i=0;i<NUM_OF_THREADS;i++) {
                executorService.execute(new RequestHandler(i));
            }
            consumer.start();
//            session.addSubscription(JCSMPFactory.onlyInstance().createTopic(REQUEST_TOPIC), true);
            session.addSubscription(JCSMPFactory.onlyInstance().createTopic(REQUEST_TOPIC_V3), true);
            logger.info("Listening for request messages...");
            while (true) {
                Thread.sleep(10000);
            }
        } catch (JCSMPException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.closeSession();
            }
        }
    }
    
    public static void main(String... args) throws JCSMPException  {
        if (args.length < 3) {
            System.out.println("Not enough args");
            System.out.println("Usage: "+GeoRoutingSubscriptionManager.class.getName()+" <Solace IP or hostname> <vpn> <user> <password>");
            System.exit(-1);
        }
        String host = args[0];
        String vpn = args[1];
        String user = args[2];
        String pw = args.length > 3 ? args[3] : "";
        GeoRoutingSubscriptionManager mgr = new GeoRoutingSubscriptionManager(host,vpn,user,pw);
        mgr.run();
    }
}
