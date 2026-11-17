
package com.vetcare;

public class ServiceItem {
    public final int id;
    public String name;
    public long price;

    public ServiceItem(int id, String name, long price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getCode() {
        return String.format("SRV%03d", id);
    }

    @Override
    public String toString() {
        return getCode() + " - " + name
            + " (" + String.format("%,d", price) + " VND)";
    }
}
