package com.datesearcher.datesearcher;

public class Post {
    private String card_url;
    private String like;
    private String keyword;
    private String hashtag;

    public Post(String card_url, String like, String hashtag, String keyword) {
        this.card_url = card_url;
        this.like = like;
        this.hashtag = hashtag;
        this.keyword = keyword;
    }

    public String getCard_url() {
        return card_url;
    }

    public String getLike() {
        return like;
    }

    public String getHashtag() { return hashtag; }

    public void setCard_url(int card_img) {
        this.card_url = card_url;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public String getKeyword(){
        return keyword;
    }
}
