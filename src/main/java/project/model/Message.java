package project.model;

public class Message {
    private String message;

    public Message(String str) {
        message = str;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}
