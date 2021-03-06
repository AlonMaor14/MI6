package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Inventory;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {

    private static ConcurrentHashMap<String, Queue<Message>> registers;
    private static ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<Subscriber>> broadcastSubscribers;
    private static ConcurrentHashMap<Class<? extends Event>, LinkedBlockingQueue<Subscriber>> messagesSubscribers;
    private static ConcurrentHashMap<Event, Future> events;

    /**
     * Retrieves the single instance of this class.
     */
    private static class SingletonHolder {
        private static MessageBrokerImpl instance = new MessageBrokerImpl();
    }
    private MessageBrokerImpl() {
        registers = new ConcurrentHashMap<>();
        messagesSubscribers = new ConcurrentHashMap<>();
        broadcastSubscribers = new ConcurrentHashMap<>();
        events = new ConcurrentHashMap<>();
    }
    public static MessageBroker getInstance() {
        return SingletonHolder.instance;//safe singleton will not be loaded until this reference and only 1 thread loads classes
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
        synchronized (messagesSubscribers) {
            if (!messagesSubscribers.containsKey(type))
                messagesSubscribers.put(type, new LinkedBlockingQueue<>());
            messagesSubscribers.get(type).add(m);
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
        synchronized (broadcastSubscribers) {
            if (!broadcastSubscribers.containsKey(type))
                broadcastSubscribers.put(type, new LinkedBlockingQueue<>());
            broadcastSubscribers.get(type).add(m);
        }
    }


    @Override
    public <T> void complete(Event<T> e, T result) {
        events.get(e).resolve(result);

    }

    @Override
    public void sendBroadcast(Broadcast b) {
        synchronized (broadcastSubscribers) {
            if (!broadcastSubscribers.get(b.getClass()).isEmpty()) {
                for (Subscriber s : broadcastSubscribers.get(b.getClass())) {
                    registers.get(s.getName()).add(b);
                }
            }
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        synchronized (messagesSubscribers) {
            if (!messagesSubscribers.get(e.getClass()).isEmpty()) {
                Subscriber s = messagesSubscribers.get(e.getClass()).poll();
                registers.get(s.getName()).add(e);
                messagesSubscribers.get(e.getClass()).add(s);
                Future<T> future = new Future<>();
                events.put(e, future);
                return future;
            }
            return null;
        }
    }

    @Override
    public void register(Subscriber m) {
        registers.put(m.getName(), new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(Subscriber m) {
        messagesSubscribers.forEach((key, value) -> value.remove(m));
        broadcastSubscribers.forEach((key, value) -> value.remove(m));
        registers.remove(m.getName());
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {
        while (registers.get(m.getName()).isEmpty()) {
            TimeUnit.NANOSECONDS.sleep(100);
        }
        return registers.get(m.getName()).poll();
    }
}



