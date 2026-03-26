package com.echapps.ecom.project.payment.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    @Schema(description = "Unique identifier for the payment", example = "1")
    private Long paymentId;

    @Schema(description = "Payment method used for the transaction", example = "CARD for (Credit/Debit Card)")
    private String paymentMethod;

    @Schema(description = "Amount paid for the transaction", example = "999.99")
    private Double amount;

    @Schema(description = "payment gateway used for processing the payment", example = "Stripe")
    private String pgName;

    @Schema(description = "Unique identifier provided by the payment gateway for the transaction", example = "pay_1234567890")
    private String pgPaymentId;

    @Schema(description = "Status of the payment transaction as returned by the payment gateway", example = "succeeded")
    private String pgStatus;

    @Schema(description = "Message or description provided by the payment gateway regarding the transaction", example = "Payment successful")
    private String pgResponseMessage;

}
