import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class Auth {
    static int x = 5;

    public static String test_request() { //https://470a416f-55a5-4399-913a-4e5d250e4241-00-1hud2082shrb2.riker.replit.dev/how_many_times_has_this_been_pinged
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("https://470a416f-55a5-4399-913a-4e5d250e4241-00-1hud2082shrb2.riker.replit.dev/how_many_times_has_this_been_pinged"))
            .GET()
            .build();
        try {
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "Failure";
    }
}