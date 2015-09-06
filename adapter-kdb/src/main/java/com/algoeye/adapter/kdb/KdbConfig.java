package com.algoeye.adapter.kdb;

import org.simpleframework.xml.Attribute;

public class KdbConfig
{
    @Attribute
    public String host;

    @Attribute
    public int port;

    @Attribute(required = false)
    public String userpass;
}
