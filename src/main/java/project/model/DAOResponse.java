package project.model;

import org.springframework.http.HttpStatus;

public class DAOResponse<T> {
    public T body;
    public HttpStatus status;

    public DAOResponse() {

        this.status = HttpStatus.IM_USED;
    }

    public DAOResponse(T obj, HttpStatus status) {
        this.body = obj;
        this.status = status;
    }

}