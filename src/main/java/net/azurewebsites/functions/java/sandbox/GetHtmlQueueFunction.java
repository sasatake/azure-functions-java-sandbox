package net.azurewebsites.functions.java.sandbox;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.BlobOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.net.URI;

public class GetHtmlQueueFunction {

  @FunctionName("GetHtml")
  public void run(
      @QueueTrigger(name = "message", queueName = "jobs", connection = "QueueConnection") String message,
      @BlobOutput(name = "html", dataType = "binary", path = "app/ebook/index.html", connection = "BlobConnection") OutputBinding<byte[]> outputItem,
      final ExecutionContext context)
      throws IOException, InterruptedException {

    if (message != null && message.equals("start")) {

      var client = HttpClient.newHttpClient();

      var request = HttpRequest.newBuilder(
          URI.create("https://www.oreilly.co.jp/ebook/"))
          .build();

      outputItem.setValue(client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body());
    }
  }
}
