package com.frankenstein.frankenstein;

/**
 * Created by davidmena on 2/24/18.
 *
 * This is a Object Card that holds the Location of the image and the Caption
 */

public class Card {

    private String imgURL;
    private String caption;

    public Card(String imgURL, String caption){
        this.imgURL = imgURL;
        this.caption = caption;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
