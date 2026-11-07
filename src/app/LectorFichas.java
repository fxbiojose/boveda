package app;
import dominio.Ficha;
import dominio.TipoFicha;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Fase 1: lee el archivo, valida cada fila y devuelve las fichas en orden. Informa el primer error. */
public final class LectorFichas {
    private static final String ENCABEZADO = "id|tipo|valor|prioridad|instruccion";
    private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9_-]+");

    private LectorFichas() {}

    public static List<Ficha> leer(Path ruta) throws IOException {
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        if (lineas.isEmpty()) {
            throw new ErrorFormato("archivo vacio: falta el encabezado");
        }
        String encabezado = lineas.get(0);
        if (encabezado.startsWith("\uFEFF")) {          // BOM que algunos editores agregan
            encabezado = encabezado.substring(1);
        }
        if (!encabezado.trim().equals(ENCABEZADO)) {
            throw new ErrorFormato("linea fisica 1: el encabezado debe ser " + ENCABEZADO);
        }

        List<Ficha> fichas = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        int posicion = 0;
        for (int i = 1; i < lineas.size(); i++) {
            String texto = lineas.get(i);
            if (texto.trim().isEmpty()) {
                continue;                                 // la linea en blanco cuenta como linea fisica pero no como ficha
            }
            posicion++;
            fichas.add(interpretar(texto, i + 1, posicion, ids));
        }
        return fichas;
    }

    private static Ficha interpretar(String texto, int linea, int posicion, Set<String> ids) throws ErrorFormato {
        String[] c = texto.split("\\|", -1);
        String ubicacionSinId = "linea fisica " + linea + ", posicion " + posicion;
        if (c.length != 5) {
            throw new ErrorFormato(ubicacionSinId + ": se esperaban 5 campos y hay " + c.length);
        }

        String id = c[0];
        if (!ID_VALIDO.matcher(id).matches()) {
            throw new ErrorFormato(ubicacionSinId + ": identificador vacio o con caracteres no permitidos");
        }
        String ubicacion = "linea fisica " + linea + ", ficha " + id + " (posicion " + posicion + ")";
        if (!ids.add(id)) {
            throw new ErrorFormato(ubicacion + ": identificador repetido");
        }

        TipoFicha tipo;
        try {
            tipo = TipoFicha.valueOf(c[1]);
        } catch (IllegalArgumentException e) {
            throw new ErrorFormato(ubicacion + ": tipo invalido '" + c[1] + "' (debe ser ABRE, CIERRA, LETRA o PISTA)");
        }

        String valor = c[2];
        switch (tipo) {
            case ABRE:
                if (!(valor.length() == 1 && "([{".indexOf(valor.charAt(0)) >= 0)) {
                    throw new ErrorFormato(ubicacion + ": valor invalido '" + valor + "' (ABRE requiere ( [ o {)");
                }
                break;
            case CIERRA:
                if (!(valor.length() == 1 && ")]}".indexOf(valor.charAt(0)) >= 0)) {
                    throw new ErrorFormato(ubicacion + ": valor invalido '" + valor + "' (CIERRA requiere ) ] o })");
                }
                break;
            case LETRA:
                if (!(valor.length() == 1 && Character.isLetter(valor.charAt(0)))) {
                    throw new ErrorFormato(ubicacion + ": valor invalido '" + valor + "' (LETRA requiere una sola letra)");
                }
                break;
            default: // PISTA
                if (!valor.equals("-")) {
                    throw new ErrorFormato(ubicacion + ": valor invalido '" + valor + "' (PISTA requiere -)");
                }
        }

        int prioridad;
        if (c[3].equals("1")) prioridad = 1;
        else if (c[3].equals("2")) prioridad = 2;
        else if (c[3].equals("3")) prioridad = 3;
        else throw new ErrorFormato(ubicacion + ": prioridad invalida '" + c[3] + "' (debe ser 1, 2 o 3)");

        String instruccion = c[4];
        if (tipo == TipoFicha.PISTA) {
            if (!instruccionValida(instruccion)) {
                throw new ErrorFormato(ubicacion + ": instruccion invalida '" + instruccion
                        + "' (debe ser REVELAR:id o ENCADENAR:id1:id2)");
            }
        } else if (!instruccion.equals("-")) {
            throw new ErrorFormato(ubicacion + ": instruccion invalida '" + instruccion + "' (" + tipo + " requiere -)");
        }

        return new Ficha(id, tipo, valor, prioridad, instruccion, posicion, linea);
    }

    private static boolean instruccionValida(String s) {
        String[] p = s.split(":", -1);
        if (p.length == 2 && p[0].equals("REVELAR")) {
            return ID_VALIDO.matcher(p[1]).matches();
        }
        if (p.length == 3 && p[0].equals("ENCADENAR")) {
            return ID_VALIDO.matcher(p[1]).matches() && ID_VALIDO.matcher(p[2]).matches();
        }
        return false;
    }
}
