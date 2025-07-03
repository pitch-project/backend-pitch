package org.pitch.payment_service;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration // Marca esta clase como una fuente de definiciones de beans
public class StripeConfig {
  // Posteriomente COnfigurar Servicio De Stripe
  // Inyecta el valor de la variable de entorno directamente
  @Value("${STRIPE_SECRET_KEY}")
  private String stripeSecretKey;

  // Este método se ejecutará automáticamente después de que el bean se haya inicializado
  @PostConstruct
  public void setStripeApiKey() {
    Stripe.apiKey = stripeSecretKey;
    System.out.println("Stripe API Key cargada correctamente desde .env y configurada.");
  }
}