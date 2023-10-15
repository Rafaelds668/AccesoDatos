package org.example;
import static org.example.ProcesadorArchivos.procesadorArchivos;
public class Main {
    /*
    Este es el método principal empleado para ejecutar
    el procesamiento de archivos .txt

    @param args Hace referencia a los argumentos de linea
    de comandos (no utilizados en este caso)
     */
    public static void main(String[] args) {
        procesadorArchivos("data.csv", "template.txt");
    }
}
