public class CalculationService2 {
    public class CalculationInput {
        public String reference;
        public List<List<Map<String, String>>> value;
    }

    public class CalculationRequest {
        public Map<String, Object> request;

        public CalculationRequest(String workspaceId, String applicationId, List<CalculationInput> inputs, List<String> outputs) {
            Map<String, Object> calculation = new Map<String, Object>();
            calculation.put('inputs', inputs);
            calculation.put('outputs', outputs);
            
            Map<String, Object> input = new Map<String, Object>();
            input.put('calculation', calculation);
            
            Map<String, Object> innerRequest = new Map<String, Object>();
            innerRequest.put('input', input);
            
            Map<String, Object> outerRequest = new Map<String, Object>();
            outerRequest.put('workspaceId', workspaceId);
            outerRequest.put('applicationId', applicationId);
            outerRequest.put('request', innerRequest);
            
            this.request = outerRequest;
        }
    }

    public class OutputValue {
        public String type;
        public String formatType;
        public String format;
        public String text;
        public String value;
        public String overwrite;
    }

    public class Output {
        public String reference;
        public List<List<OutputValue>> value;
        public String overwrite;
    }

    public class Calculation {
        public Boolean success;
        public List<Output> outputs;
        public List<String> validations;
        public List<String> messages;
    }

    public class OutputResponse {
        public Boolean success;
        public Calculation calculation;
        public String goalSeek;
        public String solver;
    }

    public class Response {
        public String applicationId;
        public OutputResponse response;
        public String saveResult;
        public Integer usedTransactionSequenceId;
        public String requestId;
        public Boolean success;
        public String eventCreationDate;
        public String retryIndex;
        public String debugRetryAllowFailureCount;
    }

    public class CalculationResponse {
        public Response response;
        public Map<String, Double> timingsSeconds;
        public String performanceInformation;
        public Boolean isError;
        public List<String> messages;
        public String rawResponse;
    }

    public static CalculationResponse calculate(String workspaceId, String applicationId, List<CalculationInput> inputs, List<String> outputs) {
        String token = TokenService.getBearerToken();
        if(token == null) {
            throw new CalloutException('Token is null. Cannot proceed with the API call.');
        }
        
        Http http = new Http();
        HttpRequest request = new HttpRequest();
        request.setEndpoint('https://api.spreadsheetweb.com/calculations/calculatesingle');
        request.setMethod('POST');
        request.setHeader('Authorization', 'Bearer ' + token);
        request.setHeader('Content-Type', 'application/json');
        
        CalculationRequest calculationRequest = new CalculationRequest(workspaceId, applicationId, inputs, outputs);
        String body = JSON.serialize(calculationRequest);
        request.setBody(body);
        
        HttpResponse response = http.send(request);

        if (response.getStatusCode() == 200) {
            String responseBody = response.getBody();
            CalculationResponse calculationResponse = (CalculationResponse) JSON.deserialize(responseBody, CalculationResponse.class);
            calculationResponse.rawResponse = responseBody;
            return calculationResponse;
        } else {
            throw new CalloutException('Failed to perform calculation: ' + response.getBody());
        }
    }
}