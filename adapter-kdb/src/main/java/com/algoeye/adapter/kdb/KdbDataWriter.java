package com.algoeye.adapter.kdb;

import com.algoeye.adapter.*;
import kx.c;

import java.io.IOException;

public class KdbDataWriter implements IPriceAndTradeFeedListener, IContractSymbolsListener
{

    private final KdbConnection kdb;

    public KdbDataWriter(KdbConnection kdb)
    {
        this.kdb = kdb;
    }

    @Override
    public void OnNewPrice(IInstrument instrument, VolumePrice bid, VolumePrice ask)
    {
        try
        {
            kdb.context().ks(".u.upd", "quote", new Object[]{instrument.getSymbol(),
                    bid.getVolume() != 0 ? bid.getVolume() : c.NULL('i'), bid.getPrice(),
                    ask.getPrice(), ask.getVolume() != 0 ? ask.getVolume() : c.NULL('i')});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnNewTrade(IInstrument instrument, VolumePrice trade)
    {
        try
        {
            kdb.context().ks(".u.upd", "trade", new Object[]{ instrument.getSymbol(),
                    trade.getVolume(), trade.getPrice() });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnClosePrice(IInstrument instrument, double price)
    {

    }

    @Override
    public void OnDepth(IInstrument instrument, String marketMaker, char operation, char side, int level, int size, double price)
    {
        try
        {
            kdb.context().ks(".u.upd", "depth", new Object[]{ instrument.getSymbol(),
                    marketMaker != null ? marketMaker : c.NULL('s'),
                    operation, side, level,
                    size, price});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnInstrument(Instrument instrument)
    {
        try
        {
            kdb.context().ks(".u.upd", "refdata", new Object[]{instrument.getSymbol(),
                    instrument.getInstrumentType(), instrument.getUnderlyingName(),
                    instrument.getExchangeSymbol(), instrument.getExchange(),
                    instrument.getCurrency(), instrument.getContractSize(),
                    instrument.getExpiryDate() != null ? new java.sql.Date(instrument.getExpiryDate().getTime()) : c.NULL('d'),
                    instrument.getStrikePrice() > 0 ? instrument.getStrikePrice() : c.NULL('f'),
                    instrument.getRight() != null ? instrument.getRight() : c.NULL('s')});
        } catch (Exception e) {
            // TODO: add proper exception handling
            e.printStackTrace();
        }
    }
}
