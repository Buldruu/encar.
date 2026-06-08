package com.autoapi.client.model;

import com.google.gson.annotations.SerializedName;

/**
 * Offer-ийн data талбарыг задлах загвар.
 * Бодит талбарын нэрнүүд marketplace-аас хамаарна.
 */
public class OfferData {

    @SerializedName("mark")
    private String mark;

    @SerializedName("model")
    private String model;

    @SerializedName("year")
    private String year;

    @SerializedName("price")
    private String price;

    @SerializedName("mileage")
    private String mileage;

    @SerializedName("fuel_type")
    private String fuelType;

    @SerializedName("transmission")
    private String transmission;

    @SerializedName("color")
    private String color;

    @SerializedName("image")
    private String image;

    public String getMark()         { return mark; }
    public String getModel()        { return model; }
    public String getYear()         { return year; }
    public String getPrice()        { return price; }
    public String getMileage()      { return mileage; }
    public String getFuelType()     { return fuelType; }
    public String getTransmission() { return transmission; }
    public String getColor()        { return color; }
    public String getImage()        { return image; }
}
