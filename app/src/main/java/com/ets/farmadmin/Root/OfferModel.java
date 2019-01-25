package com.ets.farmadmin.Root;

import com.google.firebase.database.PropertyName;

public class OfferModel {

    private String img_url;

    private String key;

    public OfferModel() {
        //Required Empty Constructor
    }

    @PropertyName("img_url")
    public String getImgUrl() {
        return img_url;
    }

    @PropertyName("img_url")
    public void setImgUrl(String img_url) {
        this.img_url = img_url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}