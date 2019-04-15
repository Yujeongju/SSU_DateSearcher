package com.datesearcher.datesearcher;

public class Review_Tag {
    private String tag;
    private String tag_cnt;
    private String number;

    public Review_Tag(String tag, String tag_cnt) {
        this.tag = tag;
        this.tag_cnt = tag_cnt;
    }

    public String getTag() {
        return tag;
    }

    public String getTag_cnt() {
        return tag_cnt;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTag_cnt(String tag_cnt) {
        this.tag_cnt = tag_cnt;
    }

}
