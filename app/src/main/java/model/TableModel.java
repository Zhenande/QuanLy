package model;

/**
 * Created by LieuDucManh on 3/11/2018.
 */

public class TableModel {

    private String tableNumber;
    private boolean isAvailable;

    public TableModel() {
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
