package org.netflix.Models;

import java.time.LocalDate;

public class Subscription {
    public int id_Subscription;
    public int id_User;
    public int id_SubscriptionPlan;
    public LocalDate start_date;
    public LocalDate end_date;
    public String status;
    public Subscription(int id_Subscription, int id_User, int id_SubscriptionPlan, LocalDate start_date, LocalDate end_date, String status) {
        this.id_Subscription = id_Subscription;
        this.id_User = id_User;
        this.id_SubscriptionPlan = id_SubscriptionPlan;
        this.start_date = start_date;
        this.end_date = end_date;
        this.status = status;
    }
    public Subscription(int id_User, int id_SubscriptionPlan, LocalDate start_date, LocalDate end_date, String status) {
        this.id_User = id_User;
        this.id_SubscriptionPlan = id_SubscriptionPlan;
        this.start_date = start_date;
        this.end_date = end_date;
        this.status = status;
    }
    public int getId_Subscription() { return id_Subscription; }
    public int getId_User() { return id_User; }
    public int getId_SubscriptionPlan() { return id_SubscriptionPlan; }
    public LocalDate getStart_date() { return start_date; }
    public LocalDate getEnd_date() { return end_date; }
    public String getStatus() { return status; }
    public void setId_Subscription(int id_Subscription) { this.id_Subscription = id_Subscription; }
    public void setId_User(int id_User) { this.id_User = id_User; }
    public void setId_SubscriptionPlan(int id_SubscriptionPlan) { this.id_SubscriptionPlan = id_SubscriptionPlan; }
    public void setStart_date(LocalDate start_date) { this.start_date = start_date; }
    public void setEnd_date(LocalDate end_date) { this.end_date = end_date; }
    public void setStatus(String status) { this.status = status; }

}
