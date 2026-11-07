package manual;

/**
 * Tres colas FIFO enlazadas, una por prioridad (1, 2 y 3).
 * desencolar() siempre atiende la cola no vacia de menor numero.
 */
public final class ColaPistas<T> {
    private static final class Fila<T> {
        Nodo<T> frente;
        Nodo<T> fin;

        void encolar(T dato) {
            Nodo<T> nuevo = new Nodo<>(dato);
            if (fin == null) {
                frente = nuevo;
            } else {
                fin.siguiente = nuevo;
            }
            fin = nuevo;
        }

        T desencolar() {
            T dato = frente.dato;
            frente = frente.siguiente;
            if (frente == null) {
                fin = null;
            }
            return dato;
        }

        boolean vacia() { return frente == null; }
    }

    private final Fila<T> p1 = new Fila<>();
    private final Fila<T> p2 = new Fila<>();
    private final Fila<T> p3 = new Fila<>();

    private Fila<T> fila(int prioridad) {
        switch (prioridad) {
            case 1: return p1;
            case 2: return p2;
            case 3: return p3;
            default: throw new IllegalArgumentException("prioridad fuera de 1-3: " + prioridad);
        }
    }

    public void encolar(T pista, int prioridad) {
        fila(prioridad).encolar(pista);
    }

    public T desencolar() {
        if (!p1.vacia()) return p1.desencolar();
        if (!p2.vacia()) return p2.desencolar();
        if (!p3.vacia()) return p3.desencolar();
        throw new IllegalStateException("colas vacias");
    }

    public boolean estaVacia() {
        return p1.vacia() && p2.vacia() && p3.vacia();
    }
}
