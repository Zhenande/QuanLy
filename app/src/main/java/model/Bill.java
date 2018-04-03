package model;

/**
 * Created by LieuDucManh on 3/31/2018.
 */

public class Bill {

    private String id;
    private String time;
    private String customerName;
    private String costTotal;

    public Bill(String id, String time, String customerName, String costTotal) {
        this.id = id;
        this.time = time;
        this.customerName = customerName;
        this.costTotal = costTotal;
    }

    public Bill() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(String costTotal) {
        this.costTotal = costTotal;
    }
}
