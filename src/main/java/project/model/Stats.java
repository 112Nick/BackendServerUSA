package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Stats {


    private Integer users;
    private Integer pages;
    private Integer views;

    @JsonCreator
    public Stats(
            @JsonProperty("users") Integer users,
            @JsonProperty("pages") Integer pages,
            @JsonProperty("views") Integer views
    ) {
        this.users = users;
        this.pages = pages;
        this.views = views;
    }



    public Stats() {
        this.users = 100;
        this.pages = 200;
        this.views = 300;
    }

    public Integer getUsers() {
        return users;
    }

    public void setUsers(Integer users) {
        this.users = users;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }
}
