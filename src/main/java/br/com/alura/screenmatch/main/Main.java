package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private final String API_KEY = "&apikey=3d3dd38";
    private final String URL = "https://www.omdbapi.com/?t=";
    private final DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ConverteDados converte = new ConverteDados(new ObjectMapper());
    private ConsumoAPI consumo = new ConsumoAPI();
    private Scanner sc = new Scanner(System.in);

    public void exibeMenu(){

        System.out.println("Digite o nome da série: ");
        var nomeSerie = sc.nextLine();
        var jsonSerie = consumo.obterDados(URL + nomeSerie.replace(" ", "+") + API_KEY);
        var dadosSerie = converte.obterDados(jsonSerie, DadosSerie.class);

        List<DadosTemporada> temporadas = new ArrayList<>();
        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            var jsonTemporada= consumo.obterDados(URL + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            var dadosTemporada = converte.obterDados(jsonTemporada, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        //temporadas.forEach(System.out::println);

        // With FOR
        //for (int i = 0; i < temporadas.size(); i++) {
        //    List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
        //   for (int j = 0; j < episodiosTemporada.size(); j++) {
        //       System.out.println("#" + j + ": "+ episodiosTemporada.get(j).titulo());
        //    }
        //}

        //WITH LAMBDA EXPRESSION
        temporadas.forEach(t -> t.episodios()
                .forEach(e -> System.out.println("#" + e.numeroEpisodio() + " " + e.titulo())));



        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream().map(d -> new Episodio(t.numeroTemporada(), d)))
                .collect(Collectors.toList());

        System.out.println(episodios);

       /* //Filtro busca: Episódio por Nome.
        System.out.println("Digite o nome do episódio: ");
        var trechoTitulo = sc.nextLine();
        var episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if(episodioBuscado.isPresent()){
            System.out.println("Ep. encontrado!");
            var ep = episodioBuscado.get();
            String tituloCompleto = ep.getTitulo().toUpperCase();
            int temporadaEp = ep.getTemporada();
            int numeroEp = ep.getNumeroEpisodio();
            System.out.print(String.format("TÍTULO: %s | TEMPORADA: %d | EPISÓDIO: %d", tituloCompleto, temporadaEp, numeroEp));
        }*/

        //Pegar ep com maior avaliação
        System.out.println("5 Episódios melhores avaliados!");
        episodios.stream()
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(5)
                .forEach(e -> System.out.println(
                        "Temporada:  " + e.getTemporada() + ","+
                                " Episódio: " + e.getTitulo() + "," +
                                " Data de lançamento: " + e.getDataLancamento().format(formatador)
                ));



        //Pegar a média de avaliação por temporada
        Map<Integer, Double> mediaAvaliacaoPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(mediaAvaliacaoPorTemporada);


        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println(est);


        /*System.out.println("Top 5 Episódios");
        episodios.stream()
                .sorted(Comparator.comparing(Episodio::getAvaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);*/

        /*System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = sc.nextInt();
        sc.nextLine();



        var dataBusca = LocalDate.of(ano, 1, 1);
        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        "Temporada:  " + e.getTemporada() +
                                " Episódio: " + e.getTitulo() +
                                " Data lançamento: " + e.getDataLancamento().format(formatador)
                ));*/
    }

}
