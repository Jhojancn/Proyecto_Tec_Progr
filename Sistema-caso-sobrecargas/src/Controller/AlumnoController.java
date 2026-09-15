/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Model.Alumno;
import java.util.ArrayList;
/**
 *
 * @author ander
 */
public class AlumnoController {
    public ArrayList<Alumno> lista = new ArrayList<>();

    //Sobrecarga del metodo agregarEstudiante
    public void agregarEstudiante(Alumno alumno){
        lista.add(alumno);
    }

    public void agregarEstudiante(String nombre, String num_documento, String modalidad, java.time.LocalDate fecha_nacimiento){
        Alumno alumno = new Alumno(nombre, num_documento, modalidad, fecha_nacimiento);
        lista.add(alumno);
    }

    public void listarEstudiantes(){
        for (int i = 0; i < lista.size(); i++){
            lista.get(i).verDatos();
        }
    }
}
