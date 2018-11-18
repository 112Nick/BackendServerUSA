package project.dao;

import org.springframework.jdbc.core.RowMapper;
import project.model.Page;
import project.model.PageContainer;
import project.model.UserYa;

import java.math.BigDecimal;
import java.sql.Array;

public class Mappers {

    public static final RowMapper<PageContainer> pageContainerMapper = (res, num) -> {
        String uuid = res.getString("uuid");
        Integer ownerID = res.getInt("ownerid");
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Boolean isStatic = res.getBoolean("isstatic");
        String template = res.getString("template");
        Array innerPagesuids = res.getArray("innerpages");
        String date = res.getString("date");
        return new PageContainer(uuid, ownerID, title, isPublic, isStatic, true, template, (String[])innerPagesuids.getArray(), date);
    };

    public static final RowMapper<Page> pageMapper = (res, num) -> {
        String uuid = res.getString("uuid");
        Integer ownerID = res.getInt("ownerid");
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Boolean isStatic = res.getBoolean("isstatic");
        String template = res.getString("template");
        Array fieldsNames = res.getArray("fieldsnames");
        Array fieldsValues = res.getArray("fieldsvalues");
        String date = res.getString("date");

        return new Page(uuid, ownerID, title, isPublic, isStatic, true, template, (String[])fieldsNames.getArray(), (String[])fieldsValues.getArray(), date);
    };

    public static final RowMapper<Page> pageFullMapper = (res, num) -> {
        String uuid = res.getString("uuid");
//        Integer ownerID = res.getInt("ownerid");
        Integer ownerID = 0;
        String title = res.getString("title");
        Boolean isPublic = res.getBoolean("ispublic");
        Boolean isStatic = res.getBoolean("isstatic");
        Boolean isMine = res.getBoolean("ismine");
        String template = res.getString("template");
        Array fieldsNames = res.getArray("fieldsnames");
        Array fieldsValues = res.getArray("fieldsvalues");
        String date = res.getString("date");

        return new Page(uuid, ownerID, title, isPublic, isStatic, isMine, template, (String[])fieldsNames.getArray(), (String[])fieldsValues.getArray(), date);
    };

    public static final RowMapper<UserYa> userMapper = (res, num) -> {
        BigDecimal id = res.getBigDecimal("id");
        String login = res.getString("login");
        String email = res.getString("email");
        String token = res.getString("token");
        Array devices = res.getArray("devices");
        if (devices == null) {
            System.out.println("123");
        }
        return new UserYa(id, login, email, token, (String[])devices.getArray());
    };
}
