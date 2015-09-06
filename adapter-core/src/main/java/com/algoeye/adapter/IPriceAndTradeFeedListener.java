package com.algoeye.adapter;

public interface IPriceAndTradeFeedListener
{
    void OnNewPrice(
            IInstrument instrument,
            VolumePrice bid,
            VolumePrice ask
    );

    void OnNewTrade(
            IInstrument instrument,
            VolumePrice trade
    );

    void OnClosePrice(
            IInstrument instrument,
            double price
    );
}
