package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.dto.DadosSerie;
import br.com.alura.screenmatch.model.dto.DadosTemporada;
import br.com.alura.screenmatch.model.entity.Episodio;
import br.com.alura.screenmatch.model.entity.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

public class MainApplication {
    private final String API_KEY = "&apikey=3d3dd38";
    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoAPI consumo = new ConsumoAPI();
    private final ConverteDados conversor = new ConverteDados(new ObjectMapper());
    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;
    private final SerieRepository serieRepository;

    public MainApplication(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void showMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """ 
                                        
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Buscar melhores séries
                    7 - Buscar por gênero
                    8 - Filtrar Séries
                    9 - Buscar episódio por trecho
                    10 - Top 5 episódio por série
                    11 - Buscar episódio a partir de uma data
                                        
                    0 - Sair
                                        
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTopSeries();
                    break;
                case 7:
                    buscarPorGenero();
                    break;
                case 8:
                    buscarPorTotalDeTemporadasEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    top5EpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodioAPartirDeUmAno();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }


    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, DadosSerie.class);
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        var serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(serie);
    }

    private void buscarEpisodioPorSerie() {

        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            var serieEncontrada = serieBusca.get();

            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            var episodios = temporadas.stream()
                    .flatMap(dt -> dt.episodios().stream().map(e -> new Episodio(dt.numeroTemporada(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);

        } else {
            System.out.println("Série com o nome: " + nomeSerie + " não encontrada.");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite um título de série: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBusca.isPresent()){
            System.out.println("Dados da série: " + serieBusca.get());
        }
        else {
            System.out.println("Série não encontrada");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Digite o nome de um ator: ");
        var ator = leitura.nextLine();
        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCase(ator);

        if (seriesEncontradas.isEmpty()) {
            System.out.println("Nenhuma série encontrada para o ator: " + ator);
        } else {
            System.out.println("Séries em que " + ator + " trabalhou:");
            seriesEncontradas.forEach(s ->
                    System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
        }

    }

    private void buscarTopSeries() {
        List<Serie> topSeries = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        topSeries.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));

    }

    private void buscarPorGenero() {

        System.out.println("Digite um gênero");
        var categoriaDigitada = leitura.nextLine();
        var categoria = Categoria.fromPortugues(categoriaDigitada);
        List<Serie> seriesPorGenero = serieRepository.findByGenero(categoria);

        System.out.println("Séries da categoria: " + categoriaDigitada);
        seriesPorGenero.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));

    }

    private void buscarPorTotalDeTemporadasEAvaliacao() {

        System.out.println("Digite o máximo de temporadas: ");
        var maxTemporadas = leitura.nextInt();
        System.out.println("Digite a avaliação mínima: ");
        var minAvaliacao = leitura.nextDouble();

        List<Serie> seriesFiltradas = serieRepository
                .findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(maxTemporadas, minAvaliacao);

        System.out.println("Séries filtradas: ");
        seriesFiltradas.forEach(s ->
                System.out.println(s.getTitulo() + ", avaliação: " + s.getAvaliacao() + ", total de temporadas: " + s.getTotalTemporadas()));

    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite um trecho do episodio: ");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodios = serieRepository.episodiosPorTrecho(trechoEpisodio);
        episodios.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void top5EpisodiosPorSerie(){

        buscarSeriePorTitulo();

        if(serieBusca.isPresent()){
           Serie serie = serieBusca.get();
           List<Episodio> episodios = serieRepository.findTop5EpisodiosBySerie(serie);
           episodios.forEach(System.out::println);
        }

    }

    private void buscarEpisodioAPartirDeUmAno(){

        buscarSeriePorTitulo();

        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            System.out.println("Digite a partir de que ano deseja filtrar os episódios: ");
            var ano = leitura.nextInt();
            List<Episodio> episodios = serieRepository.findEpisodioByAno(serie, ano);
            episodios.forEach(System.out::println);

        }

    }


}
