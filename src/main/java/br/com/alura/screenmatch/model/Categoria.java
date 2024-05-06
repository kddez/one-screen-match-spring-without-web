package br.com.alura.screenmatch.model;

public enum Categoria {


    ACAO("Action", "Ação"),
    ROMANCE("Romance", "Romance"),
    COMEDIA("Comedy", "Comédia"),
    DRAMA("Drama", "Drama"),
    CRIME("Crime", "Crime"),
    TERROR("Horror", "Terror");

    private String categoriaOmdb;
    private String categoriaEmPTBR;

    Categoria(String categoriaOmdb, String categoriaEmPTBR) {
        this.categoriaOmdb = categoriaOmdb;
        this.categoriaEmPTBR = categoriaEmPTBR;
    }

    public static Categoria fromString(String text){
        for (Categoria c: Categoria.values()){
            if(c.categoriaOmdb.equalsIgnoreCase(text)){
                return c;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a String fornecida");
    }

    public static Categoria fromPortugues(String text){
        for (Categoria c: Categoria.values()){
            if(c.categoriaEmPTBR.equalsIgnoreCase(text)){
                return c;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada");
    }
}
