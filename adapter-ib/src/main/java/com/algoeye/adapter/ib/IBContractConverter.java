package com.algoeye.adapter.ib;

import com.algoeye.adapter.IInstrument;
import com.ib.client.Contract;

import java.text.SimpleDateFormat;

public class IBContractConverter
{
    private final static SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyyMMdd");

    public static Contract convertToContract(IInstrument instrument)
    {
        Contract contract = new Contract();

        contract.m_symbol = instrument.getUnderlyingName();
        contract.m_secType = instrument.getInstrumentType();
        contract.m_exchange = instrument.getExchange();
        if (instrument.getExpiryDate() != null)
        {
            contract.m_expiry = expiryFormat.format(instrument.getExpiryDate());
        }
        contract.m_currency = instrument.getCurrency();

        int multiplier = instrument.getContractSize();

        if (multiplier > 0)
        {
            contract.m_multiplier = String.valueOf(multiplier);
        }

        if (instrument.getStrikePrice() > 0)
        {
            contract.m_strike = instrument.getStrikePrice();
            contract.m_right = instrument.getRight();
        }

        return contract;
    }

}
