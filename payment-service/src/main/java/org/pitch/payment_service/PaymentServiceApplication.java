package org.pitch.payment_service;

import org.springframework.context.ApplicationContext;
import org.pitch.payment_service.service.PaymentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(PaymentServiceApplication.class, args);

		PaymentService paymentService = context.getBean(PaymentService.class);

		String clientPriceId = "price_1RgudRGgnkDsQzovev5Laf4u";
		System.out.println( paymentService.registerUserAndSubscribe( "Jose", "jose_123@email.com", clientPriceId ));
	}
}
