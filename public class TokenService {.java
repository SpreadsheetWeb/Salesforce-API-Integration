public class TokenService {
    public class TokenResponse {
        public String access_token;
        public String token_type;
        public Integer expires_in;
    }

    public static String getBearerToken() {
        Http http = new Http();
        HttpRequest request = new HttpRequest();
        request.setEndpoint('https://identity.spreadsheetweb.com/connect/token');
        request.setMethod('POST');
        request.setHeader('Content-Type', 'application/x-www-form-urlencoded');
        
       
        String clientId = 'YOUR-CLIENT-ID';
        String clientSecret = 'YOUR-CLIENT SECRET';
        String body = 'grant_type=client_credentials&client_id=' + clientId + '&client_secret=' + clientSecret;
        request.setBody(body);
        
        HttpResponse response = http.send(request);

        if (response.getStatusCode() == 200) {
            TokenResponse tokenResponse = (TokenResponse) JSON.deserialize(response.getBody(), TokenResponse.class);
            return tokenResponse.access_token;
        } else {
            throw new CalloutException('Failed to get token: ' + response.getBody());
        }
    }
}