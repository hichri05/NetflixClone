package org.netflix.Models;

public class Payment_Method {
    private int id_payment;
    private int id_User;
    private String provider;
    private int last4;
    private String brand;
    private int isDefault;
    public Payment_Method(int id_payment, int id_User, String provider, int last4, String brand, int isDefault) {
        this.id_payment = id_payment;
        this.id_User = id_User;
        this.provider = provider;
        this.last4 = last4;
        this.brand = brand;
        this.isDefault = isDefault;
    }

    public void setId_payment(int id_payment) {
        this.id_payment = id_payment;
    }

    public int getId_payment() {
        return id_payment;
    }
    public void seetId_User(int id_User) {
        this.id_User = id_User;
    }

    public int getId_User() {
        return id_User;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public int getLast4() {
        return last4;
    }

    public void setId_User(int id_User) {
        this.id_User = id_User;
    }

    public String getBrand() {
        return brand;
    }

    public String getProvider() {
        return provider;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    public void setLast4(int last4) {
        this.last4 = last4;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
