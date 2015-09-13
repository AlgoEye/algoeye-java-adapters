package com.algoeye.adapter.kdb;

import com.algoeye.adapter.IInstrument;
import com.algoeye.adapter.Instrument;
import kx.c;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

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
        {Object count = kdb.context().k("count refdata");
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
            Object result = kdb.context().k(query);
            if (result instanceof c.Flip)
            {
                return readInstruments(convertFlip((c.Flip)result));
            }
            else
            {
                c.Dict dict = (c.Dict)result;
                Map<String, Object> table = convertFlip((c.Flip) dict.x);
                table.putAll(convertFlip((c.Flip) dict.y));
                return readInstruments(table);
            }
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

    protected Map<String, Object> convertFlip(c.Flip flip)
    {
        Map<String, Object> result = new HashMap<>();

        int cols = Array.getLength(flip.x);
        for (int i = 0; i < cols; ++i)
        {
            result.put(flip.x[i], flip.y[i]);
        }

        return result;
    }

    protected List<IInstrument> readInstruments(Map<String, Object> table)
    {
        ArrayList<IInstrument> instruments = new ArrayList<>();

        String[] sym = (String[])table.get("sym");
        String[] typ = (String[])table.get("typ");
        String[] underlying = (String[])table.get("underlying");
        String[] exsym = (String[])table.get("exsym");
        String[] exchange = (String[])table.get("exchange");
        String[] ccy = (String[])table.get("ccy");
        int[] lot =(int[]) table.get("lot");
        Date[] expiry = (Date[])table.get("expiry");
        double[] strike = (double[])table.get("strike");
        String[] right = (String[])table.get("right");

        int rows = Array.getLength(table.get("sym"));
        for (int i = 0; i < rows; ++i)
        {
            instruments.add(new Instrument(
                    sym[i],
                    typ[i],
                    underlying[i],
                    exsym[i],
                    exchange[i],
                    ccy[i],
                    lot[i],
                    expiry[i],
                    !Double.isNaN(strike[i]) ? strike[i] : 0,
                    right[i]));
        }

        return instruments;
    }
}
