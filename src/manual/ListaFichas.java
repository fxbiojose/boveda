package manual;
import dominio.Ficha;
import java.util.Iterator;

/** Lista enlazada simple propia. Mantiene cabeza y cola para insertar al final en O(1). */
public final class ListaFichas implements Iterable<Ficha> {
    private Nodo<Ficha> cabeza;
    private Nodo<Ficha> cola;
    private int tamanio;

    public void agregar(Ficha ficha) {
        if (ficha == null) {
            throw new IllegalArgumentException("ficha nula");
        }
        Nodo<Ficha> nuevo = new Nodo<>(ficha);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamanio++;
    }

    /** Busca por id recorriendo desde la cabeza; devuelve null si no existe. */
    public Ficha buscar(String id) {
        Nodo<Ficha> actual = cabeza;
        while (actual != null) {
            if (actual.dato.getId().equals(id)) {
                return actual.dato;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    public int tamanio() { return tamanio; }

    public Ficha primera() {
        if (cabeza == null) {
            throw new IllegalStateException("lista vacia");
        }
        return cabeza.dato;
    }

    /** Recorrido en orden de insercion sin exponer Nodo. */
    @Override
    public Iterator<Ficha> iterator() {
        return new Iterator<Ficha>() {
            private Nodo<Ficha> actual = cabeza;
            @Override public boolean hasNext() { return actual != null; }
            @Override public Ficha next() {
                Ficha f = actual.dato;
                actual = actual.siguiente;
                return f;
            }
        };
    }
}
