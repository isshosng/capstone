package com.example.capstone.data.model;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.skt.Tmap.MapUtils;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.poi_item.TMapPOIItem;

public class ParcelableTMapPOIItem implements Parcelable {

    public String id;
    public String name;
    public Bitmap Icon;
    public String noorLat;
    public String noorLon;
    public String upperAddrName;
    public String middleAddrName;
    public String lowerAddrName;
    public String detailAddrName;
    public String desc;
    public String distance;
    public String radius;


    public ParcelableTMapPOIItem(TMapPOIItem item) {
        this.id = item.id;
        this.name = item.name;
        this.Icon = item.Icon;
        this.noorLat = item.noorLat;
        this.noorLon = item.noorLon;
        this.upperAddrName = item.upperAddrName;
        this.middleAddrName = item.middleAddrName;
        this.lowerAddrName = item.lowerAddrName;
        this.detailAddrName = item.detailAddrName;
        this.desc = item.desc;
        this.distance = item.distance;
        this.radius = item.radius;
    }

    public ParcelableTMapPOIItem(Location location, String address) {
        this.name = "";
        this.noorLat = String.valueOf(location.getLatitude());
        this.noorLon = String.valueOf(location.getLongitude());
        this.detailAddrName = address;
        this.id = noorLat + ", " + noorLon;
    }

    protected ParcelableTMapPOIItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        Icon = in.readParcelable(Bitmap.class.getClassLoader());
        noorLat = in.readString();
        noorLon = in.readString();
        upperAddrName = in.readString();
        middleAddrName = in.readString();
        lowerAddrName = in.readString();
        detailAddrName = in.readString();
        desc = in.readString();
        distance = in.readString();
        radius = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(Icon, flags);
        dest.writeString(noorLat);
        dest.writeString(noorLon);
        dest.writeString(upperAddrName);
        dest.writeString(middleAddrName);
        dest.writeString(lowerAddrName);
        dest.writeString(detailAddrName);
        dest.writeString(desc);
        dest.writeString(distance);
        dest.writeString(radius);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParcelableTMapPOIItem> CREATOR = new Creator<ParcelableTMapPOIItem>() {
        @Override
        public ParcelableTMapPOIItem createFromParcel(Parcel in) {
            return new ParcelableTMapPOIItem(in);
        }

        @Override
        public ParcelableTMapPOIItem[] newArray(int size) {
            return new ParcelableTMapPOIItem[size];
        }
    };

    public void setID(String id) {
        this.id = id;
    }

    public Bitmap getIcon() {
        return this.Icon;
    }

    public String getPOIID() {
        return this.id;
    }

    public String getPOIName() {
        return this.name.trim();
    }

    public TMapPoint getPOIPoint() {
        return new TMapPoint(Double.parseDouble(this.noorLat), Double.parseDouble(this.noorLon));
    }

    public double getLatitude() {
        return getPOIPoint().getLatitude();
    }

    public double getLongitude() {
        return getPOIPoint().getLongitude();
    }

    public String getPOIAddress() {
        return (this.upperAddrName + " " + this.middleAddrName + " " + this.lowerAddrName + " " + this.detailAddrName)
                .replace("null", "")
                .trim();
    }

    public String getPOIContent() {
        return this.desc;
    }

    public double getDistance(TMapPoint point) {
        if (null == this.distance || this.distance.equals("")) {
            this.distance = this.radius;
        }

        TMapPoint point2 = new TMapPoint(Double.parseDouble(this.noorLat), Double.parseDouble(this.noorLon));
        this.distance = Double.toString(MapUtils.getDistance(point, point2));
        return Double.parseDouble(this.distance);
    }
}
