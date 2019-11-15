package bitcoinaverage;



import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HelloBitCoinWorld {

    //X-ba-key: YzkyZjMyNjVmMjlkNGM2YWIwZjhmYjZlNzI4MjVjNGE

    //curl -H "X-testing: testing" https://apiv2.bitcoinaverage.com/indices/global/ticker/all?crypto=BTC&fiat=USD,EUR


    public static void main(String[] args) throws Exception {

        HelloBitCoinWorld helloBitCoinWorld = new HelloBitCoinWorld();
        helloBitCoinWorld.test();

    }

    HttpClient  client = HttpClient.newHttpClient();

    public void test() throws Exception
    {
        String url = "https://apiv2.bitcoinaverage.com/indices/global/ticker/all?crypto=BTC&fiat=USD,EUR";
        String result = get(url);
        System.out.println(result);
    }


    public String get(String url) throws Exception
    {

        HttpRequest httprequest = HttpRequest.newBuilder()
                .uri(URI.create( url))
                .GET()
                .timeout(Duration.ofMinutes(1))
                .header("X-testing","testing")
                .build();


        HttpResponse<String> response =
                client.send(httprequest, HttpResponse.BodyHandlers.ofString());

        return response.body();


    }
}
