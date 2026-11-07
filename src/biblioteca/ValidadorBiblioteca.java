package biblioteca;
import dominio.Ficha;
import dominio.PistaPendiente;
import dominio.TipoFicha;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/** Mismas fases y mismas reglas que ValidadorManual, pero con estructuras de java.util. */
public final class ValidadorBiblioteca {

    public String ejecutar(List<Ficha> entrada) {
        StringBuilder t = new StringBuilder();

        // Fase 2: lista enlazada (LinkedList) y un indice por id para las busquedas
        t.append("== Fase 2: lista enlazada ==\n");
        LinkedList<Ficha> lista = new LinkedList<>();
        Map<String, Ficha> porId = new HashMap<>();
        for (Ficha f : entrada) {
            lista.add(f);
            porId.put(f.getId(), f);
            t.append("[LISTA] insertar ").append(f.getId()).append(" (posicion ").append(f.getPosicion()).append(")\n");
        }
        t.append("[LISTA] recorrido:");
        boolean primera = true;
        for (Ficha f : lista) {
            t.append(primera ? " " : " -> ").append(f.getId());
            primera = false;
        }
        t.append("\n");

        // Fase 3: pila de delimitadores (ArrayDeque usado como pila)
        t.append("== Fase 3: pila de delimitadores ==\n");
        Deque<Ficha> pila = new ArrayDeque<>();
        for (Ficha f : lista) {
            if (f.getTipo() == TipoFicha.ABRE) {
                pila.push(f);
                t.append("[PILA] apilar ").append(f.getValor()).append(" (").append(f.getId()).append(")\n");
            } else if (f.getTipo() == TipoFicha.CIERRA) {
                if (pila.isEmpty()) {
                    t.append("[PILA] consultar tope: vacia\n");
                    return error(t, "fase 3, pila", ubicacion(f) + ": cierre " + f.getValor() + " sin apertura (la pila esta vacia)");
                }
                String tope = pila.peek().getValor();
                t.append("[PILA] consultar tope: ").append(tope).append(" (para el cierre ").append(f.getId()).append(")\n");
                if (!tope.equals(apertura(f.getValor()))) {
                    return error(t, "fase 3, pila", ubicacion(f) + ": se esperaba " + cierre(tope) + " porque " + tope + " esta en el tope");
                }
                pila.pop();
                t.append("[PILA] retirar ").append(tope).append("\n");
            }
        }
        if (!pila.isEmpty()) {
            Ficha sobrante = pila.peek();
            return error(t, "fase 3, pila", ubicacion(sobrante) + ": apertura " + sobrante.getValor() + " sin cerrar al terminar");
        }
        t.append("[PILA] vacia al terminar\n");

        // Fase 4: una cola FIFO por prioridad
        t.append("== Fase 4: colas de pistas ==\n");
        List<Queue<PistaPendiente>> colas = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            colas.add(new ArrayDeque<PistaPendiente>());
        }
        for (Ficha f : lista) {
            if (f.getTipo() == TipoFicha.PISTA) {
                PistaPendiente p = new PistaPendiente(f, f.getInstruccion(), false);
                colas.get(f.getPrioridad() - 1).add(p);
                t.append("[COLA P").append(f.getPrioridad()).append("] encolar ").append(p.etiqueta()).append("\n");
            }
        }

        // Fase 5: resolver pistas
        t.append("== Fase 5: resolver pistas ==\n");
        StringBuilder mensaje = new StringBuilder();
        Set<String> reveladas = new HashSet<>();
        PistaPendiente p;
        while ((p = siguiente(colas)) != null) {
            int prioridad = p.getOrigen().getPrioridad();
            t.append("[COLA P").append(prioridad).append("] desencolar ").append(p.etiqueta()).append("\n");

            String[] partes = p.getInstruccion().split(":");
            String fallo = revelar(partes[1], p, porId, reveladas, mensaje, t);
            if (fallo != null) {
                return error(t, "fase 5, pistas", fallo);
            }
            if (partes[0].equals("ENCADENAR")) {
                PistaPendiente nueva = new PistaPendiente(p.getOrigen(), "REVELAR:" + partes[2], true);
                colas.get(prioridad - 1).add(nueva);
                t.append("[COLA P").append(prioridad).append("] encolar ").append(nueva.etiqueta()).append("\n");
            }
        }
        t.append("[COLAS] vacias al terminar\n");
        t.append("MENSAJE: ").append(mensaje);
        return t.toString();
    }

    /** Saca de la primera cola no vacia (P1, luego P2, luego P3); null si todas estan vacias. */
    private PistaPendiente siguiente(List<Queue<PistaPendiente>> colas) {
        for (Queue<PistaPendiente> cola : colas) {
            if (!cola.isEmpty()) {
                return cola.poll();
            }
        }
        return null;
    }

    private String revelar(String id, PistaPendiente p, Map<String, Ficha> porId, Set<String> reveladas,
                           StringBuilder mensaje, StringBuilder t) {
        String donde = ubicacion(p.getOrigen()) + (p.esGenerada() ? ", pista generada " + p.getInstruccion() : "");
        Ficha letra = porId.get(id);
        if (letra == null) {
            t.append("[LISTA] buscar ").append(id).append(" -> no existe\n");
            return donde + ": la referencia " + id + " no existe";
        }
        t.append("[LISTA] buscar ").append(id).append(" -> ").append(letra.getTipo()).append(" ").append(letra.getValor()).append("\n");
        if (letra.getTipo() != TipoFicha.LETRA) {
            return donde + ": la referencia " + id + " no es una LETRA (es " + letra.getTipo() + ")";
        }
        if (!reveladas.add(id)) {
            return donde + ": la letra " + id + " ya fue revelada";
        }
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
