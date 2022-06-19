package net.azurewebsites.functions.java.sandbox;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class GetHtmlStartHttpFunction {

  @FunctionName("GetHtmlStart")
  public HttpResponseMessage run(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.GET }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
      @QueueOutput(name = Constants.EBOOK_SUMMARY_QUEUE_NAME, queueName = Constants.EBOOK_SUMMARY_QUEUE_NAME, connection = "QueueConnection") OutputBinding<String> message,
      final ExecutionContext context) throws JsonParseException, JsonMappingException, JsonProcessingException {

    var response = "Accepted.";

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(new BaseHttpResponse(response));

    message.setValue("start");

    return request.createResponseBuilder(HttpStatus.ACCEPTED)
        .body(json).header("content-type", "application/json").build();
  }
}
