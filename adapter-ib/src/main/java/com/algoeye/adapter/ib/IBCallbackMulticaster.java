package com.algoeye.adapter.ib;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

public class IBCallbackMulticaster implements EWrapper
{
    private final List<EWrapper> listeners = new CopyOnWriteArrayList<EWrapper>();
    
    public void addListener(EWrapper listener)
    {
        listeners.add(listener);
    }
    
    public void removeListener(EWrapper listener)
    {
        listeners.remove(listener);
    }
    
    @Override
    public void error(Exception e)
    {
        for (EWrapper listener : listeners)
        {
            listener.error(e);
        }
    }

    @Override
    public void error(String str)
    {
        for (EWrapper listener : listeners)
        {
            listener.error(str);
        }
    }

    @Override
    public void error(int id, int errorCode, String errorMsg)
    {
        for (EWrapper listener : listeners)
        {
            listener.error(id, errorCode, errorMsg);
        }
    }

    @Override
    public void connectionClosed()
    {
        for (EWrapper listener : listeners)
        {
            listener.connectionClosed();
        }
    }

    @Override
    public void tickPrice(int tickerId, int field, double price,
            int canAutoExecute)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickPrice(tickerId, field, price, canAutoExecute);
        }
    }

    @Override
    public void tickSize(int tickerId, int field, int size)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickSize(tickerId, field, size);
        }
    }

    @Override
    public void tickOptionComputation(int tickerId, int field,
            double impliedVol, double delta, double modelPrice,
            double pvDividend)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickOptionComputation(tickerId, field, impliedVol, delta, modelPrice, pvDividend);
        }
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickGeneric(tickerId, tickType, value);
        }
    }

    @Override
    public void tickString(int tickerId, int tickType, String value)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickString(tickerId, tickType, value);
        }
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints,
            String formattedBasisPoints, double impliedFuture, int holdDays,
            String futureExpiry, double dividendImpact, double dividendsToExpiry)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickEFP(tickerId, tickType, basisPoints, formattedBasisPoints, impliedFuture, holdDays, futureExpiry, dividendImpact, dividendsToExpiry);
        }
    }

    @Override
    public void orderStatus(int orderId, String status, int filled,
            int remaining, double avgFillPrice, int permId, int parentId,
            double lastFillPrice, int clientId, String whyHeld)
    {
        for (EWrapper listener : listeners)
        {
            listener.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld);
        }
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order,
            OrderState orderState)
    {
        for (EWrapper listener : listeners)
        {
            listener.openOrder(orderId, contract, order, orderState);
        }
    }

    @Override
    public void openOrderEnd()
    {
        for (EWrapper listener : listeners)
        {
            listener.openOrderEnd();
        }
    }

    @Override
    public void updateAccountValue(String key, String value, String currency,
            String accountName)
    {
        for (EWrapper listener : listeners)
        {
            listener.updateAccountValue(key, value, currency, accountName);
        }
    }

    @Override
    public void updatePortfolio(Contract contract, int position,
            double marketPrice, double marketValue, double averageCost,
            double unrealizedPNL, double realizedPNL, String accountName)
    {
        for (EWrapper listener : listeners)
        {
            listener.updatePortfolio(contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName);
        }
    }

    @Override
    public void updateAccountTime(String timeStamp)
    {
        for (EWrapper listener : listeners)
        {
            listener.updateAccountTime(timeStamp);
        }
    }

    @Override
    public void accountDownloadEnd(String accountName)
    {
        for (EWrapper listener : listeners)
        {
            listener.accountDownloadEnd(accountName);
        }
    }

    @Override
    public void nextValidId(int orderId)
    {
        for (EWrapper listener : listeners)
        {
            listener.nextValidId(orderId);
        }
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails)
    {
        for (EWrapper listener : listeners)
        {
            listener.contractDetails(reqId, contractDetails);
        }
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails)
    {
        for (EWrapper listener : listeners)
        {
            listener.bondContractDetails(reqId, contractDetails);
        }
    }

    @Override
    public void contractDetailsEnd(int reqId)
    {
        for (EWrapper listener : listeners)
        {
            listener.contractDetailsEnd(reqId);
        }
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution)
    {
        for (EWrapper listener : listeners)
        {
            listener.execDetails(reqId, contract, execution);
        }
    }

    @Override
    public void execDetailsEnd(int reqId)
    {
        for (EWrapper listener : listeners)
        {
            listener.execDetailsEnd(reqId);
        }
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation,
            int side, double price, int size)
    {
        for (EWrapper listener : listeners)
        {
            listener.updateMktDepth(tickerId, position, operation, side, price, size);
        }
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position,
            String marketMaker, int operation, int side, double price, int size)
    {
        for (EWrapper listener : listeners)
        {
            listener.updateMktDepthL2(tickerId, position, marketMaker, operation, side, price, size);
        }
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message,
            String origExchange)
    {
        for (EWrapper listener : listeners)
        {
            listener.updateNewsBulletin(msgId, msgType, message, origExchange);
        }
    }

    @Override
    public void managedAccounts(String accountsList)
    {
        for (EWrapper listener : listeners)
        {
            listener.managedAccounts(accountsList);
        }
    }

    @Override
    public void receiveFA(int faDataType, String xml)
    {
        for (EWrapper listener : listeners)
        {
            listener.receiveFA(faDataType, xml);
        }
    }

    @Override
    public void historicalData(int reqId, String date, double open,
            double high, double low, double close, int volume, int count,
            double WAP, boolean hasGaps)
    {
        for (EWrapper listener : listeners)
        {
            listener.historicalData(reqId, date, open, high, low, close, volume, count, WAP, hasGaps);
        }
    }

    @Override
    public void scannerParameters(String xml)
    {
        for (EWrapper listener : listeners)
        {
            listener.scannerParameters(xml);
        }
    }

    @Override
    public void scannerData(int reqId, int rank,
            ContractDetails contractDetails, String distance, String benchmark,
            String projection, String legsStr)
    {
        for (EWrapper listener : listeners)
        {
            listener.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr);
        }
    }

    @Override
    public void scannerDataEnd(int reqId)
    {
        for (EWrapper listener : listeners)
        {
            listener.scannerDataEnd(reqId);
        }
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high,
            double low, double close, long volume, double wap, int count)
    {
        for (EWrapper listener : listeners)
        {
            listener.realtimeBar(reqId, time, open, high, low, close, volume, wap, count);
        }
    }

    @Override
    public void currentTime(long time)
    {
        for (EWrapper listener : listeners)
        {
            listener.currentTime(time);
        }
    }

    @Override
    public void fundamentalData(int reqId, String data)
    {
        for (EWrapper listener : listeners)
        {
            listener.fundamentalData(reqId, data);
        }
    }

    @Override
    public void deltaNeutralValidation(int reqId, UnderComp underComp)
    {
        for (EWrapper listener : listeners)
        {
            listener.deltaNeutralValidation(reqId, underComp);
        }
    }

    @Override
    public void tickSnapshotEnd(int reqId)
    {
        for (EWrapper listener : listeners)
        {
            listener.tickSnapshotEnd(reqId);
        }
    }
} 
