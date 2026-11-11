package Resources;

public enum Register { //In the case of expanding instruction size (in bits) you can make each 4 bits long
                       //Do not forget to change the value you pass to the packer (3 to 4).
    R0((byte) 0b0000),
    R1((byte) 0b0001),
    R2((byte) 0b0010),
    R3((byte) 0b0011),
    R4((byte) 0b0100),
    R5((byte) 0b0101),
    R6((byte) 0b0110),
    R7((byte) 0b0111),
    R8((byte) 0b1000),
    R9((byte) 0b1001),
    R10((byte) 0b1010),
    R11((byte) 0b1011),
    R12((byte) 0b1100),
    R13((byte) 0b1101),
    R14((byte) 0b1110),
    R15((byte) 0b1111);

    public static final int REGCODE_BIT_COUNT = 4;
    public static final int REGISTER_SIZE = 16; //Note: Probably not needed for this type of ISA

    byte regCode = 0b0;

    Register (byte regCode){
        this.regCode = regCode;
    }

    public static boolean isRegister(String name){
        try {
            Register.valueOf(name.toUpperCase());
            return true;
        }
        catch (IllegalArgumentException error){
            return false;
        }
    }

    public byte getRegCode(){
        return this.regCode;
    }
}
