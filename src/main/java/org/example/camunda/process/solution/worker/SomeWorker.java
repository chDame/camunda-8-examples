package org.example.camunda.process.solution.worker;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SomeWorker {

  @Autowired ZeebeClient client;

  private static final Logger LOG = LoggerFactory.getLogger(SomeWorker.class);

  @JobWorker
  public ProcessVariables getPriceFromSheet(@Variable String product)
      throws IOException, GeneralSecurityException {
    LOG.info("Invoking getPriceFromSheet for product: " + product);
    try (InputStream is = new FileInputStream("Price spreadsheet.xlsx");
        ReadableWorkbook wb = new ReadableWorkbook(is)) {
      Sheet sheet = wb.getFirstSheet();
      List<Row> rows = sheet.read();
      for (int i = 1; i < rows.size(); i++) {
        Row r = rows.get(i);
        String productCode = r.getCellAsString(0).orElse(null);
        if (productCode.equals(product)) {
          BigDecimal price = r.getCellAsNumber(1).orElse(null);
          return new ProcessVariables()
              .setPrice(price)
              .addInformation("Price retrieved : " + price);
        }
      }
    }
    return new ProcessVariables()
        .addInformation("Price could not be retrieved cause the product code doesn't exist");
  }

  @JobWorker
  public ProcessVariables checkInventory(@VariablesAsType ProcessVariables variables) {
    if (variables.getQty() > 200) {
      return variables
          .addInformation("Product " + variables.getProduct() + " has not enough stock")
          .setInsufficientStock(true);
    }
    return variables
        .addInformation("Product " + variables.getProduct() + " has enough stock")
        .setInsufficientStock(false);
  }

  @JobWorker
  public ProcessVariables emitSupplierOrder(@VariablesAsType ProcessVariables variables) {
    client
        .newPublishMessageCommand()
        .messageName("SUPPLIER_INVOICE")
        .correlationKey(variables.getOrderId())
        .timeToLive(Duration.ofHours(1))
        .send();
    return variables
        .addInformation("Supplier order emmited for Product " + variables.getProduct())
        .setReappro(DateUtils.now());
  }

  @JobWorker
  public ProcessVariables paySupplier(@VariablesAsType ProcessVariables variables) {
    client
        .newPublishMessageCommand()
        .messageName("SHIPMENT")
        .correlationKey(variables.getOrderId())
        .timeToLive(Duration.ofHours(1))
        .send();
    return variables.addInformation("Supplier invoiced paid on " + variables.getProduct());
  }

  @JobWorker
  public ProcessVariables pickAndPack(@VariablesAsType ProcessVariables variables) {
    return variables.addInformation("Pick and pack done");
  }

  @JobWorker
  public ProcessVariables dispatch(@VariablesAsType ProcessVariables variables) {
    return variables.addInformation("Dispacth done");
  }
}
