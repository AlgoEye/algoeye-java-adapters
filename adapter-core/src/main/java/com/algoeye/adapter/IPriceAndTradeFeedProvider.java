package com.algoeye.adapter;

import java.util.List;

public interface IPriceAndTradeFeedProvider
{
    enum SubscriptionState
    {
        UNKNOWN,
        SUBSCRIBED,
        UNAVAILABLE,
        RESTRICTED,
        LIMITED
    }

    void subscribeToPriceAndTradeFeed(List<IInstrument> instruments, List<IPriceAndTradeFeedListener> listeners);
    void unsubscribeFromPriceAndTradeFeed(List<IInstrument> instruments, List<IPriceAndTradeFeedListener> listeners);

    void subscribeToPriceDepth(List<IInstrument> instruments, List<IPriceAndTradeFeedListener> listeners);


    SubscriptionState getSubscriptionState(IInstrument instrument);
}
