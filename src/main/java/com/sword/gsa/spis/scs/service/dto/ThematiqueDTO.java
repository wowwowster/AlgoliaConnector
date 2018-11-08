package com.sword.gsa.spis.scs.service.dto;

import java.util.ArrayList;
import java.util.List;

public class ThematiqueDTO {

    private String lvl0;
    private List<String> lvl1 = new ArrayList<>();
    private List<String> lvl2 = new ArrayList<>();

    // TODO @author claurier voir si ce constructeur est OK pour Algolia
    public ThematiqueDTO() {}

    public String getLvl0() {
        return lvl0;
    }

    public ThematiqueDTO setLvl0(String lvl0) {
        this.lvl0 = lvl0;
        return this;
    }

    public List<String> getLvl1() {
        return lvl1;
    }

    public ThematiqueDTO setLvl1(List<String> lvl1) {
        this.lvl1 = lvl1;
        return this;
    }

    public List<String> getLvl2() {
        return lvl2;
    }

    public ThematiqueDTO setLvl2(List<String> lvl2) {
        this.lvl2 = lvl2;
        return this;
    }

    public void addLvl1(String theme) {
        this.lvl1.add(theme);
    }

    public void addLvl2(String theme) {
        this.lvl2.add(theme);
    }

    @Override
    public String toString() {
        return "ThematiqueDTO{" + "lvl0='" + lvl0 + '\'' + ", lvl1=" + lvl1 + ", lvl2=" + lvl2 + '}';
    }
}

