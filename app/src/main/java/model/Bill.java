package model;

/**
 * Created by LieuDucManh on 3/31/2018.
 */

public class Bill {

    private String id;
    private String time;
    private String costTotal;
    private String billNumber;
    private String waiterName;

    public Bill(String id, String time, String costTotal) {
        this.id = id;
        this.time = time;
        this.costTotal = costTotal;
    }

    public Bill() {
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
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

    public String getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(String costTotal) {
        this.costTotal = costTotal;
    }
}
