package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {

    private int currTick;

    public Q() {
        super("Q");
    }

    @Override
    protected void initialize() {
        Inventory ourInventory = Inventory.getInstance();
        Callback<TickBroadcast> callbackTimeTickBroadcast = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast c) {
                currTick = c.getCurrTick();
            }
        };
        Callback<GadgetAvailableEvent> callbackGadgetAvailable = new Callback<GadgetAvailableEvent>() {
            @Override
            public void call(GadgetAvailableEvent e) {
                if (!ourInventory.getItem(e.getGadget())) {
                    complete(e, -1);
                } else {
                    complete(e, currTick);
                }

            }
        };

        this.subscribeEvent(GadgetAvailableEvent.class, callbackGadgetAvailable);
        this.subscribeBroadcast(TickBroadcast.class, callbackTimeTickBroadcast);
    }

}
