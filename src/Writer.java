import java.io.*;

public class Writer{

    private FileWriter file;
    private PrintWriter writer;

    /*
    * Apertura del writer, creacion de BufferedReader
     */
    public Writer(String source){
        this.file = null;
        this.writer = null;
        try
        {
            file = new FileWriter(source);
            writer = new PrintWriter(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PrintWriter getWriter() {
        return writer;
    }


    /*
        * Cierre del writer
         */
    public void close(){
        try {
            if (null != file)
                file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}