package com.echapps.ecom.project.payment.model;

import com.echapps.ecom.project.order.model.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private Order order;

    @NotBlank
    @Size(min = 4, message = "Payment method must be at least 4 characters")
    private String paymentMethod;

    // Payment gateway (pg) details
    private String pgName;
    private String pgPaymentId;
    private String pgStatus;
    private String pgResponseMessage;

    public Payment(String pgName, Long paymentId, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        this.pgName = pgName;
        this.paymentId = paymentId;
        this.pgPaymentId = pgPaymentId;
        this.pgStatus = pgStatus;
        this.pgResponseMessage = pgResponseMessage;
    }

}
