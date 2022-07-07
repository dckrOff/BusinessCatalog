package com.a1tech.businesscatalog.Model;

public class Item {

    private final String itemName;
    private final String itemPrice;
    private final String itemImg;
    private final String itemAmount;

    public Item(String itemName, String itemPrice, String itemImg, String itemAmount) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.itemImg = itemImg;
        this.itemAmount = itemAmount;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public String getItemImg() {
        return itemImg;
    }

    public String getItemAmount() {
        return itemAmount;
    }
}
