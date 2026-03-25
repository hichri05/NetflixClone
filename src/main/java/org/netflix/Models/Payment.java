package org.netflix.Models;

public class Payment {
    private int id_payment;
    private int id_User;
    private int id_Subscription;
    private float amount;
    private String currency;
    private int Status;
    public Payment(int id_payment, int id_User, int id_Subscription, float amount, String currency, int Status) {
        this.id_payment = id_payment;
        this.id_User = id_User;
        this.id_Subscription = id_Subscription;
        this.amount = amount;
        this.currency = currency;
        this.Status = Status;
    }

    public int getId_payment() {
        return id_payment;
    }

    public float getAmount() {
        return amount;
    }

    public int getId_Subscription() {
        return id_Subscription;
    }

    public int getId_User() {
        return id_User;
    }

    public int getStatus() {
        return Status;
    }

    public String getCurrency() {
        return currency;
    }
    public void setId_payment(int id_payment) {
        this.id_payment = id_payment;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setId_Subscription(int id_Subscription) {
        this.id_Subscription = id_Subscription;
    }

    public void setId_User(int id_User) {
        this.id_User = id_User;
    }

    public void setStatus(int status) {
        Status = status;
    }
}
