package com.example.ewaste.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL) // Hide null fields in JSON
public class ResponseMessage {
    private String status;  // "success" / "error"
    private String message;
    private Object data;
    private LocalDateTime timestamp; // Added timestamp

    public ResponseMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ResponseMessage(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static ResponseMessage success(String msg) {
        return new ResponseMessage("success", msg, null);
    }

    public static ResponseMessage success(String msg, Object data) {
        return new ResponseMessage("success", msg, data);
    }

    public static ResponseMessage error(String msg) {
        return new ResponseMessage("error", msg, null);
    }

    public static ResponseMessage error(String msg, Object data) {
        return new ResponseMessage("error", msg, data);
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setData(Object data) { this.data = data; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
