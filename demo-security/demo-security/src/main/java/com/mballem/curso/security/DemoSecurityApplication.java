package com.mballem.curso.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mballem.curso.security.service.EmailService;

@SpringBootApplication
public class DemoSecurityApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(DemoSecurityApplication.class, args);			
	}
	
	@Autowired
	EmailService service;	
	
	
//	@Autowired
//	JavaMailSender sender;	
//
//	@Override
//	public void run(String... args) throws Exception {
//		
//		SimpleMailMessage simple = new SimpleMailMessage();
//		simple.setTo("carlosagustoroque@gmail.com");
//		simple.setText("Teste numero 1");
//	    simple.setSubject("teste 1");	
//	    sender.send(simple);
//		
//		service.enviarPedidoDeConfirmacaoDeCadastro("carlosagustoroque@gmail.com","9852pol");
//	}
}
