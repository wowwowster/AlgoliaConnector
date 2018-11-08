package com.sword.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "webpage")
public class WebPage {

    @NotNull
    @Id
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "content")
    private String content;

    @Column(name = "url")
    private String url;

    @OneToMany
    private Set<HTMLMetadata> htmlMetadata = new HashSet<HTMLMetadata>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<HTMLMetadata> getHtmlMetadata() {
        return htmlMetadata;
    }

    public void setHtmlMetadata(Set<HTMLMetadata> htmlMetadata) {
        this.htmlMetadata = htmlMetadata;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
