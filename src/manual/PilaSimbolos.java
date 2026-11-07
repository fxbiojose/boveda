package manual;

/** Pila propia (LIFO) sobre nodos enlazados. */
public final class PilaSimbolos {
    private Nodo<String> tope;

    public void apilar(String simbolo) {
        Nodo<String> nuevo = new Nodo<>(simbolo);
        nuevo.siguiente = tope;
        tope = nuevo;
    }

    public String consultar() {
        if (tope == null) {
            throw new IllegalStateException("pila vacia");
        }
        return tope.dato;
    }

    public String retirar() {
        if (tope == null) {
            throw new IllegalStateException("pila vacia");
        }
        String simbolo = tope.dato;
        tope = tope.siguiente;
        return simbolo;
    }

    public boolean estaVacia() { return tope == null; }
}
