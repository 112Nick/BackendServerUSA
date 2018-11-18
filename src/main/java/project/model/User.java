package project.model;

import java.math.BigDecimal;

public interface User {

    BigDecimal getId();

    String getLogin();

    String getEmail();

    String getDefault_email();

    String getToken();

    String[] getDevices();

    void setDevices(String[] devices);

    void setId(BigDecimal id);

}
