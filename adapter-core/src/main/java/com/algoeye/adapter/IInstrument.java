package com.algoeye.adapter;

import java.util.Date;

public interface IInstrument
{
    String getSymbol();
    String getInstrumentType();
    String getUnderlyingName();
    String getExchangeSymbol();
    String getExchange();
    String getCurrency();
    int getContractSize();
    Date getExpiryDate();
    double getStrikePrice();
    String getRight();
}
