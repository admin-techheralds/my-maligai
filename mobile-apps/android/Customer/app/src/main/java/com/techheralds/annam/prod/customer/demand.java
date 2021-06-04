package com.techheralds.annam.prod.customer;

import java.util.ArrayList;
import java.util.Map;

public class demand {
    String consumer;
    String supplier;
    String deliveryTime;
    String status;
    String timeCreated;
    ArrayList<Map<String, Object>> demandList;
    ArrayList<Map<String, Object>> timeLine;
    String key;
    String address;
    double price;
    String payment_mode;
    String rejectionReason;
    String paid;
    String saleId;

    public demand() {
    }

    public demand(String consumer, String supplier, String deliveryTime, String status, String timeCreated, ArrayList<Map<String, Object>> demandList, ArrayList<Map<String, Object>> timeLine, String key, String address, double price, String payment_mode, String rejectionReason, String paid, String saleId) {
        this.consumer = consumer;
        this.supplier = supplier;
        this.deliveryTime = deliveryTime;
        this.status = status;
        this.timeCreated = timeCreated;
        this.demandList = demandList;
        this.timeLine = timeLine;
        this.key = key;
        this.address = address;
        this.price = price;
        this.payment_mode = payment_mode;
        this.rejectionReason = rejectionReason;
        this.paid = paid;
        this.saleId = saleId;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public ArrayList<Map<String, Object>> getDemandList() {
        return demandList;
    }

    public void setDemandList(ArrayList<Map<String, Object>> demandList) {
        this.demandList = demandList;
    }

    public ArrayList<Map<String, Object>> getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(ArrayList<Map<String, Object>> timeLine) {
        this.timeLine = timeLine;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }
}
