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

    void OnDepth(
            IInstrument instrument,
            String marketMaker,
            char operation,
            char side,
            int level,
            int size,
            double price
    );
}
