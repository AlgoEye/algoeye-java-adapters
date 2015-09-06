package com.algoeye.adapter.ib;

import java.io.EOFException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.ib.client.EClientSocket;

public class IBConnection extends EClientSocket
{
    private final Logger l = Logger.getLogger(getClass());
    
    private String ip;
    private int port;
    private int clientId;
    private int requestId = -1;
    private final CountDownLatch readyLatch = new CountDownLatch(1);
    private boolean doReconnect = true;
    private boolean stopped = false;
    private Thread connectionThread;
    
    class EventHandler extends IBCallbackAdaptor
    {
        @Override
        public void nextValidId(int orderId)
        {
            setRequestId(orderId);
        }

        @Override
        public void connectionClosed()
        {
            l.warn("Connection closed");
            if (doReconnect)
            {
            	if (!isConnected())
    	        {
    	            l.warn("Reconnecting...");
    	            connect();
    	        }
            }
        }

        @Override
        public void error(Exception e)
        {
        	if (isConnected() && !isReady() && e instanceof EOFException)
        	{
                l.warn("Got EOFException, trying next clientid");
                clientId++;
        	}
        }
    }

    public IBConnection(IBCallbackMulticaster multicaster, IBConfig config)
    {
        this(multicaster, config.host, config.port, config.client);
    }

    public IBConnection(IBCallbackMulticaster multicaster, String ip, int port, int clientId)
    {
        super(multicaster);
        this.ip = ip;
        this.port = port;
        this.clientId = clientId;
        multicaster.addListener(new EventHandler());
    }

    public void connect()
    {
    	int delay = 1000;
    	
        while (!stopped && !tryConnect())
        {
            try
            {
            	connectionThread = Thread.currentThread();
                Thread.sleep(delay);
                connectionThread = null;
                delay = Math.min(60000, 2 * delay);
            }
            catch (Exception x)
            {
            }
        }
    }

    public void disconnect()
    {
        if (isConnected())
        {
            eDisconnect();
            l.debug("Disconnected from InteractiveBrokers, ip: " +
                    ip + " port: " + port + " clientId: " + clientId);
        }
    }

    public void stop()
    {
    	stopped = true;
    	if (connectionThread != null)
    	{
    		connectionThread.interrupt();
    	}
    	disconnect();
    }

    protected void setRequestId(int requestId)
    {
        if (!isReady())
        {
            // set the id only on the first call to this function
            l.info("NextValidId has been set to " + requestId);
            this.requestId = requestId;

            readyLatch.countDown();
        }
    }

    protected boolean tryConnect()
    {
        if (!isConnected())
        {
            eConnect(ip, port, clientId);
            
            if (isConnected())
            {
                l.debug("Connected to InteractiveBrokers, ip: " +
                        ip + " port: " + port + " clientId: " + clientId);
            }
            else
            {
                l.error("Failed to connected to InteractiveBrokers, ip: " +
                        ip + " port: " + port + " clientId: " + clientId);
            }
        }
        
        return isConnected();
    }
    
    public int getClientId()
    {
        return clientId;
    }
    
    public boolean isReady()
    {
    	return readyLatch.getCount() == 0;
    }
    
    public void waitWhenReady()
    {
    	try
		{
			readyLatch.await();
		}
		catch (InterruptedException e)
		{
		}
    }

    public int getNextRequestId()
    {
    	if (!isReady())
    	{
    		l.error("Got a call to getNextRequestId but NextRequestId is not known yet");
    	}
        return requestId++;
    }
}
