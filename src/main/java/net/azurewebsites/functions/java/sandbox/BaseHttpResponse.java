package net.azurewebsites.functions.java.sandbox;

public class BaseHttpResponse {

  private String message;

  public BaseHttpResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
