package com.algoeye.adapter.ib;

import java.text.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.algoeye.adapter.IContractSymbolsListener;
import com.algoeye.adapter.Instrument;
import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;

public class IBContractSymbolsFeeder extends IBCallbackAdaptor implements Runnable
{
    private final Logger l = Logger.getLogger(getClass());

    private final String underlying;
    private final String exchange;
    private final String currency;
    private final int multiplier;
    private final List<String> types;
    private final Map<String, String> symbolFormat;
    private final IBConnection connection;
    private final IContractSymbolsListener listener;

    private Set<Integer> reqIds = new HashSet<>();
    private final SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyyMMdd");

    private CountDownLatch latch;

    public IBContractSymbolsFeeder(
            IBConnection connection,
            IContractSymbolsListener listener,
            String underlying,
            String exchange,
            String currency,
            int multiplier,
            List<String> types,
            Map<String, String> symbolFormat)
    {
        this.connection = connection;
        this.listener = listener;
        this.underlying = underlying;
        this.exchange = exchange;
        this.currency = currency;
        this.multiplier = multiplier;
        this.types = types;
        this.symbolFormat = symbolFormat;
    }

    public Date convertExpiryDate(String date)
    {
        try
        {
            return new Date(expiryFormat.parse(date).getTime());
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails)
    {
        if (reqIds.contains(reqId))
        {
            String type = contractDetails.m_summary.m_secType;
            double strike = contractDetails.m_summary.m_strike;
            int contractSize = contractDetails.m_summary.m_multiplier != null ? Integer.parseInt(contractDetails.m_summary.m_multiplier) : 1;
            Date expiryDate = convertExpiryDate(contractDetails.m_summary.m_expiry);
            String exchangeSymbol = contractDetails.m_summary.m_localSymbol;
            String pattern = symbolFormat.get(type);
            String code = pattern != null ?
                    MessageFormat.format(pattern, underlying, expiryDate, strike, contractDetails.m_summary.m_right) :
                    exchangeSymbol;
            code = code.toUpperCase();

            l.debug("Received " + type + " contract: " + code + " (" + exchangeSymbol + ")");

            listener.OnInstrument(new Instrument(code, type, underlying,
                    exchangeSymbol, contractDetails.m_summary.m_exchange, contractDetails.m_summary.m_currency, contractSize,
                    expiryDate, strike, contractDetails.m_summary.m_right));
        }

        super.contractDetails(reqId, contractDetails);
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
        if (reqIds.contains(id))
        {
            l.error("Error: " + errorMsg + ", code=" + errorCode);
            latch.countDown();
        }
    }

    @Override
    public void contractDetailsEnd(int reqId)
    {
        // TODO Auto-generated method stub
        super.contractDetailsEnd(reqId);

        latch.countDown();
    }

    void requestContracts(String type)
    {
        l.debug("Requesting " + type + " contracts for underlying " + underlying);

        Contract contract = new Contract();
        contract.m_secType = type;
        contract.m_symbol = underlying;
        contract.m_exchange = exchange;
        contract.m_currency = currency;

        if (multiplier > 0)
        {
            contract.m_multiplier = String.valueOf(multiplier);
        }

        int reqId = connection.getNextRequestId();
        reqIds.add(reqId);
        connection.reqContractDetails(reqId, contract);
    }

    @Override
    public void run()
    {
        reqIds.clear();
        latch = new CountDownLatch(types.size());
        types.forEach(type -> requestContracts(type));
        try
        {
            latch.await();
        } catch (InterruptedException e)
        {
        }
    }
}
