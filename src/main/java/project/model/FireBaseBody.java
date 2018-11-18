package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FireBaseBody {
    private String success;

    public FireBaseBody() {
        success = "null";
    }

    @JsonCreator
    public FireBaseBody( @JsonProperty("success") String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}


