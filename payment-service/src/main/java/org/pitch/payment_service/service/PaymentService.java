package org.pitch.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
  private final StripeClient stripeClient;

  @Autowired
  public PaymentService(StripeClient stripeClient) {
    this.stripeClient = stripeClient;
  }

  public String processTestTransaction(Long amount, String currency, String paymentMethod, String description) {
    try {
      PaymentIntent paymentIntent = stripeClient.createPaymentIntent(amount, currency, paymentMethod, description);
      return "Payment intent created: " + paymentIntent.getId();
    } catch (StripeException e) {
      return "Error: " + e.getMessage();
    }
  }

  public String registerUserAndSubscribe(String userName, String userEmail, String planPriceId) {
    try {
      Customer customer = stripeClient.createStripeCustomer(userName, userEmail);

      System.out.println("Customer created: " + customer.getId());

      Subscription subscription = stripeClient.createSubscription(customer.getId(), planPriceId);
      System.out.println("Subscription created: " + subscription.getId());

      return "User registered successfully " + customer.getName();
    } catch (StripeException e) {
      return "Error: " + e.getMessage();
    } // Capturar Escecopnes "Exception"
  }
}
