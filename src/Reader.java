import java.io.*;

public class Reader{

    private FileReader reader;
    private BufferedReader buffer;

    /*
    * Apertura del reader, creacion de BufferedReader
     */
    public Reader(String source){
        File archivo;
        this.reader = null;
        this.buffer = null;
        try {
            archivo = new File (source);
            reader = new FileReader (archivo);
            buffer = new BufferedReader(reader);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public BufferedReader getBuffer() {
        return buffer;
    }

    /*
        * Cierre del reader
         */
    public void close(){
        try{
            if( null != reader){
                reader.close();
            }
        }catch (Exception e2){
            e2.printStackTrace();
        }
    }
}