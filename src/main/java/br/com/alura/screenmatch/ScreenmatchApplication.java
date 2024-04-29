package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		ConsumoAPI api = new ConsumoAPI();
		ConverteDados conversor = new ConverteDados(new ObjectMapper());

		//Dados Série
		var jsonSerie = api.obterDados("https://www.omdbapi.com/?t=gilmore+girls&apikey=3d3dd38");
		var dadosSerieConvertidos = conversor.obterDados(jsonSerie, DadosSerie.class);
		System.out.println("Serie: " + dadosSerieConvertidos);

		//Dados Episódio
		var jsonEpisode = api.obterDados("https://www.omdbapi.com/?t=gilmore+girls&season=1&episode=5&apikey=3d3dd38");
		var dadosEpisodeConvertidos = conversor.obterDados(jsonEpisode, DadosEpisodio.class);
		System.out.println("Episode: " + dadosEpisodeConvertidos);

		List<DadosTemporada> temporadas = new ArrayList<>();

		//Dados Temporada
		for (int i = 1; i <= dadosSerieConvertidos.totalTemporadas(); i++) {
			var jsonTemporada= api.obterDados("https://www.omdbapi.com/?t=gilmore+girls&season=" + i + "&apikey=3d3dd38");
			var dadosTemporada = conversor.obterDados(jsonTemporada, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}
		temporadas.forEach(System.out::println);



	}
}
