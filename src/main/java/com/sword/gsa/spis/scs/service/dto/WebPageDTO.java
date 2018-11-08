package com.sword.gsa.spis.scs.service.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

public class WebPageDTO {

    private String url;
    private long updatedAt;
    private HashMap metadata ;

    public WebPageDTO() {
    }

    public WebPageDTO(String url, long updatedAt, HashMap metadata) {
        this.url = url;
        this.updatedAt = updatedAt;
        this.metadata = metadata;
    }

    public String getUrl() {
        return url;
    }

    public WebPageDTO setUrl(String url) {
        this.url = url;
        return this;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    /** on peut se contenter de LocalDate */
    public WebPageDTO setUpdatedAt(LocalDateTime updatedAtLocalDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        this.updatedAt = updatedAtLocalDateTime.atZone(zoneId).toEpochSecond();
        return this;
    }

    public WebPageDTO setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public HashMap getMetadata() {
        return metadata;
    }

    public WebPageDTO setMetadata(HashMap metadata) {
        this.metadata = metadata;
        return this;
    }

}
