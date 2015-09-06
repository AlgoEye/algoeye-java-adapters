package com.algoeye.adapter.kdb;

import com.algoeye.adapter.IInstrument;
import com.algoeye.adapter.Instrument;
import kx.c;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nick on 30/08/15.
 */
public class KdbQuery
{
    final Logger l = Logger.getLogger(getClass());

    private final KdbConnection kdb;

    public KdbQuery(KdbConnection kdb)
    {
        this.kdb = kdb;
    }

    public boolean hasRefData()
    {
        try
        {
            Object count = kdb.context().k("count refdata");
            return 0 < (Long)count;
        }
        catch (c.KException e)
        {
            l.error("Failed to query 'count refdata'", e);
        }
        catch (IOException e)
        {
            l.error("Failed to query 'count refdata'", e);
        }

        return false;
    }

    public List<IInstrument> queryInstruments(String query)
    {
        l.debug("Executing KDB expiry: " + query);
        try
        {
            c.Flip flip = (c.Flip)kdb.context().k(query);

            ArrayList<IInstrument> instruments = new ArrayList<>();

            int rows = Array.getLength(flip.y[0]);
            for (int i = 0; i < rows; ++i)
            {
                String symbol = (String)c.at(flip.y[0], i);
                String type = (String)c.at(flip.y[1], i);
                String underlying = (String)c.at(flip.y[2], i);
                String exchangeSymbol = (String)c.at(flip.y[3], i);
                String exchange = (String)c.at(flip.y[4], i);
                String currency = (String)c.at(flip.y[5], i);
                Integer contractSize = (Integer)c.at(flip.y[6], i);
                Date expiryDate = (Date)c.at(flip.y[7], i);
                Double strike = (Double)c.at(flip.y[8], i);
                String right = (String)c.at(flip.y[9], i);

                instruments.add(new Instrument(
                        symbol,
                        type,
                        underlying,
                        exchangeSymbol,
                        exchange,
                        currency,
                        contractSize,
                        expiryDate,
                        strike != null ? strike : 0,
                        right));
            }

            return instruments;
        }
        catch (c.KException e)
        {
            l.error("Failed to query instruments: " + query, e);
        }
        catch (IOException e)
        {
            l.error("Failed to query instruments: " + query, e);
        }

        return null;
    }
}
