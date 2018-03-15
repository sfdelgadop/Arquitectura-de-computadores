import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Assembler {

    private HashMap<String, String> labels;

    private HashMap<String, String> var;

    private int memory_counter;

    /*
    * NOTAS:
    * No pueden haber nombres repetidos entre etiquetas y variables
    * Cada linea puede tener hasta 1 etiqueta
    * La seccion .data va estrictamente al final
    * Todas las variables hay que inializarlas (asi sea en 0)
    * Todos los operadores deben estar llenos
    * Ninguna variable ni label debe empezar por r (Mayus o Minus)
    * Para labels y variables hay case sensitive
    * Los valores de los arreglos estan separados por ' '
     */

    public static void main(String [] arg) throws IOException{

        Assembler assembler = new Assembler();

        ArrayList<String[]> machineCode = new ArrayList<>(255);

        // Indices de los LDI que no tienen direccion en la 1ra pasada
        ArrayList<Integer> faltantes = new ArrayList<>();

        Reader r = new Reader("C:\\\\Bolt/read.txt");
        BufferedReader reader = r.getBuffer();

        String line = reader.readLine();
        String[] command;
        String[] previo;
        String[] valores; //Para los arrays

        boolean var = false; //False => .code, True => .data

        /*
        * Primera Pasada
         */
        while (line != null){
            if(!var){ //Seguimos en .code
                previo = new String[2];
                /*
                * previo separa el codigo de maquina en 2
                * 0: comando (ISA)
                * 1: operandos (Ra, Rb)
                 */
                command = line.split( "\t");
                /*
                * El formato de cada comando es:
                * 0: etiqueta
                * 1: comando (ISA)
                * 2: Operando 1
                * 3: Operando 2
                 */
                if(!command[0].equals("")){ // Se agrega la etiqueta si existe
                    assembler.labels.put(
                            command[0], Integer.toString(assembler.memory_counter, 16).toUpperCase());
                }

                previo[0] = ISA.get_ISA(command[1].toUpperCase());

                if(command[1].toUpperCase().equals("LDI")){ // LDI tiene codificacion en 2 espacios de memoria
                    previo[1] = Assembler.Hex_base(command[2].substring(1), "00");
                    machineCode.add(previo);
                    assembler.memory_counter++;
                    previo = new String[2];

                    if(command[3].toUpperCase().startsWith("0X")){ // Cuando carga un valor fijo
                        previo[0] = command[3].substring(2, 4).toUpperCase();
                        previo[1] = "";
                    }else{
                        if(assembler.labels.containsKey(command[3])){ // Cuando carga una etiqueta que ya conocemos
                            previo[0] = assembler.labels.get(command[3]);
                            previo[1] = "";
                        }else{ // Cuando carga una variable o una etiqueta desconocida
                            previo[0] = "";
                            previo[1] = command[3];
                            faltantes.add(assembler.memory_counter);
                        }
                    }
                }else{ // Todos los demas comandos
                    if(command[3].toUpperCase().startsWith("R")){
                        previo[1] = Assembler.Hex_base(command[2].substring(1), command[3].substring(1));
                    }else{
                        previo[1] = Assembler.Hex_base(command[2].substring(1), command[3]);
                    }
                }

                machineCode.add(previo);
                assembler.memory_counter++;
                line = reader.readLine();

                if(line != null) var = line.startsWith(".data");// Comprobamos si inicia la seccion de variables
                if(var)line = reader.readLine(); // .data ocupa una linea
            }else{
                previo = new String[2];
                command = line.split( "\t");
                // Se agregan las variables
                if(command[0].startsWith(".array")){
                    /*
                    * Formato para Arrays: (Todos los valores del Array deben estar inicializados)
                    * .Array nombre tamano
                    * valor0 valor1 ... valorN
                     */
                    assembler.var.put(command[1], Integer.toString(assembler.memory_counter, 16).toUpperCase());
                    valores = reader.readLine().split(" ");
                    for(int i = 0; i < Integer.parseInt(command[2]); i++){
                        previo = new String[2];
                        previo[0] = valores[i];
                        previo[1] = "";
                        machineCode.add(previo);
                        assembler.memory_counter++;
                    }
                    line = reader.readLine();
                }else{ // Cuando es una variable normal
                    assembler.var.put(command[0], Integer.toString(assembler.memory_counter, 16).toUpperCase());
                    previo[0] = command[1];
                    previo[1] = "";

                    machineCode.add(previo);
                    assembler.memory_counter++;
                    line = reader.readLine();
                }
            }
        }

        r.close(); // Se cierra el BufferReader

        /*
        * Segunda Pasada
         */

        for(Integer f: faltantes){
            String key = machineCode.get(f)[1];
            /*
            * Se asume que si no se direccionaba a un label, se hacia hacia una variable
            * Si no se encuentra queda "None"
             */
            if(assembler.labels.containsKey(key)){
                machineCode.get(f)[1] = assembler.labels.get(key);
            }else{
                machineCode.get(f)[1] = assembler.var.get(key);
            }
        }

        /*
        **** Aqui empieza el Output
         */

        Writer w = new Writer("C:\\\\Bolt/RAM.txt");
        PrintWriter writer = w.getWriter();

        int printCounter = 0;
        int lineCounter = 0;
        String toPrint;

        for(int i = 0; i < 16; i++){ // Unicamente formato
            writer.print("--0" + Integer.toHexString(i).toUpperCase() + "--");
        }
        writer.println("\n");

        for(String[] code : machineCode){
            toPrint = code[0] + code[1]; // Se unen las 2 partes del comando
            if(toPrint.length() == 1) toPrint = "0" + toPrint; // Esto para rellenar valores faltantes

            writer.print("x\"" + toPrint + "\",");
            printCounter++;
            lineCounter = formato(printCounter, lineCounter, writer); // Unicamente formato
        }

        while (printCounter < 256){ // Para terminar de llenar la RAM
            if (printCounter == 255) writer.print("x\"00\" ");// El ultimo va sin ,
            else writer.print("x\"00\",");
            printCounter++;
            lineCounter = formato(printCounter, lineCounter, writer); // Unicamente formato
        }

        w.close(); //Se cierra el escritor

        w = new Writer("C:\\\\Bolt/tables.txt");
        writer = w.getWriter();

        assembler.printTables(writer);

        w.close();

    }

    private void printTables(PrintWriter writer){
        writer.println("Labels");
        for (String key: labels.keySet()){
            writer.print(key);
            for(int i = 10 - key.length(); i > 0; i--) writer.print(" ");// Esto es solo grafico
            writer.println("|\t" + labels.get(key));
        }
        writer.println("\n\nVariables");
        for (String key: var.keySet()){
            writer.println(key + "\t|\t" + var.get(key));
        }
    }

    private static int formato(int printCounter, int lineCounter, PrintWriter writer){
        if(printCounter%16 == 0){
            writer.println(" --" + Integer.toHexString(lineCounter).toUpperCase() + "F");
            lineCounter++;
            if(lineCounter%4 == 0) writer.println("--");
        }
        return lineCounter;
    }

    private static String Hex_base(String a, String b){ // Conversion de 2 numeros a 1 hexadecimal
        a = Integer.toBinaryString(Integer.parseInt(a));
        b = Integer.toBinaryString(Integer.parseInt(b));
        if(a.length() == 1) a = "0" + a;
        if(b.length() == 1) b = "0" + b;
        return Integer.toHexString(Integer.parseInt(a + b, 2)).toUpperCase();
    }

    private Assembler(){
        labels = new HashMap<>();
        var = new HashMap<>();
        memory_counter = 0;
    }

}