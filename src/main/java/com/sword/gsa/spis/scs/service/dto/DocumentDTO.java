package com.sword.gsa.spis.scs.service.dto;

import com.sword.gsa.spis.scs.extracting.enums.ParagraphSplitModeEnum;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DocumentDTO {

    private String id;
    private String name;
    private String mimeType;
    private String url;
    private String site;
    private long indexationDate;
    private long creationDate;
    private int totalPages;
    private long documentSize;
    private List<PageDTO> pages = new ArrayList<PageDTO>();
    private ParagraphSplitModeEnum paragraphSplitMode;
    private ThematiqueDTO thematique;
    private TypeDocumentDTO typeDocument;

    // TODO @claurier voir sil'on ajoute l'attribut suivant : private HashMap metadata ;

    public DocumentDTO() {}

    public String getId() {
        return id;
    }

    public DocumentDTO setId(String id) {
        this.id = id;
        return this;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public DocumentDTO setTotalPages( int totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public List<PageDTO> getPages() {
        return pages;
    }

    public DocumentDTO setPages(List<PageDTO> pages) {
        this.pages = pages;
        return this;
    }

    public String getName() {
        return name;
    }

    public DocumentDTO setName(String name) {
        this.name = name;
        return this;
    }

    public long getIndexationDate() {
        return indexationDate;
    }

    public DocumentDTO setIndexationDate(long indexationDate) {
        this.indexationDate = indexationDate;
        return this;
    }

    /** @claurier on peut se contenter de LocalDate */
    public DocumentDTO setIndexationDate(LocalDateTime indexationDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        this.indexationDate = indexationDateTime.atZone(zoneId).toEpochSecond();
        return this;
    }

    public DocumentDTO setIndexationDate(String indexationDateTime) {
        this.indexationDate = Long.valueOf(indexationDateTime        );
        return this;
    }

    public void addPage(PageDTO page) {
        this.pages.add(page);
    }

    public long getDocumentSize() {
        return documentSize;
    }

    public DocumentDTO  setDocumentSize(long documentSize) {
        this.documentSize = documentSize;
        return this;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public DocumentDTO setCreationDate(long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    /** @claurier on peut se contenter de LocalDate */
    public DocumentDTO setCreationDate(LocalDateTime creationDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        this.creationDate = creationDateTime.atZone(zoneId).toEpochSecond();
        return this;
    }

    public DocumentDTO setCreationDate(String creationDateTime) {
        this.creationDate = Long.valueOf(creationDateTime);
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public DocumentDTO setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DocumentDTO setUrl(String url) {
        this.url = url;
        return this;
    }

    public ParagraphSplitModeEnum getParagraphSplitMode() {
        return paragraphSplitMode;
    }

    public DocumentDTO setParagraphSplitMode(ParagraphSplitModeEnum paragraphSplitMode) {
        this.paragraphSplitMode = paragraphSplitMode;
        return this;
    }

    public ThematiqueDTO getThematique() {
        return thematique;
    }

    public DocumentDTO setThematique(ThematiqueDTO thematique) {
        this.thematique = thematique;
        return this;
    }

    public TypeDocumentDTO getTypeDocument() {
        return typeDocument;
    }

    public DocumentDTO setTypeDocument(TypeDocumentDTO typeDocument) {
        this.typeDocument = typeDocument;
        return this;
    }

    public String getSite() {
        return site;
    }

    public DocumentDTO setSite(String site) {
        this.site = site;
        return this;
    }

    @Override public String toString() {
        return "DocumentDTO{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", mimeType='"
            + mimeType + '\'' + ", url='" + url + '\'' + ", site='" + site + '\''
            + ", indexationDate=" + indexationDate + ", creationDate=" + creationDate
            + ", totalPages=" + totalPages + ", documentSize=" + documentSize + ", pages=" + pages
            + ", paragraphSplitMode=" + paragraphSplitMode + ", thematique=" + thematique
            + ", typeDocument=" + typeDocument + '}';
    }
}
