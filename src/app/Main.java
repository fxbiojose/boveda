package app;
import biblioteca.ValidadorBiblioteca;
import dominio.Ficha;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import manual.ValidadorManual;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -cp out app.Main <archivo.txt>");
            return;
        }

        List<Ficha> fichas;
        try {
            fichas = LectorFichas.leer(Paths.get(args[0]));
        } catch (ErrorFormato e) {
            System.out.println("ERROR (fase 1, lectura): " + e.getMessage());
            System.out.println("No se ejecutan las fases siguientes.");
            return;
        } catch (IOException e) {
            System.out.println("ERROR: no se pudo leer el archivo: " + e.getMessage());
            return;
        }

        System.out.println("== Fase 1: lectura y validacion ==");
        for (Ficha f : fichas) {
            System.out.println("[LECTURA] " + f + " (linea fisica " + f.getLinea() + ")");
        }
        System.out.println("[LECTURA] " + fichas.size() + " fichas validas");

        String manual = new ValidadorManual().ejecutar(fichas);
        String biblioteca = new ValidadorBiblioteca().ejecutar(fichas);

        System.out.println();
        System.out.println("######## VERSION MANUAL ########");
        System.out.println(manual);
        System.out.println();
        System.out.println("######## VERSION JAVA.UTIL ########");
        System.out.println(biblioteca);
        System.out.println();

        boolean mismoResultado = ultimaLinea(manual).equals(ultimaLinea(biblioteca));
        boolean mismaTraza = manual.equals(biblioteca);
        System.out.println("######## COMPARACION ########");
        System.out.println("Mensaje o primer error: " + (mismoResultado ? "IGUAL" : "DIFERENTE"));
        System.out.println("Orden de eventos (traza completa): " + (mismaTraza ? "IGUAL" : "DIFERENTE"));
    }

    private static String ultimaLinea(String informe) {
        return informe.substring(informe.lastIndexOf('\n') + 1);
    }
}
