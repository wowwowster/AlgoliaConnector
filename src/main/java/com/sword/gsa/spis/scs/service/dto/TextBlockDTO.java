package com.sword.gsa.spis.scs.service.dto;

import com.sword.gsa.spis.scs.algolia.annotation.IndexAnnotation;

@IndexAnnotation(indexName = "dev_textblocks") public class TextBlockDTO {

    private String objectID;

    private int insertNumber;

    private String content;

    private int pageNumber;

    private String urlImage;

    private DocumentDTO document;

    public TextBlockDTO() {
    }

    public int getInsertNumber() {
        return insertNumber;
    }

    public TextBlockDTO setInsertNumber(int insertNumber) {
        this.insertNumber = insertNumber;
        return this;
    }

    public String getContent() {
        return content;
    }

    public TextBlockDTO setContent(String content) {
        this.content = content;
        return this;
    }

    public String getObjectID() {
        return objectID;
    }

    public TextBlockDTO setObjectID(String objectID) {
        this.objectID = objectID;
        return this;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public TextBlockDTO setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        return this;
    }

    public DocumentDTO getDocument() {
        return document;
    }

    public TextBlockDTO setDocument(DocumentDTO document) {
        this.document = document;
        return this;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    @Override public String toString() {
        return "TextBlockDTO{" + "objectID='" + objectID + '\'' + ", insertNumber=" + insertNumber
            + ", content='" + content + '\'' + ", pageNumber=" + pageNumber + ", urlImage='"
            + urlImage + '\'' + ", document=" + document + '}';
    }
}
