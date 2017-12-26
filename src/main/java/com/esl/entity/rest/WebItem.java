package com.esl.entity.rest;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class WebItem {
    public String url;
    public String mime;
    public int imageHeight;
    public int imageWidth;

    public WebItem() {}

    public WebItem(String url, String mime, int imageHeight, int imageWidth) {
        this.url = url;
        this.mime = mime;
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("url", url)
                .append("mime", mime)
                .append("imageHeight", imageHeight)
                .append("imageWidth", imageWidth)
                .toString();
    }


}
