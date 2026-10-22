/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.manejo.archivos;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author PC
 */
public class ManejoArchivos {

    public static void main(String[] args) {
       try {
            // Escribir sin borrar el contenido anterior
            FileWriter escribir = new FileWriter("datos.txt", true);
            escribir.write("\nHola");
            escribir.close();

            // Leer el archivo
            FileReader leer = new FileReader("datos.txt");
            int caracter;

            while ((caracter = leer.read()) != -1) {
                System.out.print((char) caracter);
            }

            leer.close();

        } catch (IOException e) {
            System.out.println("Error al manejar el archivo.");
        }
    }
}
