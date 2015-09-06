package com.algoeye.adapter.kdb;

import kx.c;

public class KdbConnection
{
    private final KdbConfig config = new KdbConfig();

    private c conn;

    public KdbConnection(KdbConfig c)
    {
        this(c.host, c.port, c.userpass);
    }

    public KdbConnection(String host, int port)
    {
        this(host, port, null);
    }

    public KdbConnection(String host, int port, String userpass)
    {
        config.host = host;
        config.port = port;
        config.userpass = userpass;
        connect();
    }

    public void connect()
    {
        try
        {
            conn = (config.userpass == null) ?
                    new c(config.host, config.port) :
                    new c(config.host, config.port, config.userpass);
        } catch (Exception e)
        {
            e.printStackTrace();
            // re-connect
        }
    }

    public c context()
    {
        return conn;
    }
}
