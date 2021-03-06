package edu.bath.rdfUtils.rdfReplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import edu.bath.sensorframework.DataReading;
import edu.bath.sensorframework.Visualisation;
import edu.bath.sensorframework.DataReading.Value;
import edu.bath.sensorframework.client.ReadingHandler;
import edu.bath.sensorframework.client.SensorClient;
import edu.bath.sensorframework.sensor.Sensor;

import org.jivesoftware.smack.XMPPException;
import java.util.Random;

public class WorkerSimNonThreadSender extends Sensor  {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private ArrayList<RDFHalf> messageStore;
	private String URIRequestsURL = "http://127.0.0.1/simDefinitions/";

	
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
		RDFHalf newMsg = new RDFHalf();
		newMsg.pred=URIRequestsURL+predicate;
		newMsg.val=objectVal;
		synchronized(messageStore){
                    messageStore.add(newMsg);
		}
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
