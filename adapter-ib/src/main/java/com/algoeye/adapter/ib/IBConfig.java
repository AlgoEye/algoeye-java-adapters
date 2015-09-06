package com.algoeye.adapter.ib;

import org.simpleframework.xml.Attribute;

public class IBConfig
{
    @Attribute
    public String host;

    @Attribute
    public int port;

    @Attribute
    public int client;
}
