# Boveda de fichas (Java)

Programa que lee un archivo de fichas, comprueba que los delimitadores esten bien anidados y descifra un mensaje a partir de pistas. Hay dos implementaciones equivalentes: una con estructuras propias (`manual`) y otra con `java.util` (`biblioteca`).

## Compilar y ejecutar

Desde la carpeta del proyecto:

    javac -encoding UTF-8 -d out $(find src -name "*.java")
    java -cp out app.Main datos/ejemplo_valido.txt
    java -cp out pruebas.CasosPrueba datos

En Windows (CMD), como las rutas pueden tener espacios, se compila por paquetes con rutas relativas:

    javac -encoding UTF-8 -d out src\dominio\*.java src\app\*.java src\manual\*.java src\biblioteca\*.java src\pruebas\*.java

Compilacion por separado de cada version (ambas solo dependen de `dominio`):

    javac -encoding UTF-8 -d out_manual src/dominio/*.java src/manual/*.java
    javac -encoding UTF-8 -d out_biblioteca src/dominio/*.java src/biblioteca/*.java

## Estructura

- `src/dominio`: `Ficha`, `TipoFicha` y `PistaPendiente` (una pista esperando en las colas, sea del archivo o generada por ENCADENAR).
- `src/app`: `LectorFichas` (fase 1: lee y valida el archivo), `ErrorFormato` y `Main`.
- `src/manual`: `Nodo`, `ListaFichas`, `PilaSimbolos`, `ColaPistas` y `ValidadorManual` (fases 2 a 5).
- `src/biblioteca`: `ValidadorBiblioteca`, las mismas fases con `LinkedList`, `ArrayDeque`, `HashMap` y `HashSet`.
- `src/pruebas`: `CasosPrueba`, pruebas aisladas de las tres estructuras y pruebas con archivos para ambas versiones.
- `datos`: archivos validos (`ejemplo_valido`, `prueba_*`) e invalidos (`error_*`).
- `salidas`: salida textual de cada ejecucion.
- `Informe_Boveda_Fabio_Mercado.docx`: informe del trabajo.

## Que hace cada clase

- `LectorFichas.leer`: comprueba el encabezado, que cada fila tenga cinco campos, que el tipo, el valor, la prioridad y la instruccion sean validos, y que los id no se repitan. Guarda la linea fisica y la posicion de cada ficha. Informa solo el primer error.
- `ListaFichas`: lista enlazada simple con cabeza y cola. `agregar` inserta al final, `buscar` recorre por id, `primera` devuelve la cabeza y se puede recorrer con for-each sin exponer `Nodo`.
- `PilaSimbolos`: pila LIFO sobre nodos (`apilar`, `consultar`, `retirar`, `estaVacia`).
- `ColaPistas`: tres colas FIFO enlazadas, una por prioridad. `desencolar` siempre toma la de menor numero que tenga elementos.
- `ValidadorManual` y `ValidadorBiblioteca`: fase 2 (lista y recorrido), fase 3 (pila de delimitadores), fase 4 (encolar pistas), fase 5 (resolver pistas y armar el mensaje). Devuelven la traza y, en la ultima linea, `MENSAJE: ...` o `ERROR (...)`.

## Salida

La ultima linea del informe de cada version es el mensaje o el primer error. `Main` ejecuta las dos versiones con el mismo archivo y compara esa linea y la traza completa.
