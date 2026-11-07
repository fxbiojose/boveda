package manual;
import dominio.Ficha;
import dominio.PistaPendiente;
import dominio.TipoFicha;
import java.util.List;

/** Fases 2 a 5 con lista, pila y colas propias. Devuelve la traza y, al final, el mensaje o el primer error. */
public final class ValidadorManual {

    public String ejecutar(List<Ficha> entrada) {
        StringBuilder t = new StringBuilder();

        // Fase 2: lista enlazada
        t.append("== Fase 2: lista enlazada ==\n");
        ListaFichas lista = new ListaFichas();
        for (Ficha f : entrada) {
            lista.agregar(f);
            t.append("[LISTA] insertar ").append(f.getId()).append(" (posicion ").append(f.getPosicion()).append(")\n");
        }
        t.append("[LISTA] recorrido:");
        boolean primera = true;
        for (Ficha f : lista) {
            t.append(primera ? " " : " -> ").append(f.getId());
            primera = false;
        }
        t.append("\n");

        // Fase 3: pila de delimitadores
        t.append("== Fase 3: pila de delimitadores ==\n");
        PilaSimbolos pila = new PilaSimbolos();
        PilaSimbolos pilaIds = new PilaSimbolos();   // misma forma que pila, guarda el id de cada apertura para reportar
        for (Ficha f : lista) {
            if (f.getTipo() == TipoFicha.ABRE) {
                pila.apilar(f.getValor());
                pilaIds.apilar(f.getId());
                t.append("[PILA] apilar ").append(f.getValor()).append(" (").append(f.getId()).append(")\n");
            } else if (f.getTipo() == TipoFicha.CIERRA) {
                if (pila.estaVacia()) {
                    t.append("[PILA] consultar tope: vacia\n");
                    return error(t, "fase 3, pila", ubicacion(f) + ": cierre " + f.getValor() + " sin apertura (la pila esta vacia)");
                }
                String tope = pila.consultar();
                t.append("[PILA] consultar tope: ").append(tope).append(" (para el cierre ").append(f.getId()).append(")\n");
                if (!tope.equals(apertura(f.getValor()))) {
                    return error(t, "fase 3, pila", ubicacion(f) + ": se esperaba " + cierre(tope) + " porque " + tope + " esta en el tope");
                }
                pila.retirar();
                pilaIds.retirar();
                t.append("[PILA] retirar ").append(tope).append("\n");
            }
        }
        if (!pila.estaVacia()) {
            Ficha sobrante = lista.buscar(pilaIds.consultar());
            return error(t, "fase 3, pila", ubicacion(sobrante) + ": apertura " + pila.consultar() + " sin cerrar al terminar");
        }
        t.append("[PILA] vacia al terminar\n");

        // Fase 4: encolar pistas por prioridad
        t.append("== Fase 4: colas de pistas ==\n");
        ColaPistas<PistaPendiente> colas = new ColaPistas<>();
        for (Ficha f : lista) {
            if (f.getTipo() == TipoFicha.PISTA) {
                PistaPendiente p = new PistaPendiente(f, f.getInstruccion(), false);
                colas.encolar(p, f.getPrioridad());
                t.append("[COLA P").append(f.getPrioridad()).append("] encolar ").append(p.etiqueta()).append("\n");
            }
        }

        // Fase 5: resolver pistas
        t.append("== Fase 5: resolver pistas ==\n");
        StringBuilder mensaje = new StringBuilder();
        ListaFichas reveladas = new ListaFichas();   // letras ya reveladas (cada una solo una vez)
        while (!colas.estaVacia()) {
            PistaPendiente p = colas.desencolar();
            int prioridad = p.getOrigen().getPrioridad();
            t.append("[COLA P").append(prioridad).append("] desencolar ").append(p.etiqueta()).append("\n");

            String[] partes = p.getInstruccion().split(":");
            String fallo = revelar(partes[1], p, lista, reveladas, mensaje, t);
            if (fallo != null) {
                return error(t, "fase 5, pistas", fallo);
            }
            if (partes[0].equals("ENCADENAR")) {
                PistaPendiente nueva = new PistaPendiente(p.getOrigen(), "REVELAR:" + partes[2], true);
                colas.encolar(nueva, prioridad);
                t.append("[COLA P").append(prioridad).append("] encolar ").append(nueva.etiqueta()).append("\n");
            }
        }
        t.append("[COLAS] vacias al terminar\n");
        t.append("MENSAJE: ").append(mensaje);
        return t.toString();
    }

    /** Busca la letra y la agrega al mensaje. Devuelve el texto del error o null si todo salio bien. */
    private String revelar(String id, PistaPendiente p, ListaFichas lista, ListaFichas reveladas,
                           StringBuilder mensaje, StringBuilder t) {
        String donde = ubicacion(p.getOrigen()) + (p.esGenerada() ? ", pista generada " + p.getInstruccion() : "");
        Ficha letra = lista.buscar(id);
        if (letra == null) {
            t.append("[LISTA] buscar ").append(id).append(" -> no existe\n");
            return donde + ": la referencia " + id + " no existe";
        }
        t.append("[LISTA] buscar ").append(id).append(" -> ").append(letra.getTipo()).append(" ").append(letra.getValor()).append("\n");
        if (letra.getTipo() != TipoFicha.LETRA) {
            return donde + ": la referencia " + id + " no es una LETRA (es " + letra.getTipo() + ")";
        }
        if (reveladas.buscar(id) != null) {
            return donde + ": la letra " + id + " ya fue revelada";
        }
        reveladas.agregar(letra);
        mensaje.append(letra.getValor());
        t.append("[MENSAJE] ").append(mensaje).append("\n");
        return null;
    }

    private String error(StringBuilder t, String fase, String texto) {
        t.append("ERROR (").append(fase).append("): ").append(texto);
        return t.toString();
    }

    private String ubicacion(Ficha f) {
        return "linea fisica " + f.getLinea() + ", ficha " + f.getId() + " (posicion " + f.getPosicion() + ")";
    }

    private String apertura(String cierre) {
        switch (cierre) {
            case ")": return "(";
            case "]": return "[";
            default: return "{";
        }
    }

    private String cierre(String apertura) {
        switch (apertura) {
            case "(": return ")";
            case "[": return "]";
            default: return "}";
        }
    }
}
