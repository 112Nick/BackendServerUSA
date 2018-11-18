package project.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import project.model.*;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Service
public class PageDAO {

    private final JdbcTemplate template;

    public PageDAO(JdbcTemplate template) {
        this.template = template;
    }

    public DAOResponse<Page> createPage(Page body)  {
        DAOResponse<Page> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO page(uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone)" +
                                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getUuid());
                statement.setInt(2, body.getOwnerID());
                statement.setString(3, body.getTitle());
                statement.setBoolean(4, body.isPublic());
                statement.setBoolean(5, body.isStatic());
                statement.setString(6, body.getTemplate());
                statement.setArray(7, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(8, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setString(9, body.getDate());
                statement.setBoolean(10, body.isStandalone());
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
            return result;
        }
        catch (DuplicateKeyException e) {
            e.printStackTrace();
            result.status = HttpStatus.CONFLICT;
        }
        return result;

    }


    public DAOResponse<PageContainer> createPageContainer(PageContainer body) {
        DAOResponse<PageContainer> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String[] innerPagesUuids = new String[body.getInnerPages().length];
        for (int i = 0; i < body.getInnerPages().length; i++) {
            body.getInnerPages()[i].setStandalone(false);
            createPage(body.getInnerPages()[i]);
            innerPagesUuids[i] = body.getInnerPages()[i].getUuid();
        }

        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO page(uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, innerPages, date, standalone)" +
                                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1, body.getUuid());
                statement.setInt(2, body.getOwnerID());
                statement.setString(3, body.getTitle());
                statement.setBoolean(4, body.isPublic());
                statement.setBoolean(5, body.isStatic());
                statement.setString(6, body.getTemplate());
                statement.setArray(7, con.createArrayOf("TEXT", new Object[1]));
                statement.setArray(8, con.createArrayOf("TEXT",  new Object[1]));
                statement.setArray(9, con.createArrayOf("TEXT", innerPagesUuids));
                statement.setString(10, body.getDate());
                statement.setBoolean(11, body.isStandalone());
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
            return result;
        }
        catch (DuplicateKeyException e) {
            e.printStackTrace();
            result.status = HttpStatus.CONFLICT;
        }
        return result;

    }

    public DAOResponse<Page> getPageByID(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        try {
            final Page foundPage =  template.queryForObject(
                    "SELECT * FROM page WHERE uuid = ?",
                    new Object[]{pageUUID},  Mappers.pageMapper);
            result.body = foundPage;
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }

    public DAOResponse<PageContainer> getPageContainerByID(String pageContainerUUID) {
        DAOResponse<PageContainer> result = new DAOResponse<>();
        System.out.println(pageContainerUUID);
        try {
            final PageContainer foundPageContainer =  template.queryForObject(
                    "SELECT * FROM page WHERE uuid = ?",
                    new Object[]{pageContainerUUID},  Mappers.pageContainerMapper);
            Page[] innerPages = new Page[foundPageContainer.getInnerPagesUuids().length];
            String[] innerPagesUuids = new String[foundPageContainer.getInnerPagesUuids().length];
            for(int i = 0; i < foundPageContainer.getInnerPagesUuids().length; i++) {
                innerPages[i] = getPageByID(foundPageContainer.getInnerPagesUuids()[i]).body;
                innerPagesUuids[i] = innerPages[i].getUuid();
                System.out.println(innerPages[i].getUuid());
            }

            foundPageContainer.setInnerPages(innerPages);
            foundPageContainer.setInnerPagesUuids(innerPagesUuids);

            result.body = foundPageContainer;
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }


    public DAOResponse<Stats> getStats() {
        DAOResponse<Stats> result = new DAOResponse<>();
        try {

            Integer users = template.queryForObject(
                    "SELECT COUNT(*) FROM \"user\";",
                    new Object[]{}, Integer.class);

            Integer pages = template.queryForObject(
                    "SELECT COUNT(*) FROM page;",
                    new Object[]{}, Integer.class);


            Integer views = getViews();


            result.body = new Stats();
            result.body.setPages(pages);
            result.body.setUsers(users);
            result.body.setViews(views);
            result.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            result.body = null;
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }


    public DAOResponse<Page> editPage(Page body, String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE page SET " +
                                " title = ?," +
                                " ispublic = ?, " +
                                " isstatic = ?, " +
                                " fieldsnames = ?, " +
                                " fieldsvalues = ? " +
                                "WHERE uuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , body.getTitle());
                statement.setBoolean(2, body.isPublic());
                statement.setBoolean(3, body.isStatic());
                statement.setArray(4, con.createArrayOf("TEXT", body.getFieldsNames()));
                statement.setArray(5, con.createArrayOf("TEXT", body.getFieldsValues()));
                statement.setString(6, pageUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        } catch(DuplicateKeyException e){
            e.printStackTrace();
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

    public void incrementViews() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            Integer views = getViews() + 1;
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE views SET " +
                                " views = ? where id = 1",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1 , views);
                return statement;
            }, keyHolder);
        } catch(DuplicateKeyException e){
            e.printStackTrace();
        }
    }

    public void setViews() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT into views(views) VALUES(?) ",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1 , 1);
                return statement;
            }, keyHolder);
        } catch(DuplicateKeyException e){
            e.printStackTrace();
        }
    }

    public Integer getViews() {
        Integer result = 0;
        try {
            result = template.queryForObject(
                    "SELECT views FROM views where id = 1;",
                    new Object[]{}, Integer.class);
            return result;
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            setViews();
            return 0;
        }
    }


    public DAOResponse<PageContainer> editPageContainer(PageContainer body, String pageContainerUUID) {
        DAOResponse<PageContainer> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String[] innerPagesUuids = new String[body.getInnerPages().length];
        DAOResponse<Page> daoResponse;
        for (int i = 0; i < body.getInnerPages().length; i++) {
            daoResponse = getPageByID(body.getInnerPagesUuids()[i]);
            if (daoResponse.status == HttpStatus.NOT_FOUND || daoResponse.body == null) {
                createPage(body.getInnerPages()[i]);
            } else {
                editPage(body.getInnerPages()[i], body.getInnerPages()[i].getUuid());
            }
            innerPagesUuids[i] = body.getInnerPages()[i].getUuid();
        }

        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "UPDATE page SET " +
                                " title = ?," +
                                " ispublic = ?, " +
                                " innerPages = ? " +
                                "WHERE uuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , body.getTitle());
                statement.setBoolean(2, body.isPublic());
                statement.setArray(3, con.createArrayOf("TEXT", innerPagesUuids));
                statement.setString(4, pageContainerUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        } catch(DuplicateKeyException e){
            e.printStackTrace();
            result.status = HttpStatus.CONFLICT;
        }
        return result;

    }



    public DAOResponse<Page> deletePage(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                    PreparedStatement statement = con.prepareStatement(
                            "DELETE FROM page WHERE uuid = ?",
                            PreparedStatement.RETURN_GENERATED_KEYS);
                    statement.setString(1 , pageUUID);
                    return statement;
                    }, keyHolder);
            result.status = HttpStatus.OK;
        }
        catch (Exception e) {
            e.printStackTrace();
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }

    public DAOResponse<PageContainer> deletePageContainer(String pageUUID) {
        DAOResponse<PageContainer> result = new DAOResponse<>();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "DELETE FROM page WHERE uuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , pageUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        }
        catch (Exception e) {
            e.printStackTrace();
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;

    }

    public DAOResponse<Page> deletePageFromViewers(String pageUUID) {
        DAOResponse<Page> result = new DAOResponse<>();
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "DELETE FROM userpages WHERE pageuuid = ?",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setString(1 , pageUUID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.OK;
        }
        catch (Exception e) {
            e.printStackTrace();
            result.status = HttpStatus.NOT_FOUND;
        }
        return result;
    }

    public DAOResponse<UserYa> addViewedPage(Integer userID, String pageID)  {
        DAOResponse<UserYa> result = new DAOResponse<>();
        result.body = null;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            template.update(con -> {
                PreparedStatement statement = con.prepareStatement(
                        "INSERT INTO userpages(userid, pageuuid)" + " VALUES(?, ?)" ,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                statement.setInt(1, userID);
                statement.setString(2, pageID);
                return statement;
            }, keyHolder);
            result.status = HttpStatus.CREATED;
        }
        catch (DuplicateKeyException e) {
            result.status = HttpStatus.CONFLICT;
        }
        return result;
    }

//    public DAOResponse<UserYa> addViewedPageContainer(Integer userID, String pageID)  {
//        DAOResponse<UserYa> result = new DAOResponse<>();
//        result.body = null;
//        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
//        try {
//            template.update(con -> {
//                PreparedStatement statement = con.prepareStatement(
//                        "INSERT INTO userpagecontainers(userid, pageuuid)" + " VALUES(?, ?)" ,
//                        PreparedStatement.RETURN_GENERATED_KEYS);
//                statement.setInt(1, userID);
//                statement.setString(2, pageID);
//                return statement;
//            }, keyHolder);
//            result.status = HttpStatus.CREATED;
//        }
//        catch (DuplicateKeyException e) {
//            result.status = HttpStatus.CONFLICT;
//        }
//        return result;
//    }


    public DAOResponse<List<Page>> getUsersPages(Integer userID, String sort, String own, String search) {
        DAOResponse<List<Page>> daoResponse = new DAOResponse<>();
        List<Object> tmpObj = new ArrayList<>();
        tmpObj.add(userID);
        tmpObj.add(userID);
        String sqlQuery;

        if (sort == null || sort.equals("")) {
            sort = "a-z";
        }
        if (own == null || own.equals("")) {
            own = "all";
        }

        switch(own) {
            case "me":
                sqlQuery = "SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM page WHERE ownerid = ? AND standalone = true";
                break;
            case "others":
                sqlQuery = "SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM userpages JOIN page ON pageuuid = uuid WHERE userid = ? AND standalone = true";

                break;
            case "all":
                tmpObj.add(userID);
                tmpObj.add(userID);
                sqlQuery = "SELECT * FROM (SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM page WHERE ownerid = ? AND standalone = true " +
                        "UNION SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM userpages JOIN page ON pageuuid = uuid WHERE userid = ? AND standalone = true) as h";
                break;
            default:
                tmpObj.add(userID);
                tmpObj.add(userID);
                sqlQuery = "SELECT * FROM (SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone, " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine  " +
                        "FROM page WHERE ownerid = ? AND standalone = true " +
                        "UNION SELECT uuid, ownerid, title, ispublic, isstatic, template, fieldsnames, fieldsvalues, date, standalone,  " +
                        "CASE WHEN ownerid = ? THEN true ELSE false END AS ismine " +
                        "FROM userpages JOIN page ON pageuuid = uuid WHERE userid = ? AND standalone = true) as h";
                break;
        }

        if (search != null && !search.isEmpty()) {
            sqlQuery += " WHERE LOWER(title) LIKE '%" + search.toLowerCase() + "%'";
        }

        switch (sort) {
            case "a-z":
                sqlQuery += " ORDER BY title";
                break;
            case "z-a":
                sqlQuery += " ORDER BY title DESC";
                break;
            case "date":
                sqlQuery += " ORDER BY date DESC";
                break;
        }

        try {
            daoResponse.body = template.query( sqlQuery,
                    tmpObj.toArray(), Mappers.pageFullMapper);
            daoResponse.status = HttpStatus.OK;
        }
        catch (DataAccessException e) {
            e.printStackTrace();
            daoResponse.body = null;
            daoResponse.status = HttpStatus.NOT_FOUND;
        }
        return daoResponse;
    }

    ///////////////////////////////
    public void dropTables() {
        template.update(
                "TRUNCATE page, userpages CASCADE;" //TODO only users when connected
        );
    }


    ///////////////////////////////

}
