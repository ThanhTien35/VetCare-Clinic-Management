
package com.vetcare;

public class Owner {
    public final int id;
    public String name;
    public String phone;
    public String email;

    public Owner(int id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}
