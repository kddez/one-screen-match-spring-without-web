package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		ConsumoAPI api = new ConsumoAPI();
		var json = api.obterDados("https://www.omdbapi.com/?t=gilmore+girls&apikey=3d3dd38");

		ConverteDados conversor = new ConverteDados(new ObjectMapper());

		var dadosConvertidos = conversor.obterDados(json, DadosSerie.class);

		System.out.println(dadosConvertidos);

	}
}
