package com.sword.gsa.spis.scs.service.dto;

public class ParagraphDTO {

    private String objectID;

    private int insertNumber;

    private String content;

    private int pageNumber;

    private String urlImage;

    private DocumentDTO document;

    public ParagraphDTO() {
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public int getInsertNumber() {
        return insertNumber;
    }

    public ParagraphDTO setInsertNumber(int insertNumber) {
        this.insertNumber = insertNumber;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ParagraphDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public String getObjectID() {
        return objectID;
    }

    public ParagraphDTO setObjectID(String objectID) {
        this.objectID = objectID;
        return this;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public ParagraphDTO setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public DocumentDTO getDocument() {
        return document;
    }

    public ParagraphDTO setDocument(DocumentDTO document) {
        this.document = document;
        return this;
    }

}
