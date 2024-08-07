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
            String workspaceId = 'YOUR-WORKSPACE-ID';
            String applicationId = 'YOUR-APPLICATION-ID';
            
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
                            for (Integer i = 1; i < valueList.size(); i++) { 
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
