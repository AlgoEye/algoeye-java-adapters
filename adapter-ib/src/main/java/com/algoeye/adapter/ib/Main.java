package com.algoeye.adapter.ib;

import com.algoeye.adapter.IInstrument;
import com.algoeye.adapter.kdb.KdbConfig;
import com.algoeye.adapter.kdb.KdbConnection;
import com.algoeye.adapter.kdb.KdbDataWriter;
import com.algoeye.adapter.kdb.KdbQuery;
import org.apache.log4j.PropertyConfigurator;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.*;

public class Main
{
    static class SymbolConfig
    {
        @Attribute
        public String name;

        @Attribute
        public String exchange;

        @Attribute
        public String currency;

        @Attribute(required=false)
        public int multiplier;

        @Attribute
        public String type;
    }

    static class FormatConfig
    {
        @Attribute
        public String type;

        @Attribute
        public String format;
    }

    static class Config
    {
        @Element
        IBConfig ib;

        @Element
        KdbConfig tickerplant;

        @Element
        KdbConfig database;

        @ElementList(entry = "symbol")
        List<SymbolConfig> symbols;

        @ElementList(entry = "format")
        List<FormatConfig> formats;

        @Element
        String subscribe;
    }

    public static void main(String[] args) throws Exception
    {
        PropertyConfigurator.configureAndWatch("log4j.properties", 60000);

        Serializer serializer = new Persister();
        File source = new File(args[0]);

        Config config = serializer.read(Config.class, source);

        KdbConnection tickerplant = new KdbConnection(config.tickerplant);
        KdbDataWriter writer = new KdbDataWriter(tickerplant);

        KdbConnection database = new KdbConnection(config.database);
        KdbQuery query = new KdbQuery(database);

        IBCallbackMulticaster multicaster = new IBCallbackMulticaster();
        IBConnection connection = new IBConnection(multicaster, config.ib);

        connection.connect();
        connection.waitWhenReady();

        if (!query.hasRefData())
        {
            Map<String, String> formats = new HashMap<>();
            for (FormatConfig format : config.formats)
            {
                formats.put(format.type, format.format);
            }

            for (SymbolConfig symbol : config.symbols)
            {
                IBContractSymbolsFeeder feeder = new IBContractSymbolsFeeder(connection, writer,
                        symbol.name, symbol.exchange, symbol.currency, symbol.multiplier,
                        Arrays.asList(symbol.type.split(",")), formats);
                multicaster.addListener(feeder);
                feeder.run();
                multicaster.removeListener(feeder);
            }
        }

        List<IInstrument> instruments = query.queryInstruments(config.subscribe);
        IBMarketDataProvider data = new IBMarketDataProvider(connection);
        multicaster.addListener(data);
        data.subscribeToPriceAndTradeFeed(instruments, Arrays.asList(writer));

        while (connection.isConnected())
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                return;
            }
        }
    }
}
