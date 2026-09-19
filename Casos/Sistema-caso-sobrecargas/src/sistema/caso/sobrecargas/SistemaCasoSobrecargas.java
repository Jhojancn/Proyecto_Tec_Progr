/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sistema.caso.sobrecargas;


import Controller.AlumnoController;
import Model.Alumno;
import java.util.Scanner;
import java.time.LocalDate;
/**
 *
 * @author ander
 */
public class SistemaCasoSobrecargas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("=====ALUMNOS=====");

    AlumnoController controlador = new AlumnoController();
    Scanner scanner = new Scanner(System.in);

    Alumno.setModalidad("Presencial");
    Alumno alum1 = new Alumno("Lucas", "12345678", LocalDate.of(2006, 10, 25));
    Alumno alum2 = new Alumno("Juan", "54628691", LocalDate.of(2008, 10, 25));
    Alumno alum3 = new Alumno("Pedro", "54739474", "Virtual", LocalDate.of(2009, 10, 25));
    Alumno alum4 = new Alumno("Maria", "87634893", "Presencial", LocalDate.of(2006, 8, 25));

    controlador.agregarEstudiante(alum1);
    controlador.agregarEstudiante(alum2);
    controlador.agregarEstudiante(alum3);
    controlador.agregarEstudiante(alum4);

    int opcion = 0;

    do{
        limpiarConsola();
        System.out.println("Opciones: ");
        System.out.println("1. Establecer modalidad \t 2. Agregar estudiante nuevo \t 3. Mostrar estudiantes agregados hasta el momento \t 0. Salir");
        opcion = scanner.nextInt();
        Menu(opcion, controlador);
    }while (opcion != 0);
    }
    
    public static void limpiarConsola() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
    }
    
    public static void Menu(int opcion, AlumnoController controlador){
    Alumno generico = new Alumno();
    Scanner scanner = new Scanner(System.in);
    switch (opcion) {
        case 1:
            System.out.println("Indique la modalidad de los estudiantes: ");
            String modalidad = scanner.nextLine();
            Alumno.setModalidad(modalidad);
        case 2:
            agregarEstudiante(generico, controlador);
        case 3:
            System.out.println("Mostrando todos los estudiantes agregados hasta el momento: ");
            controlador.listarEstudiantes();
    }
    }
    
    public static void agregarEstudiante(Alumno generico, AlumnoController c){
    System.out.println("Ingrese los datos de el estudiante a agregar: ");
    System.out.printf("Nombre: ");
    Scanner scanner = new Scanner(System.in);
    String nombre = scanner.nextLine();
    generico.setNombre(nombre);
    System.out.printf("DNI: ");
    String numero_documento = scanner.nextLine();
    while (numero_documento.length() != 8){
        System.out.println("Error, ingrese un DNI valido: ");
        numero_documento = scanner.nextLine();
    }
    generico.setNum_documento(numero_documento);
    System.out.printf("Fecha nacimiento: ");
    System.out.println("Año: ");
    int año = scanner.nextInt();
    System.out.println("Mes: ");
    int mes = scanner.nextInt();
    System.out.println("Dia: ");
    int dia = scanner.nextInt();
    LocalDate fecha = LocalDate.of(año, mes, dia);
    generico.setFecha_nacimiento(fecha);
    c.agregarEstudiante(generico);
    }
}
