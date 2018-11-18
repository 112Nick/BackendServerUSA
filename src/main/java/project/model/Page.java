package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Page {
    private String uuid;
    private int ownerID;
    private String title;
    private boolean isPublic;
    private boolean isStatic;
    private boolean isMine;
    private boolean standalone;
    private String template;
    private String[] fieldsNames;
    private String[] fieldsValues;
    private String date;


    public Page() {
        this.title = "JustCreated";
        this.standalone = false;
    }


    @JsonCreator
    public Page( @JsonProperty("uuid") String uuid,
                 @JsonProperty("ownerID") int ownerID,
                 @JsonProperty("title") String title,
                 @JsonProperty("isPublic") boolean isPublic,
                 @JsonProperty("isStatic") boolean isStatic,
                 @JsonProperty("isMine") boolean isMine,
                 @JsonProperty("template") String template,
                 @JsonProperty("fieldsNames") String[] fieldsNames,
                 @JsonProperty("fieldsValues") String[] fieldsValues,
                 @JsonProperty("date") String date) {
        this.uuid = uuid;
        this.ownerID = ownerID;
        this.title = title;
        this.isPublic = isPublic;
        this.isStatic = isStatic;
        this.isMine = isMine;
        this.template = template;
        this.fieldsNames = fieldsNames;
        this.fieldsValues = fieldsValues;
        this.date = date;
        this.standalone = false;

    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String UUID) {
        this.uuid = UUID;
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

    public String[] getFieldsNames() {
        return fieldsNames;
    }

    public void setFieldsNames(String[] fieldsNames) {
        this.fieldsNames = fieldsNames;
    }

    public String[] getFieldsValues() {
        return fieldsValues;
    }

    public void setFieldsValues(String[] fieldsValues) {
        this.fieldsValues = fieldsValues;
    }

    public boolean isStandalone() {
        return standalone;
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }


}
