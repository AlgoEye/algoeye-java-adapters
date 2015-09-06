package com.algoeye.adapter;

import java.util.Date;

public class Instrument implements IInstrument
{
    private final String symbol;
    private final String type;
    private final String underlying;
    private final String exchangeSymbol;
    private final String exchange;
    private final String currency;
    private final int contractSize;
    private final Date expiryDate;
    private final double strikePrice;
    private final String right;

    public Instrument(
            String symbol,
            String type,
            String underlying,
            String exchangeSymbol,
            String exchange,
            String currency,
            int contractSize,
            Date expiryDate,
            double strikePrice,
            String right)
    {
        this.symbol = symbol;
        this.type = type;
        this.underlying = underlying;
        this.exchangeSymbol = exchangeSymbol;
        this.exchange = exchange;
        this.currency = currency;
        this.contractSize = contractSize;
        this.expiryDate = expiryDate;
        this.strikePrice = strikePrice;
        this.right = right;
    }


    @Override
    public String getSymbol()
    {
        return symbol;
    }

    @Override
    public String getInstrumentType()
    {
        return type;
    }

    @Override
    public String getUnderlyingName()
    {
        return underlying;
    }

    @Override
    public String getExchangeSymbol()
    {
        return exchangeSymbol;
    }

    @Override
    public String getExchange()
    {
        return exchange;
    }

    @Override
    public String getCurrency()
    {
        return currency;
    }

    @Override
    public int getContractSize()
    {
        return contractSize;
    }

    @Override
    public Date getExpiryDate()
    {
        return expiryDate;
    }

    @Override
    public double getStrikePrice()
    {
        return strikePrice;
    }

    @Override
    public String getRight()
    {
        return right;
    }
}
