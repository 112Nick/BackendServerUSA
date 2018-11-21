
package project.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class UserVk implements User{
    private BigDecimal id;
    private String login;
    private String default_email;
    private String token;
    private String[] devices;

    @JsonCreator
    public UserVk(
            @JsonProperty("id") BigDecimal id,
            @JsonProperty("first_name") String login,
            @JsonProperty("phone") String default_email,
            @JsonProperty("token") String token,
            @JsonProperty("devices") String[] devices
    ) {
        this.id = id;
        this.login = login;
        this.default_email = default_email;
        this.token = token;
        this.devices = devices;
    }



    public UserVk() {
        this.id = BigDecimal.valueOf(0);
        this.login = "created";
        this.default_email = "created";
        this.token = "created";
        this.devices = new String[5];

    }

    @Override
    public BigDecimal getId() {
        return id;
    }

    @Override
    public void setId(BigDecimal id) {
        this.id = id;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getEmail() {
        return default_email;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public String getDefault_email() {
        return default_email;
    }

    public void setDefault_email(String default_email) {
        this.default_email = default_email;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String[] getDevices() {
        return this.devices;
    }

    @Override
    public void setDevices(String[] devices) {
        this.devices = devices;
    }


    public void setToken(String token) {
        this.token = token;
    }
}


