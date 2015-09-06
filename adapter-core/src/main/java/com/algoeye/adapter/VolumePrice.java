package com.algoeye.adapter;

public class VolumePrice
{
    public int volume = 0;
    public double price = Double.NaN;

    public VolumePrice()
    {
    }

    public VolumePrice(int volume, double price)
    {
        setVolumePrice(volume, price);
    }

    final public void setVolumePrice(VolumePrice vp)
    {
        this.volume = vp.volume;
        this.price = vp.price;
    }
    
    final public void setVolumePrice(int volume, double price)
    {
        this.volume = volume;
        this.price = price;
    }
    
    final public boolean isValid()
    {
        return !Double.isNaN(price);
    }

    final public boolean isActive()
    {
        return !Double.isNaN(price) && volume != 0;
    }

    final public void clear()
    {
        volume = 0;
        price = Double.NaN;
    }

    final public int getVolume()
    {
        return volume;
    }

    final public double getPrice()
    {
        return price;
    }

    @Override
    public String toString()
    {
        if (isValid())
        {
            return volume + "@" + price;
        }
        else
        {
            return "n/a";
        }
    }
}
