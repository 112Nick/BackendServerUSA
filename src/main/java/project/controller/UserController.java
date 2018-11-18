package project.controller;


import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import project.dao.PageDAO;
import project.dao.UserDAO;
import project.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@ResponseBody
@RestController
@CrossOrigin({"http://127.0.0.1:8000", "*"})
@RequestMapping("/")
public class UserController {

    private UserDAO userDAO;
    private PageDAO pageDAO;
    private static final String SESSION_KEY = "SessionKey";

    public UserController(UserDAO userDAO, PageDAO pageDAO) {
        this.userDAO = userDAO;
        this.pageDAO = pageDAO;
//        pageDAO.setViews();
    }

    @RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> baseRequest(HttpSession httpSession,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         @RequestParam(value = "own", required = false) String own,
                                         @RequestParam(value = "search", required = false) String search) {
        System.out.println("pages");

        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        DAOResponse<List<Page>> daoResponse = pageDAO.getUsersPages(user.getId().intValue(), sort, own, search);
        if (daoResponse.status == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("No pages found, check filters or create new one"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(daoResponse.body);
    }

    @RequestMapping(path = "/getuser", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getUser(HttpSession httpSession) {
        final User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Message(user.getDefault_email()));
    }


    @RequestMapping(path = "/stats", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> getStats(HttpSession httpSession) {
        System.out.println("pages");

        User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        DAOResponse<Stats> daoResponse = pageDAO.getStats();
        if (daoResponse.status == HttpStatus.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("No pages found, check filters or create new one"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(daoResponse.body);
    }


    @RequestMapping(path = "/login/yandex", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> loginUserYandex(@RequestBody Token token, HttpSession httpSession) {
        System.out.println("login");
        try{
            URL url = new URL("https://login.yandex.ru/info?format=json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "OAuth " + token.getToken());
            Integer status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                Gson g = new Gson();
                System.out.println(content.toString());
                UserYa user = g.fromJson(content.toString(), UserYa.class);
                System.out.println(user.getDefault_email());
                user.setToken(token.getToken());
                DAOResponse<User> daoResponse = userDAO.createUser(user);
                if (daoResponse.status == HttpStatus.CONFLICT) {
                    DAOResponse<Integer> daoResponse1 = userDAO.getUserID(user.getDefault_email());
                    user.setId(BigDecimal.valueOf(daoResponse1.body));
                    httpSession.setAttribute(SESSION_KEY, user);
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully authorized"));
                }
                user.setId(daoResponse.body.getId());
                httpSession.setAttribute(SESSION_KEY, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(new Message("Successfully registered"));
            }
            con.disconnect();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 1"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 2"));
        }
    }


    @RequestMapping(path = "/login/google", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> loginUserGoogle(@RequestBody Token token, HttpSession httpSession) {
        System.out.println("login");
        System.out.println(token.getToken());
        try{
            URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + token.getToken());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            System.out.println(url);
            con.setRequestMethod("GET");
//            con.setRequestProperty("Content-Type", "application/json");
            Integer status = con.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                Gson g = new Gson();
                System.out.println(content.toString());
                UserGoogle user = g.fromJson(content.toString(), UserGoogle.class);
                System.out.println(user.getEmail());
                user.setToken(token.getToken());
                DAOResponse<User> daoResponse = userDAO.createUser(user);
                if (daoResponse.status == HttpStatus.CONFLICT) {
                    DAOResponse<Integer> daoResponse1 = userDAO.getUserID(user.getEmail());
                    user.setId(BigDecimal.valueOf(daoResponse1.body));
                    httpSession.setAttribute(SESSION_KEY, user);
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully authorized"));
                }
                user.setId(daoResponse.body.getId());
                httpSession.setAttribute(SESSION_KEY, user);
                return ResponseEntity.status(HttpStatus.CREATED).body(new Message("Successfully registered"));
            }
            con.disconnect();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 1"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Try another service to login 2"));
        }
    }

    @RequestMapping(path = "/setdevice", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> setDevice(@RequestBody Token token, HttpSession httpSession) {
        final User user = (User) httpSession.getAttribute(SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message("User isn't authorized"));
        }
        Integer userID = userDAO.getUserID(user.getEmail()).body;
        String[] userDevices = userDAO.getUserDevicesById(userID).body;
        String[] newUserDevices = new String[userDevices.length + 1];
        for (int i = 0 ; i < userDevices.length; i++) {
            newUserDevices[i] = userDevices[i];
        }
        newUserDevices[newUserDevices.length - 1] = token.getToken();
        DAOResponse<Integer> daoResponse = userDAO.setDevices(newUserDevices, userID);
        if (daoResponse.status == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK).body(new Message("OK"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Something went wrong"));
    }


    @RequestMapping(path = "/push/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> pushNotification(@PathVariable("id") String pageUUID,  HttpSession httpSession) {
        System.out.println("push");
        DAOResponse<Page> daoResponse = pageDAO.getPageByID(pageUUID);
        if (daoResponse.status != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("No such page"));
        }
        String[] userDevices = userDAO.getUserDevicesById(daoResponse.body.getOwnerID()).body;

        String title = "\"" + daoResponse.body.getFieldsValues()[0] + "\",";
        String message = "\"" + daoResponse.body.getFieldsValues()[1] + "\",";
        String devices = "";
        for (int i = 0; i < userDevices.length; i++) {
            devices += "\"" + userDevices[i] + "\", ";
        }
        if (userDevices.length > 0 ) {
            devices = devices.substring(0, devices.length()-2);
        }
//        System.out.println(devices);

//        String body = "{" +
//                "\"notification\": {" +
//                "\"title\": \"ТЕСТ\"," +
//                "\"body\": \"Начало в 21:00\"," +
//                "\"icon\": \"https://eralash.ru.rsz.io/sites/all/themes/eralash_v5/logo.png?width=40&height=40\"," +
//                "\"click_action\": \"http://eralash.ru/\" }," +
//                "\"registration_ids\": [" +
//                "\"eBdi1qEI2p0:APA91bEFChx2F6HnxDyxYwtXflkf-TMvpm2S-sq6iamAXr_3k5-CZTRsiTXK3Ymx8WtTCVOg9tDm7bxrH1atUcjpXLCWZlkwANFekkOmik4Fw8r17WWXotcsFqdROj1NDeZmj2S4tF1u\"" +
//                "] " +
//                "}";

        String body = "{" +
                "\"notification\": {" +
                "\"title\":" + title +
                "\"body\":" + message +
                "\"icon\": \"https://velox-app.herokuapp.com/icons/favicon.jpg\"," +
                "\"click_action\": \"https://velox-app.herokuapp.com/\" }," +
                "\"registration_ids\":" + "[" + devices + "]" +
                "}";


        StringEntity entity = new StringEntity(body,
                ContentType.APPLICATION_FORM_URLENCODED);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost("https://fcm.googleapis.com/fcm/send");
//        System.out.println(System.getenv("NOTIFICATION_KEY"));
        request.setHeader("Authorization", "key=" + System.getenv("NOTIFICATION_KEY"));
//        request.setHeader("Authorization", "key=" + NOTIFICATION_KEY);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(entity);
        try {
            HttpResponse response = httpClient.execute(request);
            System.out.println(response.getStatusLine().getStatusCode());
            String firebaseBody =  EntityUtils.toString(response.getEntity());
            Gson g = new Gson();
            System.out.println(firebaseBody);
            FireBaseBody frbody = g.fromJson(firebaseBody, FireBaseBody.class);
            System.out.println(frbody.getSuccess());

            if (Integer.valueOf(frbody.getSuccess()) > 0) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    return ResponseEntity.status(HttpStatus.OK).body(new Message("Successfully notified"));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Oops, try again"));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("User wasn't notified"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Oops, something went wrong"));
        }
    }




    @RequestMapping(path = "/logout", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> logoutUser(HttpSession httpSession) {
        if (httpSession.getAttribute(SESSION_KEY) != null) {
            httpSession.invalidate();
            return ResponseEntity.status(HttpStatus.OK).body(new Message("Successful logout"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message("Unsuccessful logout"));
    }

    ///////////////////////////////////////////
    @RequestMapping(path = "/dropdb", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> clearTables(HttpSession httpSession) {
        ResponseEntity response;
        pageDAO.dropTables();
        response = ResponseEntity.status(HttpStatus.OK).body("Successful droped");
        return response;

    }
    //////////////////////////////////////////
}
