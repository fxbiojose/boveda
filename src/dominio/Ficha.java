package dominio;
public final class Ficha {
    private final String id;
    private final TipoFicha tipo;
    private final String valor;
    private final int prioridad;
    private final String instruccion;
    private final int posicion;
    private final int linea;
    public Ficha(String id, TipoFicha tipo, String valor, int prioridad,
                 String instruccion, int posicion, int linea) {
        this.id=id; this.tipo=tipo; this.valor=valor; this.prioridad=prioridad;
        this.instruccion=instruccion; this.posicion=posicion; this.linea=linea;
    }
    public String getId(){return id;}
    public TipoFicha getTipo(){return tipo;}
    public String getValor(){return valor;}
    public int getPrioridad(){return prioridad;}
    public String getInstruccion(){return instruccion;}
    public int getPosicion(){return posicion;}
    public int getLinea(){return linea;}
    @Override public String toString(){return posicion+" "+id+" "+tipo+" "+valor+" P"+prioridad+" "+instruccion;}
}
