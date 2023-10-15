package org.example;

import java.io.*;
import java.util.ArrayList;

/**
 * Clase que contiene un procesador de archivos CSV para generar archivos de correo de bienvenida.
 */

public class ProcesadorArchivos {

    /**
     * Procesa el archivo CSV y genera archivos de correo de bienvenida en la carpeta de salida.
     *
     * @param archivoCsv      Ruta al archivo CSV de entrada.
     * @param archivoPlantilla Ruta al archivo de plantilla para los correos de bienvenida.
     */
    public static void procesadorArchivos(String archivoCsv, String archivoPlantilla) {
        File archivo = new File(archivoCsv);
        boolean carpetaCreada = false;
        crearCarpetaSalida(); // Llamar a la creación de la carpeta fuera del bucle

        try (BufferedReader brArchivo = new BufferedReader(new FileReader(archivo))) {
            String lecturaArchivo;
            int linea = 0;
            while ((lecturaArchivo = brArchivo.readLine()) != null) {
                linea++;
                procesarLinea(lecturaArchivo, linea, archivoPlantilla, carpetaCreada);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de obtención de datos " + e);
        }

        imprimirArchivosCorreoBienvenida();
    }


    /**
     * Procesa una línea del archivo CSV y realiza operaciones según los datos.
     *
     * @param linea            Línea del archivo CSV.
     * @param numeroLinea      Número de la línea procesada.
     * @param archivoPlantilla Ruta al archivo de plantilla para los correos de bienvenida.
     * @param carpetaCreada    Indica si la carpeta de salida ya ha sido creada.
     */

    private static void procesarLinea(String linea, int numeroLinea, String archivoPlantilla, boolean carpetaCreada) {
        boolean todosDatos = true; // Variable para controlar si todos los datos están completos
        String[] datosArchivo = linea.split(",");
        ArrayList<String> elementosFaltantes = new ArrayList<>();

        if (datosArchivo.length >= 5) {
            String id = datosArchivo[0];
            String empresa = datosArchivo[1];
            String ciudad = datosArchivo[2];
            String email = datosArchivo[3];
            String empleado = datosArchivo[4];

            if (id.isEmpty()) elementosFaltantes.add("id");
            if (empresa.isEmpty()) elementosFaltantes.add("empresa");
            if (ciudad.isEmpty()) elementosFaltantes.add("ciudad");
            if (email.isEmpty()) elementosFaltantes.add("email");
            if (empleado.isEmpty()) elementosFaltantes.add("empleado");

            if (elementosFaltantes.isEmpty()) {
                ArrayList<String> plantillas = cargarPlantillas(archivoPlantilla, ciudad, email, empresa, empleado, todosDatos);

                if (todosDatos) {
                    escribirCorreoBienvenida(id, plantillas);
                } else {
                    imprimirErrorDatosFaltantes(numeroLinea, elementosFaltantes);
                }
            } else {
                imprimirErrorDatosFaltantes(numeroLinea, elementosFaltantes);
            }
        } else {
            imprimirErrorDatosFaltantes(numeroLinea, null);
        }
    }


    /**
     * Carga las plantillas de correo reemplazando datos específicos.
     *
     * @param archivoPlantilla Ruta al archivo de plantilla para los correos de bienvenida.
     * @param ciudad           Ciudad para reemplazar en las plantillas.
     * @param email            Correo electrónico para reemplazar en las plantillas.
     * @param empresa          Empresa para reemplazar en las plantillas.
     * @param empleado         Nombre del empleado para reemplazar en las plantillas.
     * @param todosDatos       Indica si todos los datos están completos.
     * @return Lista de plantillas de correo.
     */

    private static ArrayList<String> cargarPlantillas(String archivoPlantilla, String ciudad, String email, String empresa, String empleado, boolean todosDatos) {
        ArrayList<String> plantillas = new ArrayList<>();
        try (BufferedReader brPlantilla = new BufferedReader(new FileReader(archivoPlantilla))) {
            String lecturaPlantilla;
            while ((lecturaPlantilla = brPlantilla.readLine()) != null) {
                lecturaPlantilla = lecturaPlantilla.replace("%%2%%", ciudad);
                lecturaPlantilla = lecturaPlantilla.replace("%%3%%", email);
                lecturaPlantilla = lecturaPlantilla.replace("%%4%%", empresa);
                lecturaPlantilla = lecturaPlantilla.replace("%%5%%", empleado);
                plantillas.add(lecturaPlantilla + "\n");

                if (lecturaPlantilla.contains("%%")) {
                    todosDatos = false; // Si hay cosas sin reemplazar, establece todosDatos a false
                    break; // Sale del bucle si falta algo
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de la plantilla " + e);
        }
        return plantillas;
    }

    /**
     * Crea la carpeta de salida si no existe.
     */

    private static void crearCarpetaSalida() {
        File salida = new File("salida");
        salida.mkdir();
        System.out.println("Se ha creado correctamente la carpeta salida:");
    }

    /**
     * Escribe un archivo de correo de bienvenida.
     *
     * @param id         Identificador para el nombre del archivo.
     * @param plantillas Lista de plantillas de correo.
     */


    private static void escribirCorreoBienvenida(String id, ArrayList<String> plantillas) {
        try (BufferedWriter correoBienvenida = new BufferedWriter(new FileWriter("salida/correoBienvenida-" + id + ".txt"))) {
            for (String nuevaPlantilla : plantillas) {
                correoBienvenida.write(nuevaPlantilla);
            }
            System.out.println("Se ha creado correctamente el correoBienvenida-" + id + ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el archivo de salida " + e);
        }
    }

    /**
     * Imprime un mensaje de error si faltan datos en una línea del archivo CSV.
     *
     * @param numeroLinea      Número de la línea con datos faltantes.
     * @param elementosFaltantes Lista de elementos faltantes.
     */

    private static void imprimirErrorDatosFaltantes(int numeroLinea, ArrayList<String> elementosFaltantes) {
        String mensajeError = "Error: Faltan datos en la línea " + numeroLinea;
        if (elementosFaltantes != null) {
            mensajeError += ", falta: " + String.join(", ", elementosFaltantes);
        }
        System.err.println(mensajeError + " en el archivo data.csv");
    }

    /**
     * Imprime el contenido de los archivos de correo de bienvenida en la consola.
     */

    private static void imprimirArchivosCorreoBienvenida() {
        File salida = new File("salida");
        if (salida.exists() && salida.isDirectory()) {
            File[] archivosCorreoBienvenida = salida.listFiles((dir, name) -> name.startsWith("correoBienvenida-") && name.endsWith(".txt"));
            /*for (File archivoCorreo : archivosCorreoBienvenida) {
                 try (BufferedReader brCorreo = new BufferedReader(new FileReader(archivoCorreo))) {
                   String linea;
                    while ((linea = brCorreo.readLine()) != null) {
                        System.out.println(linea);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error al leer el archivo de salida " + e);
                }
            }*/
        }
    }


}
