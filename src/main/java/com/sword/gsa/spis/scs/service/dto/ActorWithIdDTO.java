package com.sword.gsa.spis.scs.service.dto;

import java.util.List;

public class ActorWithIdDTO extends ActorDTO {

    private String objectID;

    public ActorWithIdDTO() {
        super();
    }

    public ActorWithIdDTO(String name, String alternative_name, String image_path, List<String> movies) {
        super(name, alternative_name, image_path, movies);
    }

    public ActorWithIdDTO(String objectID, String name, String alternative_name, String image_path, List<String> movies) {
        super(name, alternative_name, image_path, movies);
        this.objectID = objectID;
    }

    public String getObjectID() {
        return objectID;
    }

    public ActorWithIdDTO setObjectID(String objectID) {
        this.objectID = objectID;
        return this;
    }
}

