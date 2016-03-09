package com.algoeye.adapter.ib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algoeye.adapter.IInstrument;
import com.algoeye.adapter.IPriceAndTradeFeedListener;
import com.algoeye.adapter.IPriceAndTradeFeedProvider;

import com.algoeye.adapter.VolumePrice;
import com.ib.client.Contract;
import org.apache.log4j.Logger;

import com.ib.client.TickType;

public class IBMarketDataProvider extends IBCallbackAdaptor implements
        IPriceAndTradeFeedProvider, Runnable
{
    private final Logger l = Logger.getLogger(getClass());

    private final IBConnection connection;
    private final Map<IInstrument, InstrumentData> dataBySymbol = new HashMap<IInstrument, InstrumentData>();
    private final Map<Integer, InstrumentData> dataByTickId = new HashMap<Integer, InstrumentData>();
    private final Map<Integer, InstrumentData> dataByDepthId = new HashMap<Integer, InstrumentData>();

	public IBMarketDataProvider(IBConnection connection)
    {
        this.connection = connection;
    }

    class InstrumentData
    {
        final IInstrument instrument;
        final VolumePrice bid = new VolumePrice();
        final VolumePrice ask = new VolumePrice();
        final VolumePrice last = new VolumePrice();
        final List<IPriceAndTradeFeedListener> listeners = new ArrayList<IPriceAndTradeFeedListener>();
        int volume = 0;
        int tickerId = 0;
        int depthId = 0;
        int resubscribeCounter = 10;
        private IPriceAndTradeFeedProvider.SubscriptionState subscriptionState = SubscriptionState.UNKNOWN;

        InstrumentData(IInstrument instrument)
        {
            this.instrument = instrument;
        }
        
        VolumePrice getSide(int tickType)
        {
            switch (tickType)
            {
                case TickType.BID:
                case TickType.BID_SIZE: return bid;
                case TickType.ASK:
                case TickType.ASK_SIZE: return ask;
                default: return null;
            }            
        }

        void sendQuote()
        {
            try
            {
                for (IPriceAndTradeFeedListener listener : listeners)
                {
                    listener.OnNewPrice(instrument, bid, ask);
                }
            }
            catch(Exception e)
            {
                l.fatal("Exception caught while processing quote notification for " + instrument.getSymbol(), e);
            }
        }

        void sendTrade()
        {
            try
            {
                for (IPriceAndTradeFeedListener listener : listeners)
                {
                    listener.OnNewTrade(instrument, last);
                }
            }
            catch(Exception e)
            {
                l.fatal("Exception caught while processing trade notification for " + instrument.getSymbol(), e);
            }
        }

        void sendClose(double price)
        {
            try
            {
                for (IPriceAndTradeFeedListener listener : listeners)
                {
                    listener.OnClosePrice(instrument, price);
                }
            }
            catch(Exception e)
            {
                l.fatal("Exception caught while processing trade notification for " + instrument.getSymbol(), e);
            }
        }
    }

    @Override
    public void tickPrice(int tickId, int tickType, double price,
            int canAutoExecute)
    {
        InstrumentData data = dataByTickId.get(tickId);

        if (data == null)
        {
            l.debug("Unknown tick price id=" + tickId + ": "
                    + TickType.getField(tickType) + "/" + price + "/"
                    + canAutoExecute);
            return;
        }

        if (l.isTraceEnabled())
        {
            l.trace("Tick price id=" + tickId + ": " + TickType.getField(tickType)
                    + "/" + price + "/" + canAutoExecute);
        }

        data.last.volume = 0;
        data.subscriptionState = SubscriptionState.SUBSCRIBED;

        switch (tickType)
        {
            case TickType.BID:
            case TickType.ASK:
                VolumePrice side = data.getSide(tickType);
                if (price <= 0.0)
                {
                    if (side.isValid())
                    {
                        side.clear();
                        data.sendQuote();
                    }
                }
                else
                if (price != side.price)
                {
                    side.price = price;
                    side.volume = 0; // force tickSize() to emit this quote
                }
                break;
            case TickType.LAST:
                data.last.price = price;
                break;
            case TickType.CLOSE:
                if (price > 0)
                {
                    data.sendClose(price);
                }
                break;
            default:
//                l.warn("unhandled price tick type: " + TickType.getField(tickType) + " (" + tickType + ")");
        }
    }

    @Override
    public void tickSize(int tickId, int tickType, int size)
    {
        InstrumentData data = dataByTickId.get(tickId);

        if (data == null)
        {
            l.debug("Unknown tick size id=" + tickId + ": "
                    + TickType.getField(tickType) + "/" + size);
            return;
        }

        if (l.isTraceEnabled())
        {
            l.trace("Tick size id=" + tickId + ": " + TickType.getField(tickType) + "/" + size);
        }

        double lastSize = data.last.volume;
        data.last.volume = 0;
        data.subscriptionState = SubscriptionState.SUBSCRIBED;

        switch (tickType)
        {
            case TickType.BID_SIZE:
            case TickType.ASK_SIZE:
                VolumePrice side = data.getSide(tickType);
                // do not flood with the same quotes!
                if (size <= 0)
                {
                    if (side.isValid())
                    {
                        side.clear();
                        data.sendQuote();
                    }
                }
                else
                if (size != side.volume)
                {
                    side.volume = size;
                    if (side.isValid())
                    {
                        // do not distribute incomplete quote
                        data.sendQuote();
                    }
                }
                break;

            case TickType.LAST_SIZE:
                data.last.volume = size;
                break;

            case TickType.VOLUME:
                if (data.volume > 0 && size-data.volume > 0)
                {
                    data.last.volume = size-data.volume;
                    data.sendTrade();
                }
                data.volume = size;
                break;

            default:
//                l.warn("unhandled size tick type: " + TickType.getField(tickType) + " (" + tickType + ")");
        }
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size)
    {
        updateMktDepthL2(tickerId, position, null, operation, side, price, size);
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size)
    {
        InstrumentData data = dataByDepthId.get(tickerId);

        if (data == null)
        {
            l.debug("Unknown depth id=" + tickerId);
            return;
        }

        try
        {
            char op = (operation == 0) ? 'I' : (operation == 1) ? 'U' : (operation == 2) ? 'D' : ' ';
            char si = (side == 0) ? 'A' : 'B';

            for (IPriceAndTradeFeedListener listener : data.listeners)
            {
                listener.OnDepth(data.instrument, marketMaker, op, si, position, size, price);
            }
        }
        catch(Exception e)
        {
            l.fatal("Exception caught while processing depth notification for " + data.instrument.getSymbol(), e);
        }
    }

    @Override
    public void error(Exception e)
    {
        l.error("Got exception: " + e.getMessage(), e);
    }

    @Override
    public void error(String str)
    {
        l.error("Error: " + str);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg)
    {
        InstrumentData data = dataByTickId.get(id);

        if (data != null)
        {
            // set error code for subscription data
            switch(errorCode)
            {
                case 101:   // Max number of tickers has been reached
                    data.subscriptionState = SubscriptionState.LIMITED;
                    break;
                case 354:   // Requested market data is not subscribed
                    data.subscriptionState = SubscriptionState.RESTRICTED;
                    data.resubscribeCounter = 0; // stop resubscribing
                    break;
                default:
                    data.subscriptionState = SubscriptionState.UNAVAILABLE;
            }
        }

        // logging
        if (errorCode >= 1000)  // system and warning messages
        {
            l.warn("[" + errorCode + "] : " + errorMsg);
        }
        else
        if (data != null)
        {
            data.tickerId = 0;
            l.error("Error: id=" + id + " [" + errorCode + "] " + data.instrument.getSymbol() + ": " + errorMsg);
            if (errorCode == 101 && --data.resubscribeCounter >= 0)
            {
                // Max number of tickers has been reached - retry
                subscribe(data);
            }
        }
    	
    	// actions
        switch (errorCode)
        {
        case 1102: // Connectivity between IB and TWS has been restored - data maintained
        // 2104 - this one causes connection to be dropped when resubscribing to a lot of contracts
        //case 2104: // Market data farm connection is OK:aufarm
            // TODO: resubscribe or reconnect?
            resubscribeInactiveSubscriptions();
            break;
        }
    }

	@Override
	public void run()
	{
        resubscribeInactiveSubscriptions();

        new java.util.Timer().schedule(
                new java.util.TimerTask()
                {
                    @Override
                    public void run()
                    {
                        resubscribeInactiveSubscriptions();
                    }
                },
                10000
        );
	}

    @Override
    public void connectionClosed()
    {
        l.warn("Connection closed - clearing IDs");
        dataByTickId.clear();
        dataByDepthId.clear();
        for (InstrumentData data : dataBySymbol.values())
        {
            data.subscriptionState = SubscriptionState.UNKNOWN;
            data.tickerId = 0;
        }

        resubscribeInactiveSubscriptions();
    }

    private int subscribe(InstrumentData data)
    {
    	if (!connection.isReady())
    	{
            l.debug("Subscription request postponed for " + data.instrument.getSymbol());
    		return 0;
    	}

        int oldId = data.tickerId;
        data.tickerId = connection.getNextRequestId();
        dataByTickId.put(data.tickerId, data);
        
        //data.active = true; // assume it's active for now and drop the flag if any error
        connection.reqMktData(data.tickerId, IBContractConverter.convertToContract(data.instrument), "", false);
        
        l.debug("Sent subscription request [" + data.tickerId + "] for " + data.instrument.getSymbol());

        if (oldId != 0)
        {
            dataByTickId.remove(oldId);
            connection.cancelMktData(oldId);
            l.debug("Unsubscribed from [" + oldId + "] for " + data.instrument.getSymbol());
        }

        return data.tickerId;
    }

    private void unsubscribe(InstrumentData data)
    {
        if (!connection.isReady())
        {
            l.debug("Unsubscription request rejected for " + data.instrument.getSymbol() + " - no connection");
            return;
        }

        connection.cancelMktData(data.tickerId);

        l.debug("Sent unsubscription request [" + data.tickerId + "] for " + data.instrument.getSymbol());

        data.tickerId = 0;
    }

    private int subscribeDepth(InstrumentData data)
    {
        if (!connection.isReady())
        {
            l.debug("Subscription request postponed for " + data.instrument.getSymbol());
            return 0;
        }

        data.depthId = connection.getNextRequestId();
        dataByDepthId.put(data.depthId, data);

        connection.reqMktDepth(data.depthId, IBContractConverter.convertToContract(data.instrument), 40);

        l.debug("Sent depth subscription request [" + data.depthId + "] for " + data.instrument.getSymbol());

        return data.depthId;
    }

    private void resubscribeAll()
    {
        dataByTickId.clear();
        for (InstrumentData data : dataBySymbol.values())
        {
            subscribe(data);
        }
    }
    
    private void resubscribeInactiveSubscriptions()
    {
        l.info("(Re)subscribing for market data");
        int count = 0;
        for (InstrumentData data : dataBySymbol.values())
        {
            if (data.subscriptionState != SubscriptionState.SUBSCRIBED)
            {
                subscribe(data);
                ++count;
            }
        }
        l.info("Sent " + count + " subscribe requests (total instruments: " + dataBySymbol.size() + ")");
    }

    private void resubscribeInactiveDepth()
    {
        l.info("(Re)subscribing for depth data");
        int count = 0;
        for (InstrumentData data : dataBySymbol.values())
        {
            if (data.depthId != 0)
            {
                subscribeDepth(data);
                ++count;
            }
        }
        l.info("Sent " + count + " subscribe requests");
    }

    @Override
    public void subscribeToPriceAndTradeFeed(List<IInstrument> new_instruments, List<IPriceAndTradeFeedListener> new_listeners)
    {
        for (IInstrument instrument : new_instruments)
        {
            InstrumentData data = dataBySymbol.get(instrument);
            if (data == null)
            {
                data = new InstrumentData(instrument);
                data.listeners.addAll(new_listeners);

                dataBySymbol.put(instrument, data);

                subscribe(data);
            } else
            {
                data.listeners.addAll(new_listeners);
            }
        }
    }

    @Override
    public void unsubscribeFromPriceAndTradeFeed(List<IInstrument> new_instruments, List<IPriceAndTradeFeedListener> new_listeners)
    {
        for (IInstrument instrument : new_instruments)
        {
            InstrumentData data = dataBySymbol.get(instrument);
            if (data != null)
            {
                data = new InstrumentData(instrument);
                unsubscribe(data);
                data.listeners.removeAll(new_listeners);
            }
        }
    }

    @Override
    public void subscribeToPriceDepth(List<IInstrument> new_instruments, List<IPriceAndTradeFeedListener> new_listeners)
    {
        for (IInstrument instrument : new_instruments)
        {
            InstrumentData data = dataBySymbol.get(instrument);
            if (data != null && data.depthId == 0)
            {
                subscribeDepth(data);
            }
        }
    }

    @Override
    public SubscriptionState getSubscriptionState(IInstrument instrument)
    {
        InstrumentData data = dataBySymbol.get(instrument);
        if (data != null)
        {
            return data.subscriptionState;
        }
        else
        {
            return SubscriptionState.UNKNOWN;
        }
    }
}
