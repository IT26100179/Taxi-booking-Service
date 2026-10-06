package com.taxibooking.repository;

import com.taxibooking.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * File Repository for Payment & Transactions (Component 5 - Fare Calculation & Payment Management)
 */
@Repository
public class PaymentFileRepository extends AbstractFileRepository<Payment, String> {

    public PaymentFileRepository(@Value("${app.data.payments-file:data/payments.txt}") String filePath) {
        super(filePath);
    }

    @Override
    protected String getId(Payment entity) {
        return entity.getPaymentId();
    }

    @Override
    protected String serialize(Payment entity) {
        return entity.toFileString();
    }

    @Override
    protected Payment deserialize(String line) {
        return Payment.fromFileString(line);
    }
}
