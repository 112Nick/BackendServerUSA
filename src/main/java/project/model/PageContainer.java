package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageContainer {
    private String uuid;
    private int ownerID;
    private String title;
    private boolean isPublic;
    private boolean isStatic;
    private boolean isMine;
    private String template;
    private Page[] innerPages;
    private String[] innerPagesUuids;
    private String date;
    private boolean standalone;



    public PageContainer() {
        this.title = "JustCreated";
    }


    @JsonCreator
    public PageContainer( @JsonProperty("uuid") String uuid,
                          @JsonProperty("ownerID") int ownerID,
                          @JsonProperty("title") String title,
                          @JsonProperty("isPublic") boolean isPublic,
                          @JsonProperty("isStatic") boolean isStatic,
                          @JsonProperty("isMine") boolean isMine,
                          @JsonProperty("template") String template,
                          @JsonProperty("innerPages") Page[] innerPages,
                          @JsonProperty("date") String date) {
        this.uuid = uuid;
        this.ownerID = ownerID;
        this.title = title;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.isMine = isMine;
        this.template = template;
        this.innerPages = innerPages;
        this.date = date;
        this.standalone = true;
    }

//    @JsonCreator
    public PageContainer( String uuid,
                         int ownerID,
                          String title,
                          boolean isPublic,
                          boolean isStatic,
                          boolean isMine,
                           String template,
                          String[] innerPagesUuids,
                           String date) {
        this.uuid = uuid;
        this.ownerID = ownerID;
        this.title = title;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.isMine = isMine;
        this.template = template;
        this.innerPagesUuids = innerPagesUuids;
        this.date = date;
        this.standalone = true;
    }



    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public Page[] getInnerPages() {
        return innerPages;
    }

    public void setInnerPages(Page[] innerPages) {
        this.innerPages = innerPages;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String[] getInnerPagesUuids() {
        return innerPagesUuids;
    }

    public void setInnerPagesUuids(String[] innerPagesUuids) {
        this.innerPagesUuids = innerPagesUuids;
    }

    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

}
