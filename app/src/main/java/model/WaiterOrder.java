package model;

/**
 * Created by LieuDucManh on 5/13/2018.
 */
public class WaiterOrder {

    private String waiterName;
    private int numberOrder;

    public WaiterOrder() {
    }

    public WaiterOrder(String waiterName, int numberOrder) {
        this.waiterName = waiterName;
        this.numberOrder = numberOrder;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public int getNumberOrder() {
        return numberOrder;
    }

    public void setNumberOrder(int numberOrder) {
        this.numberOrder = numberOrder;
    }
}
