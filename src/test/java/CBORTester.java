import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import data.Identity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CBORTester {


    public static void main(String[] args) throws Exception  {

        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
// and then read/write data as usual
        Identity identity = new Identity();
        identity.setEmail("test@test.com");
        identity.setAddress("Hello CBOR Street");
        byte[] cborData = mapper.writeValueAsBytes(identity);


        Identity otherValue = mapper.readValue(cborData, Identity.class);

/*        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.ofByteArray(cborData));
                .build();

        HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray()); */

    }
}
