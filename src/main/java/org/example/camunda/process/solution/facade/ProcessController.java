package org.example.camunda.process.solution.facade;

import io.camunda.operate.exception.OperateException;
import io.camunda.operate.model.ProcessDefinition;
import io.camunda.zeebe.client.ZeebeClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.camunda.process.solution.ProcessVariables;
import org.example.camunda.process.solution.security.annotation.IsAuthenticated;
import org.example.camunda.process.solution.service.OperateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/process")
public class ProcessController {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessController.class);
  private final ZeebeClient zeebe;
  private final OperateService operateService;
  private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:SS");

  public ProcessController(ZeebeClient client, OperateService operateService) {
    this.zeebe = client;
    this.operateService = operateService;
  }

  public static int counter = 0;

  public static String generateUUID() {
    // for demo reasons we generate something readable
    if (counter == 0) {
      counter =
          Calendar.getInstance().get(Calendar.MINUTE) + Calendar.getInstance().get(Calendar.SECOND);
    } else {
      counter++;
    }
    String result = "A-" + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + counter;
    return result;
  }

  @GetMapping("/refund/{orderId}")
  public void refund(@PathVariable String orderId) {
    zeebe.newPublishMessageCommand().messageName("RETURN").correlationKey(orderId).send();
  }

  @GetMapping("/simulateMailOrder")
  public String simulateMailOrder() {

    ProcessVariables variables =
        new ProcessVariables()
            .setOrderId(generateUUID())
            .setCustomer("jothikiruthika.viswanathan@camunda.com")
            .setProduct("B")
            .setQty((int) Math.round(Math.random() * 100))
            .addInformation("Mail received on " + sdf.format(new Date()));
    zeebe
        .newCreateInstanceCommand()
        .bpmnProcessId("price_process")
        .latestVersion()
        .variables(variables)
        .send();

    return variables.getOrderId();
  }

  @IsAuthenticated
  @PostMapping("/{bpmnProcessId}/start")
  public void startProcessInstance(
      @PathVariable String bpmnProcessId, @RequestBody Map<String, Object> variables) {

    LOG.info("Starting process `" + bpmnProcessId + "` with variables: " + variables);

    if (bpmnProcessId.equals("price_process")) {
      variables.put("orderId", generateUUID());
    }

    zeebe
        .newCreateInstanceCommand()
        .bpmnProcessId(bpmnProcessId)
        .latestVersion()
        .variables(variables)
        .send();
  }

  @IsAuthenticated
  @PostMapping("/message/{messageName}/{correlationKey}")
  public void publishMessage(
      @PathVariable String messageName,
      @PathVariable String correlationKey,
      @RequestBody Map<String, Object> variables) {

    LOG.info(
        "Publishing message `{}` with correlation key `{}` and variables: {}",
        messageName,
        correlationKey,
        variables);

    zeebe
        .newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .variables(variables)
        .send();
  }

  @IsAuthenticated
  @GetMapping("/definition/latest")
  public List<ProcessDefinition> latestDefinitions() throws OperateException {
    Set<String> present = new HashSet<>();
    List<ProcessDefinition> result = new ArrayList<>();
    List<ProcessDefinition> processDefs = operateService.getProcessDefinitions();
    if (processDefs != null) {
      for (ProcessDefinition def : processDefs) {
        if (!present.contains(def.getBpmnProcessId())) {
          result.add(def);
          present.add(def.getBpmnProcessId());
        }
      }
    }
    return result;
  }
}
