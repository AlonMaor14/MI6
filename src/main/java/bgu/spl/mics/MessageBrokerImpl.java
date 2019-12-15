package bgu.spl.mics;

import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {

	private static MessageBrokerImpl ourInstance;
	private static HashMap<Subscriber, Queue<Message>> registers;//maybe should be blocking queue
	private static List<Subscriber> agentsAvailableList;//Subscribers subscribe to this list to get this events
	private static List<Subscriber> gadgetAvailableList;
	private static List<Subscriber> missionAvailableList;
	private static List<Subscriber> tickBroadcastList;
	/**
	 * Retrieves the single instance of this class.
	 */
	public static MessageBroker getInstance() {
		if (ourInstance == null) {
			ourInstance = new MessageBrokerImpl();
			registers = new HashMap<>();
			agentsAvailableList = new LinkedList<>();
			gadgetAvailableList = new LinkedList<>();
			missionAvailableList = new LinkedList<>();
			tickBroadcastList = new LinkedList<>();
		}
		return ourInstance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
		if(type.isAssignableFrom(AgentsAvailableEvent.class))
			agentsAvailableList.add(m);
			if(type.isAssignableFrom(GadgetAvailableEvent.class))
			gadgetAvailableList.add(m);
		if(type.isAssignableFrom(MissionReceivedEvent.class))
			missionAvailableList.add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
		if(type.isAssignableFrom(TickBroadcast.class))
			tickBroadcastList.add(m);

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(Subscriber m) {
		registers.put(m,new ArrayBlockingQueue<Message>(1));
	}

	@Override
	public void unregister(Subscriber m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(Subscriber m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
