package dominio;

/**
 * Una pista esperando turno en las colas. Puede venir de una ficha PISTA del archivo
 * o haber sido generada por un ENCADENAR (en ese caso conserva la ficha de origen).
 */
public final class PistaPendiente {
    private final Ficha origen;
    private final String instruccion;
    private final boolean generada;

    public PistaPendiente(Ficha origen, String instruccion, boolean generada) {
        this.origen = origen;
        this.instruccion = instruccion;
        this.generada = generada;
    }

    public Ficha getOrigen() { return origen; }
    public String getInstruccion() { return instruccion; }
    public boolean esGenerada() { return generada; }

    /** Texto usado en la traza y en los errores. */
    public String etiqueta() {
        if (generada) {
            return instruccion + " (generada por " + origen.getId() + ")";
        }
        return origen.getId() + " " + instruccion;
    }
}
