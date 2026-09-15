/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.time.LocalDate;
import java.time.Period;
/**
 *
 * @author ander
 */
public class Alumno {
    private String nombre;
    private String num_documento;
    private static String modalidad;
    private LocalDate fecha_nacimiento;

    //Sobrecarga de constructor 
    public Alumno() {
    }

    public Alumno(String nombre, String num_documento, LocalDate fecha_nacimiento) {
        this.nombre = nombre;
        setNum_documento(num_documento);
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public Alumno(String nombre, String num_documento, String modalidad, LocalDate fecha_nacimiento) {
        this.nombre = nombre;
        setNum_documento(num_documento);
        Alumno.modalidad = modalidad;
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNum_documento() {
        return num_documento;
    }

    public static String getModalidad() {
        return modalidad;
    }

    public LocalDate getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNum_documento(String num_documento) {
        if (num_documento.length() == 8) {
            this.num_documento = num_documento;
        }
    }

    public static void setModalidad(String modalidad) {
        Alumno.modalidad = modalidad;
    }

    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    int calcularEdad() {
        LocalDate ahora = LocalDate.now();

        Period periodo = Period.between(this.fecha_nacimiento, ahora);

        return periodo.getYears();
    }

    public void verDatos() {
        System.out.println("-------------------------------");
        System.out.println("Nombre: " + this.nombre);
        System.out.println("Numero de documento: " + this.num_documento);
        System.out.println("Modalidad: " + Alumno.modalidad);
        System.out.println("Fecha de nacimiento: " + this.fecha_nacimiento);
        System.out.println("Edad: " + calcularEdad());
        System.out.println("-------------------------------");
    }
}
