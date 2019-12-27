package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBroker;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.ReleaseAgentsEvent;
import bgu.spl.mics.application.messages.SendAgentsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Squad;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {

	private int id;

	private int currTick;
	public Moneypenny(int id) {
		super("Moneypenny "+id);
		this.id = id;
	}

	@Override
	protected void initialize() {
		Squad ourSquad= Squad.getInstance();
		Callback<TickBroadcast> callbackTimeTickBroadcast = new Callback<TickBroadcast>() {
			@Override
			public void call(TickBroadcast c) {
				currTick = c.getCurrTick();
			}
		};

		Callback<AgentsAvailableEvent> callbackAgentsAvailable = new Callback<AgentsAvailableEvent>() {
			@Override
			public void call(AgentsAvailableEvent e) {
				boolean acquired = ourSquad.getAgents(e.getSerialNumbers());
				if (acquired) {
					Pair<Integer, List<String>> result = new Pair<>(id, ourSquad.getAgentsNames(e.getSerialNumbers()));
					complete(e, result);
					System.out.println(getName() + " waiting for gadget");
					int send = e.getSend();
					if (send == 1) {
						ourSquad.sendAgents(e.getSerialNumbers(), e.getTime());
						System.out.println("sent agents");
					} else if (send == -1) {
						ourSquad.releaseAgents(e.getSerialNumbers());
						System.out.println("released agents");
					}
					System.out.println("int sent was " + send);
				}else
					complete(e,new Pair<>(-1,null));
			}
		};

		Callback<SendAgentsEvent> callbackSendAgents = new Callback<SendAgentsEvent>() {
			@Override
			public void call(SendAgentsEvent e) {
				ourSquad.sendAgents(e.getAgents(),e.getTime());
				List<String> agentNameList=ourSquad.getAgentsNames(e.getAgents());
				complete(e,agentNameList);
			}
		};

		Callback<ReleaseAgentsEvent> callbackReleaseAgents = new Callback<ReleaseAgentsEvent>() {
			@Override
			public void call(ReleaseAgentsEvent e) {
				ourSquad.releaseAgents(e.getAgentsToRelease());
				complete(e,true);
			}
		};
		//if(id != 1)
			this.subscribeEvent(AgentsAvailableEvent.class, callbackAgentsAvailable);
		this.subscribeEvent(SendAgentsEvent.class, callbackSendAgents);
		this.subscribeEvent(ReleaseAgentsEvent.class, callbackReleaseAgents);
		this.subscribeBroadcast(TickBroadcast.class,callbackTimeTickBroadcast);

	}


}
