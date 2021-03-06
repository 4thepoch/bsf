package edu.bath.arDrone;

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

public class WorkerNonThreadSender extends Sensor  {

	private boolean alive = true;
	private String currentLocation;
	private String primaryHandle;
	SensorClient sensorClient;
	private String temp;
	private ArrayList<RDFHalf> messageStore;
	private String URIRequestsURL = "http://127.0.0.1/";
	private boolean debug = true;
	private String vehicleName="unassigned";

	
	private class RDFHalf
	{
		String pred;
		String val;
	}
	
	public WorkerNonThreadSender(String serverAddress, String id, String password, String nodeName, String currentLocation, String primaryHandle) throws XMPPException {
		super(serverAddress, id , password, nodeName);
		this.currentLocation = currentLocation;
		this.primaryHandle = primaryHandle;
		this.vehicleName=id;
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
	
	public void sendPublishedRate(int quantity)
	{
		try {
			DataReading testReading = new DataReading(getPrimaryHandle(), getCurrentLocation(), System.currentTimeMillis());
			testReading.setTakenBy("http://127.0.0.1/vehicles/"+vehicleName);
			String msgInfo = quantity + ""; 
			testReading.addDataValue(null, "http://127.0.0.1/simDefinitions/publishedRate", msgInfo, false);
			publish(testReading);
			//System.out.println("sent sim count message: " + msgInfo);
		}
		catch (Exception e)
		{
			System.out.println("couldnt construct sim message count message");
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
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			messageStore.clear();
		}
	}
}
