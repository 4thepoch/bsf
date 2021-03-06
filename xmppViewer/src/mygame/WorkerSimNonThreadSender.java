package mygame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.*;
import edu.bath.sensorframework.sensor.Sensor;

import org.jivesoftware.smack.XMPPException;

public class WorkerSimNonThreadSender extends Sensor  {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	private String temp;
	private ArrayList<RDFHalf> messageStore;
	private String URIRequestsURL = "http://127.0.0.1/request/";
        private final double updateRate = 0.5;
	
	private class RDFHalf
	{
		String pred;
		String val;
	}
	
	public WorkerSimNonThreadSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id, password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
		this.messageStore = new ArrayList<RDFHalf>();
	}
	
	public WorkerSimNonThreadSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle, boolean useMQTT, int qos) throws XMPPException {
		super(serverAddress, id, password, nodeName, useMQTT, qos);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
		this.messageStore = new ArrayList<RDFHalf>();
	}

	public String getCurrentLocation() {
		return currentLocation;
	}
	
	public String getPrimaryHandle() {
		return primaryHandle;
	}

	public synchronized void addMessageToSend(String predicate, String objectVal) {
		System.out.println("workersimthreadsender adding " + predicate + " " + objectVal);
		RDFHalf newMsg = new RDFHalf();
		newMsg.pred=URIRequestsURL+predicate;
		newMsg.val=objectVal;
		synchronized(messageStore){
                    messageStore.add(newMsg);
		}
	}
        
        public void disconnect()
        {
            messageStore.clear();
            cleanup();
            //disconnect();
        }

	public void send() 
        {
		synchronized(messageStore)
                {
			for(RDFHalf rdfMsg : messageStore) 
			{
				try 
				{
					DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
					testReading.addDataValue(null, rdfMsg.pred, rdfMsg.val, false);
                                        System.out.println("Sending: " + testReading.toString());
					publish(testReading);
				}	
				catch (Exception e) {
					e.printStackTrace();
				}
			}
                }
		messageStore.clear();
	}
}
