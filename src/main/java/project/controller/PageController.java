package project.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.dao.PageDAO;
import project.dao.UserDAO;
import project.model.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@ResponseBody
@RestController
@CrossOrigin({"http://127.0.0.1:8000", "*"})
@RequestMapping("/qr")
public class PageController {

    private PageDAO pageDAO;
    private UserDAO userDAO;
    private static final String SESSION_KEY = "SessionKey";
    final String alphabet = "0123456789ABCDE";
    final int N = alphabet.length();
    final int DEFAULT_UUID_LENGTH = 36;




    public PageController(PageDAO pageDAO, UserDAO userDAO) {
        this.pageDAO = pageDAO;
        this.userDAO = userDAO;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createPage(@RequestBody Page body, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("UserYa isn't authorized"));
        }
        body.setStandalone(true);
        body.setOwnerID(user.getId().intValue());
        UUID uuid = UUID.randomUUID();
        body.setUuid(uuid.toString());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Instant instant = timestamp.toInstant();
        body.setDate(instant.toString());
        if (body.getTitle().equals("")) {
            body.setTitle("Unnamed");
        }
        DAOResponse<Page> daoResponse = pageDAO.createPage(body);
        if (daoResponse.status == HttpStatus.CREATED) {
            return  ResponseEntity.status(HttpStatus.CREATED).body(body);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("Something went wrong"));
    }

    @RequestMapping(path = "/container/create", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createPageContainer(@RequestBody PageContainer body, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        System.out.println("IN CONTAINER CREATE");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("UserYa isn't authorized"));
        }
        body.setStandalone(true);
        body.setOwnerID(user.getId().intValue());
        UUID uuid = UUID.randomUUID();
        Random r = new Random();
        body.setUuid(uuid.toString() + alphabet.charAt(r.nextInt(N)));
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Instant instant = timestamp.toInstant();
        body.setDate(instant.toString());
        if (body.getTitle().equals("")) {
            body.setTitle("Unnamed");
        }
        for (int i = 0; i < body.getInnerPages().length; i++) {
            UUID uuid1 = UUID.randomUUID();
            body.getInnerPages()[i].setUuid(uuid1.toString());
            body.getInnerPages()[i].setDate(instant.toString());
            body.getInnerPages()[i].setOwnerID(user.getId().intValue());
            if ( body.getInnerPages()[i].getTitle().equals("")) {
                body.getInnerPages()[i].setTitle("Unnamed");
            }
        }
        DAOResponse<PageContainer> daoResponse = pageDAO.createPageContainer(body);
        if (daoResponse.status == HttpStatus.CREATED) {
            return  ResponseEntity.status(HttpStatus.CREATED).body(body);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("Something went wrong"));
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getPage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            user = new UserYa();
        }
        if (!isContainer(pageUUID)) {
            DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
            Page requestedPage = daoResponse.body;
            if (requestedPage != null) {
                if (requestedPage.isPublic() || user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                    if ( !user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                        pageDAO.addViewedPage(user.getId().intValue(), pageUUID);
                    }
                    requestedPage.setOwnerID(0);
                    pageDAO.incrementViews();
                    return ResponseEntity.status(HttpStatus.OK).body(requestedPage);
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("Requested page is private"));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
        } else {
            DAOResponse<PageContainer> daoResponse = pageDAO.getPageContainerByID(pageUUID);
            PageContainer requestedPage = daoResponse.body;
            if (requestedPage != null) {
                if (requestedPage.isPublic() || user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                    if ( !user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                        pageDAO.addViewedPage(user.getId().intValue(), pageUUID);
                        System.out.println("1");
                    }
                    requestedPage.setOwnerID(0);
                    pageDAO.incrementViews();
                    return ResponseEntity.status(HttpStatus.OK).body(requestedPage);
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("Requested page is private"));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
        }


    }


    @RequestMapping(path = "/{id}/edit", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> editPage(@RequestBody Page body, @PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        Page requestedPage = daoResponse.body;
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("UserYa isn't authorized"));
        }
        if (requestedPage != null) {
            if (user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                if (body.getTitle().equals("")) {
                    body.setTitle("Unnamed");
                }
                daoResponse = pageDAO.editPage(body, pageUUID);
                if (daoResponse.status == HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully edited"));
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Something went wrong"));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("You are not allowed to edit this page"));
        }
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
    }

    @RequestMapping(path = "/container/{id}/edit", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> editPageContainer(@RequestBody PageContainer body, @PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);
        System.out.println("IN CONTAINER EDIT");
        DAOResponse<PageContainer> daoResponse = pageDAO.getPageContainerByID(pageUUID);
        PageContainer requestedPage = daoResponse.body;
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        if (requestedPage != null) {
            if (user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                if (body.getTitle().equals("")) {
                    body.setTitle("Unnamed");
                }
                body.setDate(requestedPage.getDate());
                body.setOwnerID(user.getId().intValue());
                body.setUuid(pageUUID);
                for (int i = 0; i < requestedPage.getInnerPagesUuids().length; i++) {
                    pageDAO.deletePage(requestedPage.getInnerPagesUuids()[i]);
                }
                pageDAO.deletePageContainer(pageUUID);
                for (int i = 0; i < body.getInnerPages().length; i++) {
                    UUID uuid = UUID.randomUUID();

                    body.getInnerPages()[i].setUuid(uuid.toString());
                    body.getInnerPages()[i].setDate(body.getDate());
                    body.getInnerPages()[i].setOwnerID(user.getId().intValue());
                    if ( body.getInnerPages()[i].getTitle().equals("")) {
                        body.getInnerPages()[i].setTitle("Unnamed1");
                    }
                }
                pageDAO.createPageContainer(body);
                if (daoResponse.status == HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully edited"));
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Something went wrong"));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("You are not allowed to edit this page"));
        }
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
    }


    @RequestMapping(path = "/{id}/delete", method = RequestMethod.DELETE, produces = "application/json")
    public ResponseEntity<?> deletePage(@PathVariable("id") String pageUUID, HttpSession httpSession) {
        User user = (User) httpSession.getAttribute(SESSION_KEY);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("UserYa isn't authorized"));
        }
        if (!isContainer(pageUUID)) {
            DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
            Page requestedPage = daoResponse.body;
            if (requestedPage != null) {
                if (user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                    daoResponse = pageDAO.deletePage(pageUUID);
                    if (daoResponse.status == HttpStatus.OK) {
                        pageDAO.deletePageFromViewers(pageUUID);
                        return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully deleted"));
                    }
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Something went wrong"));
                }
                daoResponse = pageDAO.deletePageFromViewers(pageUUID);
                if (daoResponse.status == HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully deleted"));
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("You are not allowed to edit this page"));
            }
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
        } else {
            DAOResponse<PageContainer> daoResponse = pageDAO.getPageContainerByID(pageUUID);
            PageContainer requestedPage = daoResponse.body;
            if (requestedPage != null) {
                if (user.getId().equals(BigDecimal.valueOf(requestedPage.getOwnerID()))) {
                    for (int i = 0; i < requestedPage.getInnerPagesUuids().length; i++) {
                        pageDAO.deletePage(requestedPage.getInnerPagesUuids()[i]);
                    }
                    daoResponse = pageDAO.deletePageContainer(pageUUID);
                    if (daoResponse.status == HttpStatus.OK) {
                        pageDAO.deletePageFromViewers(pageUUID);
                        return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully deleted"));
                    }
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Something went wrong"));
                }
//                pageDAO.deletePageFromViewers(pageUUID);
                if (pageDAO.deletePageFromViewers(pageUUID).status == HttpStatus.OK) {
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully deleted"));
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message("You are not allowed to edit this page"));
            }
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Requested page isn't found"));
        }

    }

    private boolean isContainer(String UUID) {
        if (UUID.length() > DEFAULT_UUID_LENGTH) {
            return true;
        } else {
            return false;
        }
    }
}
