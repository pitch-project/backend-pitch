package org.pitch.payment_service.controller;

import org.pitch.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentController {
  private final PaymentService paymentService;

  @Autowired
  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping("/test-charge")
  public ResponseEntity<String> testStringCharge() {
    Long testAmount = 400L;
    String testCurrency = "pen";
    String testPaymentMethodId = "pm_card_visa";
    String testDescription = "Test payment fro Pitch Reservation";

    String result = paymentService.processTestTransaction(testAmount, testCurrency, testPaymentMethodId, testDescription);

    if (result.startsWith("Error")) {
      return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    } else {
      return new ResponseEntity<>(result, HttpStatus.OK);
    }
  }
}
