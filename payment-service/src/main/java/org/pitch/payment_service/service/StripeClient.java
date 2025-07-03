package org.pitch.payment_service.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeClient {
  public StripeClient() {
    System.out.println("Initializing Stripe Client");
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

  public Product retrieveProduct(String productId) throws StripeException {
    return Product.retrieve(productId);
  }

  public Price retrievePrice(String priceId) throws StripeException {
    return Price.retrieve(priceId);
  }
}
