package org.pitch.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeClient {
  public StripeClient() {
    System.out.println("Initializing Stripe Client");
  }

  /**
  *  paymentIntent.capture()
  * */

  public PaymentIntent createPaymentIntent(Long amount,
                                           String currency,
                                           String paymentMethod,
                                           String description)
      throws StripeException {
    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(amount)
        .setCurrency(currency)
        .addPaymentMethodType("card")
        .setPaymentMethod(paymentMethod)
        .setDescription(description)
        .setConfirm(true)
        .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.AUTOMATIC)
        .build();

    return PaymentIntent.create(params);
  }

  public Customer createStripeCustomer(String name, String email)
      throws StripeException {
    CustomerCreateParams params = CustomerCreateParams.builder()
        .setName(name)
        .setEmail(email)
        .build();

    return Customer.create(params);
  }

  public Subscription createSubscription(String customerId, String priceid)
      throws StripeException {
    SubscriptionCreateParams params = SubscriptionCreateParams.builder()
        .setCustomer(customerId)
        .addItem(SubscriptionCreateParams.Item.builder()
            .setPrice(priceid)
            .build())
        .build();

    return Subscription.create(params);
  }

  /**
   *  product.retrieve()
   *  price.retrieve()
  public Product retrieveProduct(String productId) throws StripeException {
    return Product.retrieve(productId);
  }

  public Price retrievePrice(String priceId) throws StripeException {
    return Price.retrieve(priceId);
  }* */
}
