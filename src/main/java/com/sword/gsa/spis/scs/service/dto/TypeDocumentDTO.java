package com.sword.gsa.spis.scs.service.dto;

import java.util.ArrayList;
import java.util.List;

public class TypeDocumentDTO {

    private String lvl0;
    private List<String> lvl1 = new ArrayList<>();
    private List<String> lvl2 = new ArrayList<>();

    // TODO @author claurier voir si ce constructeur est OK pour Algolia
    public TypeDocumentDTO() {}

    public String getLvl0() {
        return lvl0;
    }

    public TypeDocumentDTO setLvl0(String lvl0) {
        this.lvl0 = lvl0;
        return this;
    }

    public List<String> getLvl1() {
        return lvl1;
    }

    public void setLvl1(List<String> lvl1) {
        this.lvl1 = lvl1;
    }

    public List<String> getLvl2() {
        return lvl2;
    }

    public void setLvl2(List<String> lvl2) {
        this.lvl2 = lvl2;
    }

    public void addLvl1(String subType) {
        this.lvl1.add(subType);
    }

    public void addLvl2(String subType) {
        this.lvl2.add(subType);
    }

    @Override
    public String toString() {
        return "TypeDocumentDTO{" + "lvl0='" + lvl0 + '\'' + ", lvl1=" + lvl1 + ", lvl2=" + lvl2 + '}';
    }
}

