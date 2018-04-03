package model;

/**
 * Created by LieuDucManh on 3/21/2018.
 */

public class Restaurant {

    private String id;
    private String name;
    private String managerUID;
    private String address;
    private String district;
    private String city;
    private String contact;
    private String timeOpenClose;

    public Restaurant(String id, String name, String managerUID, String address, String district, String city, String contact, String timeOpenClose) {
        this.id = id;
        this.name = name;
        this.managerUID = managerUID;
        this.address = address;
        this.district = district;
        this.city = city;
        this.contact = contact;
        this.timeOpenClose = timeOpenClose;
    }

    public Restaurant() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManagerUID() {
        return managerUID;
    }

    public void setManagerUID(String managerUID) {
        this.managerUID = managerUID;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getTimeOpenClose() {
        return timeOpenClose;
    }

    public void setTimeOpenClose(String timeOpenClose) {
        this.timeOpenClose = timeOpenClose;
    }
}
