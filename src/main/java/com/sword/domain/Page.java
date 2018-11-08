package com.sword.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "page")
public class Page {

    @NotNull
    @Id
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "numero")
    private int numero;

    @Column(name = "extracted_text")
    private String extractedText;

    @ManyToOne
    //@ManyToOne(targetEntity = DocumentImpl.class)
    @JoinColumn(name="id", insertable = false, updatable = false)
    private Document document;

    public void setDocument(Document document) {
        this.document = document;
        if (!document.getPages().contains(this)) {
            document.getPages().add(this);
        }
    }

    public Document getDocument() {
        return document;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

}
