package com.sword.gsa.spis.scs.service.dto;

import java.util.List;

public class ActorDTO {

    private String name;
    private String alternative_name;
    private String image_path;
    private List<String> movies;

    public ActorDTO() {
    }

    public ActorDTO(String name, String alternative_name, String image_path, List<String> movies) {
        this.name = name;
        this.alternative_name = alternative_name;
        this.image_path = image_path;
        this.movies = movies;
    }

    public String getName() {
        return name;
    }

    public ActorDTO setName(String name) {
        this.name = name;
        return this;
    }


    public String getAlternative_name() {
        return alternative_name;
    }

    public ActorDTO setAlternative_name(String alternative_name) {
        this.alternative_name = alternative_name;
        return this;
    }

    public String getImage_path() {
        return image_path;
    }

    public ActorDTO setImage_path(String image_path) {
        this.image_path = image_path;
        return this;
    }

    public List<String> getMovies() {
        return movies;
    }

    public ActorDTO setMovies(List<String> movies) {
        this.movies = movies;
        return this;
    }

    @Override
    public String toString() {
        return "ActorDTO{" + "name='" + name + '\'' + ", alternative_name=" + alternative_name + '\'' + ", image_path=" + image_path + '\'' + ", movies=" + movies.toString() + '}';
    }
}