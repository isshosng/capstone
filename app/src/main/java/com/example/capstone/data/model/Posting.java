package com.example.capstone.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;


@IgnoreExtraProperties
public class Posting implements Parcelable {

    @DocumentId
    private String id;

    private String uid;
    private String nickName;

    private double latitude;
    private double longitude;
    private String geoHash;

    private String foodClassification;
    private String restaurantName;
    private int minimumOrderAmount;
    private int deliveryFee;
    private String contents;
    private String etc;

    @ServerTimestamp
    private Date timestamp;

    private String matchingTarget = null;

    @Exclude
    private int number;

    public Posting() {
    }

    protected Posting(Parcel in) {
        id = in.readString();
        uid = in.readString();
        nickName = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        geoHash = in.readString();
        foodClassification = in.readString();
        restaurantName = in.readString();
        minimumOrderAmount = in.readInt();
        deliveryFee = in.readInt();
        contents = in.readString();
        etc = in.readString();
        matchingTarget = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uid);
        dest.writeString(nickName);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(geoHash);
        dest.writeString(foodClassification);
        dest.writeString(restaurantName);
        dest.writeInt(minimumOrderAmount);
        dest.writeInt(deliveryFee);
        dest.writeString(contents);
        dest.writeString(etc);
        dest.writeString(matchingTarget);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Posting> CREATOR = new Creator<Posting>() {
        @Override
        public Posting createFromParcel(Parcel in) {
            return new Posting(in);
        }

        @Override
        public Posting[] newArray(int size) {
            return new Posting[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    public String getFoodClassification() {
        return foodClassification;
    }

    public void setFoodClassification(String foodClassification) {
        this.foodClassification = foodClassification;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public int getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(int minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public int getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(int deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getEtc() {
        return etc;
    }

    public void setEtc(String etc) {
        this.etc = etc;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMatchingTarget() {
        return matchingTarget;
    }

    public void setMatchingTarget(String matchingTarget) {
        this.matchingTarget = matchingTarget;
    }

    @Exclude
    public int getNumber() {
        return number;
    }

    @Exclude
    public void setNumber(int number) {
        this.number = number;
    }
}
