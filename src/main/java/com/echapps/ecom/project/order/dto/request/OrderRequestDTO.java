package com.echapps.ecom.project.order.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @Schema(description = "Unique identifier for the order", example = "1")
    private Long addressId;

    @Schema(description = "Payment method used for the transaction", example = "CARD for (Credit/Debit Card)")
    private String paymentMethod;

    @Schema(description ="pgName is the name of the payment gateway used for processing the payment, such as Stripe, PayPal, etc.", example = "Stripe")
    private String pgName;

    @Schema(description = "pgPaymentId is the unique identifier provided by the payment gateway for the transaction, such as a payment ID or transaction ID.", example = "pay_1234567890")
    private String pgPaymentId;

    @Schema(description = "pgStatus is the status of the payment transaction as returned by the payment gateway, such as 'succeeded', 'failed', 'pending', etc.", example = "succeeded")
    private String pgStatus;

    @Schema(description = "pgResponseMessage is the message or description provided by the payment gateway regarding the transaction, which may include details about the success or failure of the payment.", example = "Payment successful")
    private String pgResponseMessage;
}
