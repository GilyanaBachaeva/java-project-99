package hexlet.code.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) {
		// Получение переменных окружения
		String dbHost = System.getenv("DB_HOST");
		String dbPort = System.getenv("DB_PORT");
		String dbName = System.getenv("DB_NAME");
		String dbUser = System.getenv("DB_USER");
		String dbPassword = System.getenv("DB_PASSWORD");

		// Установка свойств для подключения к базе данных
		System.setProperty("DB_HOST", dbHost);
		System.setProperty("DB_PORT", dbPort);
		System.setProperty("DB_NAME", dbName);
		System.setProperty("DB_USER", dbUser);
		System.setProperty("DB_PASSWORD", dbPassword);

		SpringApplication.run(AppApplication.class, args);
	}
}
