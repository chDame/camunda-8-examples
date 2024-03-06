package org.example.camunda.process.solution;

public class Information {
  private String date;
  private String message;

  public Information() {}

  public Information(String date, String message) {
    super();
    this.date = date;
    this.message = message;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
