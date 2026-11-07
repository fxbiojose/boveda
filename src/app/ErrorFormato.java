package app;
import java.io.IOException;

/** Error de contenido en el archivo de fichas (fase 1). Extiende IOException para no cambiar el contrato de leer. */
public final class ErrorFormato extends IOException {
    public ErrorFormato(String mensaje) { super(mensaje); }
}
