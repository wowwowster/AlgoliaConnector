package com.sword.gsa.spis.scs.extracting.enums;

public enum ParagraphSplitModeEnum {

    NATURAL_SPLIT_MODE(1),
    INCLUDING_TITLE_SPLIT_MODE(2),
    POSITION_SPLIT_MODE(3);

    int code;

    ParagraphSplitModeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
