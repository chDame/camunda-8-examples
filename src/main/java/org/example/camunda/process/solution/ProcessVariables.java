package org.example.camunda.process.solution;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.example.camunda.process.solution.utils.DateUtils;

@JsonInclude(Include.NON_NULL)
public class ProcessVariables {

  private String orderId;
  private String customer;
  private Integer qty;
  private String product;
  private BigDecimal price;
  private List<Information> informations;
  private Boolean insufficientStock;
  private String reappro;

  public String getOrderId() {
    return orderId;
  }

  public ProcessVariables setOrderId(String orderId) {
    this.orderId = orderId;
    return this;
  }

  public String getCustomer() {
    return customer;
  }

  public ProcessVariables setCustomer(String customer) {
    this.customer = customer;
    return this;
  }

  public String getProduct() {
    return product;
  }

  public ProcessVariables setProduct(String product) {
    this.product = product;
    return this;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public ProcessVariables setPrice(BigDecimal price) {
    this.price = price;
    return this;
  }

  public Integer getQty() {
    return qty;
  }

  public ProcessVariables setQty(Integer qty) {
    this.qty = qty;
    return this;
  }

  public List<Information> getInformations() {
    return informations;
  }

  public ProcessVariables setInformations(List<Information> informations) {
    this.informations = informations;
    return this;
  }

  public ProcessVariables addInformation(String information) {
    if (this.informations == null) {
      this.informations = new ArrayList<>();
    }
    this.informations.add(new Information(DateUtils.now(), information));
    return this;
  }

  public Boolean getInsufficientStock() {
    return insufficientStock;
  }

  public ProcessVariables setInsufficientStock(Boolean insufficientStock) {
    this.insufficientStock = insufficientStock;
    return this;
  }

  public String getReappro() {
    return reappro;
  }

  public ProcessVariables setReappro(String reappro) {
    this.reappro = reappro;
    return this;
  }
}
