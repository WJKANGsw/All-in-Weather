package com.spring;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
@EnableScheduling
public class WeatherAiApplication {

	@Value("${firebase.path}")
	private String path;

	public static void main(String[] args) {
		SpringApplication.run(WeatherAiApplication.class, args);
	}

	@Bean
	public CommandLineRunner initFirebase() {
		return (args) -> {
			ClassPathResource resource = new ClassPathResource(path);
			try (InputStream is = resource.getInputStream()) {
				FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(is))
						.build();

				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
					System.out.println("Firebase application has been initialized");
				} else {
					System.out.println("Firebase application already initialized");
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
		};
	}

}
