package com.sword.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "html_metadata")
public class HTMLMetadata {

    @NotNull
    @Id
    @Column(name = "id", updatable = false)
    private long id;

    @ManyToOne
    //@ManyToOne(targetEntity = WebPage.class)
    @JoinColumn(name="id", insertable = false, updatable = false)
    private WebPage webPage;

    @Column(name = "cle", length=255)
    private String cle;

    @Column(name = "valeur", length=255)
    private String valeur;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public WebPage getWebPage() {
        return webPage;
    }

    public void setWebPage(WebPage webPage) {
        this.webPage = webPage;
    }

    public String getCle() {
        return cle;
    }

    public void setCle(String cle) {
        this.cle = cle;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }
}


