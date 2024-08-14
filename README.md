# Salesforce Loan Calculator Integration

This repository contains a Salesforce project that integrates with the SpreadsheetWeb API to perform Excel calculations. It features an Excel spreadsheet for calculating loan payments and generating an amortization schedule, though it can be applied to any Excel file containing worksheet formulas. The project includes Apex classes, a Visualforce page, and necessary configuration to perform API calls and display the results in a user-friendly manner.

## Components

### Apex Classes

- **CalculationController2**: Handles the loan calculation logic and prepares data for the Visualforce page.
- **CalculationService2**: Manages API requests to SpreadsheetWeb.
- **TokenService**: Handles token retrieval for API authentication.

### Visualforce Page

- **LoanCalculatorPage**: Collects user input and displays the loan calculation results.

## Setup and Configuration

### Prerequisites

- Salesforce Developer Org
- SpreadsheetWeb Account with access to the API

### Salesforce Setup

1. **Apex Classes**:
   - Navigate to Setup > Apex Classes.
   - Create new Apex classes with the content provided in the `CalculationController2`, `CalculationService2`, and `TokenService` sections below.
   
2. **Visualforce Page**:
   - Navigate to Setup > Visualforce Pages.
   - Create a new Visualforce page with the content provided in the `LoanCalculatorPage` section below.

3. **Configuration**:
   - Update the `workspaceId`, `applicationId`, `clientId`, and `clientSecret` in the Apex classes with your SpreadsheetWeb credentials.

### Example Configuration

```apex
// In CalculationController2
String workspaceId = 'YourWorkspaceID';
String applicationId = 'YourApplicationID';

// In TokenService
String clientId = 'YourClientID';
String clientSecret = 'YourClientSecret';
```
## Usage

1. **Navigate to the Visualforce Page**:
   - Open the newly created Visualforce page (`LoanCalculatorPage`) in your Salesforce org.

2. **Enter Loan Details**:
   - Input the loan amount, interest rate, loan period, and start date.

3. **Calculate**:
   - Click the "Calculate" button to perform the calculation.

4. **View Results**:
   - The scheduled payment amount and amortization chart will be displayed below the input form.

## Detailed Explanation of Components

### CalculationController2

The `CalculationController2` Apex class handles user inputs, prepares API requests, processes API responses, and stores the results for display on the Visualforce page.

**Key Methods:**

- `calculate()`: Prepares input data, makes an API call, and processes the response to extract the scheduled payment amount and amortization chart.

```apex
public class CalculationController2 {
    public String loanAmount { get; set; }
    public String interestRate { get; set; }
    public String loanPeriod { get; set; }
    public String loanStartDate { get; set; }
    public String scheduledPaymentAmt { get; set; }
    public List<AmortizationRow> amortizationChart { get; set; }
    public String requestBody { get; set; }
    public String responseBody { get; set; }
    public String rawResponse { get; set; }

    public CalculationController2() {
        amortizationChart = new List<AmortizationRow>();
    }

    public void calculate() {
        List<CalculationService.CalculationInput> inputs = new List<CalculationService.CalculationInput>();

        CalculationService.CalculationInput loanAmountInput = new CalculationService.CalculationInput();
        loanAmountInput.reference = 'loan_amount';
        loanAmountInput.value = new List<List<Map<String, String>>>{ new List<Map<String, String>>{ new Map<String, String>{ 'value' => loanAmount }}};
        inputs.add(loanAmountInput);

        CalculationService.CalculationInput interestRateInput = new CalculationService.CalculationInput();
        interestRateInput.reference = 'interest_rate';
        interestRateInput.value = new List<List<Map<String, String>>>{ new List<Map<String, String>>{ new Map<String, String>{ 'value' => interestRate }}};
        inputs.add(interestRateInput);

        CalculationService.CalculationInput loanPeriodInput = new CalculationService.CalculationInput();
        loanPeriodInput.reference = 'loan_period';
        loanPeriodInput.value = new List<List<Map<String, String>>>{ new List<Map<String, String>>{ new Map<String, String>{ 'value' => loanPeriod }}};
        inputs.add(loanPeriodInput);

        CalculationService.CalculationInput loanStartDateInput = new CalculationService.CalculationInput();
        loanStartDateInput.reference = 'loan_start_date';
        loanStartDateInput.value = new List<List<Map<String, String>>>{ new List<Map<String, String>>{ new Map<String, String>{ 'value' => loanStartDate }}};
        inputs.add(loanStartDateInput);

        List<String> outputs = new List<String>{ 'scheduled_payment_amt', 'amortization_chart' };

        try {
            String workspaceId = 'YourWorkspaceID';
            String applicationId = 'YourApplicationID';
            
            CalculationService.CalculationRequest calculationRequest = new CalculationService.CalculationRequest(workspaceId, applicationId, inputs, outputs);
            requestBody = JSON.serialize(calculationRequest);
            
            CalculationService.CalculationResponse response = CalculationService.calculate(workspaceId, applicationId, inputs, outputs);
            
            responseBody = JSON.serialize(response);
            rawResponse = response.rawResponse;

            Map<String, Object> rawResponseMap = (Map<String, Object>) JSON.deserializeUntyped(rawResponse);
            Map<String, Object> responseMap = (Map<String, Object>) rawResponseMap.get('response');
            Map<String, Object> responseInnerMap = (Map<String, Object>) responseMap.get('response');
            Map<String, Object> outputMap = (Map<String, Object>) responseInnerMap.get('output');
            Map<String, Object> calculationMap = (Map<String, Object>) outputMap.get('calculation');

            if (calculationMap != null && calculationMap.containsKey('outputs')) {
                List<Object> outputsList = (List<Object>) calculationMap.get('outputs');
                for (Object outputObj : outputsList) {
                    Map<String, Object> outputItem = (Map<String, Object>) outputObj;
                    if (outputItem.get('reference') == 'scheduled_payment_amt') {
                        List<Object> valueList = (List<Object>) outputItem.get('value');
                        if (valueList != null && !valueList.isEmpty()) {
                            List<Object> innerValueList = (List<Object>) valueList.get(0);
                            if (innerValueList != null && !innerValueList.isEmpty()) {
                                Map<String, Object> valueItem = (Map<String, Object>) innerValueList.get(0);
                                scheduledPaymentAmt = (String) valueItem.get('text');
                            }
                        }
                    } else if (outputItem.get('reference') == 'amortization_chart') {
                        List<Object> valueList = (List<Object>) outputItem.get('value');
                        if (valueList != null && !valueList.isEmpty()) {
                            for (Integer i = 1; i < valueList.size(); i++) { // Skip header row
                                List<Object> rowList = (List<Object>) valueList.get(i);
                                AmortizationRow row = new AmortizationRow();
                                row.Year = (String) ((Map<String, Object>) rowList.get(0)).get('text');
                                row.Remaining = (String) ((Map<String, Object>) rowList.get(1)).get('text');
                                row.InterestPaid = (String) ((Map<String, Object>) rowList.get(2)).get('text');
                                row.PrincipalPaid = (String) ((Map<String, Object>) rowList.get(3)).get('text');
                                amortizationChart.add(row);
                            }
                        }
                    }
                }
            } else {
                scheduledPaymentAmt = 'Error calculating scheduled payment amount. Full response: ' + responseBody;
            }
        } catch (Exception e) {
            responseBody = e.getMessage();
            scheduledPaymentAmt = 'Request: ' + requestBody + ' | Response: ' + rawResponse + ' | Error: ' + responseBody;
        }
    }

    public class AmortizationRow {
        public String Year { get; set; }
        public String Remaining { get; set; }
        public String InterestPaid { get; set; }
        public String PrincipalPaid { get; set; }
    }
}
```
### CalculationService2

The `CalculationService2` Apex class manages the interaction with the SpreadsheetWeb API, including constructing API requests and handling responses.

**Key Methods:**

- `calculate()`: Makes a call to the SpreadsheetWeb API with the provided inputs and returns the calculation results.

```apex
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
```
### TokenService

The `TokenService` Apex class retrieves an access token from the SpreadsheetWeb identity service, which is used to authenticate API requests.

**Key Methods:**

- `getBearerToken()`: Sends a request to the SpreadsheetWeb identity service to retrieve an access token.

```apex
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
        
        String clientId = 'YourClientID';
        String clientSecret = 'YourClientSecret';
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
```
### Visualforce Page (LoanCalculatorPage)

The Visualforce page provides a user interface for inputting loan details and displaying the results.

```html
<apex:page controller="CalculationController2">
    <apex:form>
        <apex:pageBlock title="Loan Calculator">
            <apex:pageBlockSection title="Loan Calculator" columns="2">
                <apex:inputText value="{!loanAmount}" label="Loan Amount"/>
                <apex:inputText value="{!interestRate}" label="Interest Rate"/>
                <apex:inputText value="{!loanPeriod}" label="Loan Period"/>
                <apex:inputText value="{!loanStartDate}" label="Loan Start Date"/>
                <apex:commandButton value="Calculate" action="{!calculate}"/>
            </apex:pageBlockSection>
        </apex:pageBlock>
        
        <apex:pageBlock title="Scheduled Payment Amount">
            <apex:outputText value="{!scheduledPaymentAmt}" rendered="{!scheduledPaymentAmt != null}"/>
        </apex:pageBlock>

        <apex:pageBlock title="Amortization Chart">
            <apex:pageBlockTable value="{!amortizationChart}" var="row" rendered="{!amortizationChart != null}">
                <apex:column value="{!row.Year}" headerValue="Year"/>
                <apex:column value="{!row.Remaining}" headerValue="Remaining"/>
                <apex:column value="{!row.InterestPaid}" headerValue="Interest Paid"/>
                <apex:column value="{!row.PrincipalPaid}" headerValue="Principal Paid"/>
            </apex:pageBlockTable>
        </apex:pageBlock>
    </apex:form>
</apex:page>
```
## Example Charts

Here are examples of how the amortization chart and scheduled payment amount will be displayed:

**Scheduled Payment Amount:**
Scheduled Payment Amount: $2,684

**Amortization Chart**

Here is an example of how the amortization chart will be displayed:

| Year | Remaining Balance | Interest Paid | Principal Paid |
|------|-------------------|---------------|----------------|
| 0    | $500,000          | $2,083        | $1,601         |
| 1    | $480,344          | $26,555       | $21,338        |
| 2    | $459,683          | $50,017       | $42,086        |
| 3    | $437,965          | $72,417       | $63,895        |
| ...  | ...               | ...           | ...            |

## Conclusion

This project demonstrates a practical integration between Salesforce and the SpreadsheetWeb API for loan calculations. By following the setup instructions and using the provided components, you can easily implement similar functionality in your own Salesforce org.

For further assistance or questions, please refer to the [SpreadsheetWeb API documentation](https://www.spreadsheetweb.com/api-documentation) or contact support.
