package com.example.capstone;

import android.location.Location;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {

    MutableLiveData<Location> location = new MutableLiveData<>();

    public void setLocation(@Nullable Location location) {
        this.location.setValue(location);
    }

    public LiveData<Location> getLocation() {
        return location;
    }
}
