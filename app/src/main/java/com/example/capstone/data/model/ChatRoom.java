package com.example.capstone.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatRoom implements Parcelable {

    @DocumentId
    private String id = "";

    // Posting 정보
    private String postingId = "";
    private String postingOwner = "";

    private double latitude;
    private double longitude;

    private String foodClassification;
    private String restaurantName;
    private int minimumOrderAmount;
    private int deliveryFee;
    private String contents;
    private String etc;

    // 채팅방 정보
    private List<String> participants = new ArrayList<>();
    private Map<String, String> names = new HashMap<>();

    private String latestMessage = "";

    @ServerTimestamp
    private Date latestMessageTimestamp = null;

    @ServerTimestamp
    private Date timestamp = null;


    public ChatRoom() {
    }

    public ChatRoom(Posting posting, FirebaseUser user) {
        assert !posting.getUid().equals(user.getUid());

        this.id = posting.getId() + "_" + user.getUid();
        this.postingId = posting.getId();
        this.postingOwner = posting.getUid();
        this.latitude = posting.getLatitude();
        this.longitude = posting.getLongitude();
        this.foodClassification = posting.getFoodClassification();
        this.restaurantName = posting.getRestaurantName();
        this.minimumOrderAmount = posting.getMinimumOrderAmount();
        this.deliveryFee = posting.getDeliveryFee();
        this.contents = posting.getContents();
        this.etc = posting.getEtc();

        this.participants.add(posting.getUid());
        this.participants.add(user.getUid());

        this.names.put(posting.getUid(), posting.getNickName());
        this.names.put(user.getUid(), user.getDisplayName());
    }

    protected ChatRoom(Parcel in) {
        id = in.readString();
        postingId = in.readString();
        postingOwner = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        foodClassification = in.readString();
        restaurantName = in.readString();
        minimumOrderAmount = in.readInt();
        deliveryFee = in.readInt();
        contents = in.readString();
        etc = in.readString();
        participants = in.createStringArrayList();
        latestMessage = in.readString();

        long latestMessageTimeMillis = in.readLong();
        if (latestMessageTimeMillis > 0) {
            this.latestMessageTimestamp = new Date(latestMessageTimeMillis);
        }

        long timestampTimeMillis = in.readLong();
        if (timestampTimeMillis > 0) {
            this.timestamp = new Date(timestampTimeMillis);
        }

        int namesSize = in.readInt();
        for (int i = 0; i < namesSize; i++) {
            names.put(in.readString(), in.readString());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(postingId);
        dest.writeString(postingOwner);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(foodClassification);
        dest.writeString(restaurantName);
        dest.writeInt(minimumOrderAmount);
        dest.writeInt(deliveryFee);
        dest.writeString(contents);
        dest.writeString(etc);
        dest.writeStringList(participants);
        dest.writeString(latestMessage);
        dest.writeLong(latestMessageTimestamp == null ? 0 : latestMessageTimestamp.getTime());
        dest.writeLong(timestamp == null ? 0 : timestamp.getTime());

        dest.writeInt(names.size());
        for (Map.Entry<String, String> entry : names.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatRoom> CREATOR = new Creator<ChatRoom>() {
        @Override
        public ChatRoom createFromParcel(Parcel in) {
            return new ChatRoom(in);
        }

        @Override
        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostingId() {
        return postingId;
    }

    public void setPostingId(String postingId) {
        this.postingId = postingId;
    }

    public String getPostingOwner() {
        return postingOwner;
    }

    public void setPostingOwner(String postingOwner) {
        this.postingOwner = postingOwner;
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

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public Date getLatestMessageTimestamp() {
        return latestMessageTimestamp;
    }

    public void setLatestMessageTimestamp(Date latestMessageTimestamp) {
        this.latestMessageTimestamp = latestMessageTimestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
