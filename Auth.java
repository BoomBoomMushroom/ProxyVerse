import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.URI;

public class Auth {
    static int x = 5;

    public static String test_request() { //https://470a416f-55a5-4399-913a-4e5d250e4241-00-1hud2082shrb2.riker.replit.dev/how_many_times_has_this_been_pinged
        HttpClient client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
            URI.create("https://470a416f-55a5-4399-913a-4e5d250e4241-00-1hud2082shrb2.riker.replit.dev/how_many_times_has_this_been_pinged")
        ).build();
        try{var response = client.send(request, BodyHandlers.ofString());}
        catch (Exception e) {throw e;}
        return "response.body()";
    }
}