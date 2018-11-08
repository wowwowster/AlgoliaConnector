package com.sword.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document")
public class Document {

    @NotNull
    @Id
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "name", length=255)
    private String name;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_page")
    private int totalPages;

    // TODO     stocker les données dans une colonne de type TEXT dans MySQL
    @OneToMany(mappedBy="document")
    //@OneToMany(mappedBy = "page", targetEntity = Page.class, cascade = CascadeType.ALL)
    private List<Page> pages = new ArrayList<Page>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public void addPage(Page page) {
        this.pages.add(page);
        if (page.getDocument() != this) {
            page.setDocument(this);
        }
    }
}
