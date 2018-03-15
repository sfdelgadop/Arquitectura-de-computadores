import java.util.HashMap;

public class ISA {

    private static HashMap<String, String> InstructionSet = new HashMap<>();

    public static String get_ISA(String code){
        if(InstructionSet.isEmpty()){
            InstructionSet.put("NOP", "0");
            InstructionSet.put("LDI", "1");
            InstructionSet.put("LD" , "2");
            InstructionSet.put("ST" , "3");
            InstructionSet.put("ADD", "4");
            InstructionSet.put("INC", "5");
            InstructionSet.put("NEG", "6");
            InstructionSet.put("SUB", "7");
            InstructionSet.put("JMP", "8");
            InstructionSet.put("JZ" , "9");
            InstructionSet.put("JN" , "A");
        }
        return InstructionSet.get(code);
    }
}
