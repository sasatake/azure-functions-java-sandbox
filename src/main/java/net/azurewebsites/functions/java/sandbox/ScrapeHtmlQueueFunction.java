package net.azurewebsites.functions.java.sandbox;

import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.BlobInput;
import com.microsoft.azure.functions.annotation.BlobOutput;
import com.microsoft.azure.functions.annotation.QueueTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import java.util.List;

public class ScrapeHtmlQueueFunction {

  @FunctionName("ScrapeHtml")
  public void run(
      @QueueTrigger(name = "getEbookSummaryJobs", queueName = "getEbookSummaryJobs", connection = "QueueConnection") String message,
      @BlobInput(name = "html", dataType = "binary", path = "app/ebook/index.html", connection = "BlobConnection") byte[] html,
      @BlobOutput(name = "html", dataType = "binary", path = "app/ebook/index.html", connection = "BlobConnection") OutputBinding<byte[]> outputItem,
      @QueueOutput(name = "getEbookDetailJobs", queueName = "getEbookDetailJobs", connection = "QueueConnection") OutputBinding<List<String>> outMessages,
      final ExecutionContext context)
      throws IOException, InterruptedException {

    if (message == null || !message.equals("start")) {
      return;
    }

    byte[] content = html;
    var baseUri = "https://www.oreilly.co.jp/ebook/";

    if (content == null || content.length == 0) {
      var client = HttpClient.newHttpClient();
      var request = HttpRequest.newBuilder(
          URI.create(baseUri))
          .build();
      content = client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
      outputItem.setValue(content);
      context.getLogger().info("http request to " + baseUri);
    }

    Document topPage = Jsoup.parse(new ByteArrayInputStream(content), StandardCharsets.UTF_8.name(), baseUri);
    var ebookUrlList = topPage.select("table#bookTable > tbody > tr")
        .stream().map(e -> e.select("td.title > a[href]").attr("abs:href"))
        .collect(Collectors.toList());
    outMessages.setValue(ebookUrlList);
  }
}
