package pruebas;
import app.LectorFichas;
import biblioteca.ValidadorBiblioteca;
import dominio.Ficha;
import dominio.TipoFicha;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import manual.ColaPistas;
import manual.ListaFichas;
import manual.PilaSimbolos;
import manual.ValidadorManual;

/** Pruebas aisladas de las estructuras y pruebas con archivos para las dos versiones. Uso: java -cp out pruebas.CasosPrueba datos */
public final class CasosPrueba {
    private static int pasaron = 0;
    private static int fallaron = 0;

    public static void main(String[] args) {
        String carpeta = args.length > 0 ? args[0] : "datos";

        System.out.println("== Estructuras propias (pruebas aisladas) ==");
        pruebasLista();
        pruebasPila();
        pruebasCola();

        System.out.println("== Archivos de entrada (version manual y version java.util) ==");
        valido(carpeta, "ejemplo_valido.txt", "SOL");
        valido(carpeta, "prueba_tres_prioridades.txt", "SOL");
        valido(carpeta, "prueba_fifo_misma_prioridad.txt", "BA");
        valido(carpeta, "prueba_encadenar_cola_con_pistas.txt", "SOL");
        valido(carpeta, "prueba_sin_pistas.txt", "");
        error(carpeta, "error_cierre_incorrecto.txt", "fase 3, pila", "linea fisica 6, ficha F05 (posicion 5): se esperaba ] porque [ esta en el tope");
        error(carpeta, "error_cierre_sin_apertura.txt", "fase 3, pila", "linea fisica 3, ficha F02 (posicion 2): cierre ) sin apertura");
        error(carpeta, "error_apertura_sobrante.txt", "fase 3, pila", "linea fisica 2, ficha F01 (posicion 1): apertura ( sin cerrar");
        error(carpeta, "error_id_repetido.txt", "fase 1, lectura", "linea fisica 4, ficha F01 (posicion 3): identificador repetido");
        error(carpeta, "error_linea_incompleta.txt", "fase 1, lectura", "linea fisica 3, posicion 2: se esperaban 5 campos y hay 4");
        error(carpeta, "error_tipo_invalido.txt", "fase 1, lectura", "tipo invalido 'NUMERO'");
        error(carpeta, "error_prioridad_fuera_de_rango.txt", "fase 1, lectura", "prioridad invalida '4'");
        error(carpeta, "error_referencia_inexistente.txt", "fase 5, pistas", "la referencia F99 no existe");
        error(carpeta, "error_referencia_no_letra.txt", "fase 5, pistas", "la referencia F01 no es una LETRA");
        error(carpeta, "error_letra_repetida.txt", "fase 5, pistas", "linea fisica 4, ficha F03 (posicion 3): la letra F01 ya fue revelada");

        System.out.println();
        System.out.println("Resumen: " + pasaron + " pasaron, " + fallaron + " fallaron");
        if (fallaron > 0) {
            System.exit(1);
        }
    }

    // ---------- estructuras ----------
    private static Ficha ficha(String id, int pos) {
        return new Ficha(id, TipoFicha.LETRA, "X", 1, "-", pos, pos + 1);
    }

    private static void pruebasLista() {
        ListaFichas l = new ListaFichas();
        verificar("lista: nueva esta vacia (tamanio 0)", l.tamanio() == 0);
        verificar("lista: buscar en vacia devuelve null", l.buscar("F01") == null);
        l.agregar(ficha("F01", 1));
        l.agregar(ficha("F02", 2));
        l.agregar(ficha("F03", 3));
        verificar("lista: tamanio 3 tras tres inserciones", l.tamanio() == 3);
        verificar("lista: primera es F01", l.primera().getId().equals("F01"));
        verificar("lista: buscar F03 la encuentra", l.buscar("F03") != null);
        verificar("lista: buscar F99 devuelve null", l.buscar("F99") == null);
        StringBuilder orden = new StringBuilder();
        for (Ficha f : l) {
            orden.append(f.getId());
        }
        verificar("lista: el recorrido respeta el orden de insercion", orden.toString().equals("F01F02F03"));
        boolean lanzo = false;
        try {
            new ListaFichas().primera();
        } catch (IllegalStateException e) {
            lanzo = true;
        }
        verificar("lista: primera en vacia lanza excepcion", lanzo);
    }

    private static void pruebasPila() {
        PilaSimbolos p = new PilaSimbolos();
        verificar("pila: nueva esta vacia", p.estaVacia());
        p.apilar("(");
        p.apilar("[");
        verificar("pila: consultar devuelve el tope sin retirarlo", p.consultar().equals("[") && !p.estaVacia());
        verificar("pila: retirar respeta LIFO (primero [)", p.retirar().equals("["));
        verificar("pila: retirar respeta LIFO (luego ()", p.retirar().equals("("));
        verificar("pila: queda vacia", p.estaVacia());
        boolean lanzo = false;
        try {
            p.retirar();
        } catch (IllegalStateException e) {
            lanzo = true;
        }
        verificar("pila: retirar en vacia lanza excepcion", lanzo);
    }

    private static void pruebasCola() {
        ColaPistas<String> c = new ColaPistas<>();
        verificar("cola: nueva esta vacia", c.estaVacia());
        c.encolar("p3-a", 3);
        c.encolar("p2-a", 2);
        c.encolar("p1-a", 1);
        c.encolar("p2-b", 2);
        c.encolar("p1-b", 1);
        String salida = "";
        while (!c.estaVacia()) {
            salida += c.desencolar() + " ";
        }
        verificar("cola: sale P1, luego P2, luego P3 y FIFO dentro de cada una", salida.equals("p1-a p1-b p2-a p2-b p3-a "));
        boolean lanzo = false;
        try {
            c.encolar("x", 4);
        } catch (IllegalArgumentException e) {
            lanzo = true;
        }
        verificar("cola: prioridad fuera de 1-3 lanza excepcion", lanzo);
        lanzo = false;
        try {
            c.desencolar();
        } catch (IllegalStateException e) {
            lanzo = true;
        }
        verificar("cola: desencolar en vacia lanza excepcion", lanzo);
    }

    // ---------- archivos ----------
    /** Devuelve {informe manual, informe java.util}; si falla la lectura devuelve el error dos veces. */
    private static String[] correr(String carpeta, String archivo) throws IOException {
        List<Ficha> fichas;
        try {
            fichas = LectorFichas.leer(Paths.get(carpeta, archivo));
        } catch (app.ErrorFormato e) {
            String texto = "ERROR (fase 1, lectura): " + e.getMessage();
            return new String[] {texto, texto};
        }
        return new String[] {new ValidadorManual().ejecutar(fichas), new ValidadorBiblioteca().ejecutar(fichas)};
    }

    private static String ultimaLinea(String informe) {
        return informe.substring(informe.lastIndexOf('\n') + 1);
    }

    private static void valido(String carpeta, String archivo, String esperado) {
        try {
            String[] r = correr(carpeta, archivo);
            verificar(archivo + ": mensaje '" + esperado + "' en la version manual", ultimaLinea(r[0]).equals("MENSAJE: " + esperado));
            verificar(archivo + ": la version java.util produce la misma traza", r[0].equals(r[1]));
        } catch (IOException e) {
            verificar(archivo + ": no se pudo leer (" + e.getMessage() + ")", false);
        }
    }

    private static void error(String carpeta, String archivo, String fase, String fragmento) {
        try {
            String[] r = correr(carpeta, archivo);
            String esperado = "ERROR (" + fase + "): ";
            verificar(archivo + ": primer error en " + fase, ultimaLinea(r[0]).startsWith(esperado) && ultimaLinea(r[0]).contains(fragmento));
            verificar(archivo + ": la version java.util produce la misma traza y el mismo error", r[0].equals(r[1]));
        } catch (IOException e) {
            verificar(archivo + ": no se pudo leer (" + e.getMessage() + ")", false);
        }
    }

    private static void verificar(String nombre, boolean condicion) {
        if (condicion) {
            pasaron++;
            System.out.println("[OK]    " + nombre);
        } else {
            fallaron++;
            System.out.println("[FALLA] " + nombre);
        }
    }
}
