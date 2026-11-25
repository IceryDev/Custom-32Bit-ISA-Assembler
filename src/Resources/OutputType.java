package Resources;

public enum OutputType {
    Hex() {
        @Override
        public StringBuilder writeMem(StringBuilder sb, Integer val, int lineNo) {
            sb.setLength(0);
            sb.append(String.format("x\"%8s\"", Integer.toHexString(val)).replace(' ', '0').toUpperCase());
            sb.append(String.format(", --%02X", lineNo));
            return sb;
        }

        @Override
        public StringBuilder writeNull(StringBuilder sb, int lineNo) {
            sb.setLength(0);
            sb.append(String.format("x\"00000000\", --%02X", lineNo));
            return sb;
        }

        @Override
        public StringBuilder writeInst(StringBuilder sb, Integer inst, int lineNo) {
            sb.setLength(0);
            int parsedVal = (inst & OPCODE_MASK) >>> (PAYLOAD_SLOT_WIDTH * PAYLOAD_SLOTS);
            sb.append(String.format("x\"%5s\"&", Integer.toHexString(parsedVal)).replace(' ', '0').toUpperCase());
            parsedVal = (inst & DR_MASK) >>> (PAYLOAD_SLOT_WIDTH * (PAYLOAD_SLOTS-1));
            sb.append(String.format("x\"%1s\"&", Integer.toHexString(parsedVal)).replace(' ', '0').toUpperCase());
            parsedVal = (inst & REGA_MASK) >>> (PAYLOAD_SLOT_WIDTH);
            sb.append(String.format("x\"%1s\"&", Integer.toHexString(parsedVal)).replace(' ', '0').toUpperCase());
            parsedVal = (inst & REGB_MASK);
            sb.append(String.format("x\"%1s\"", Integer.toHexString(parsedVal)).replace(' ', '0').toUpperCase());
            sb.append(String.format(", --%02X", lineNo));
            return sb;
        }
    },
    Binary() {
        @Override
        public StringBuilder writeMem(StringBuilder sb, Integer val, int lineNo) {
            sb.setLength(0);
            sb.append(String.format("\"%32s\"", Integer.toBinaryString(val)).replace(' ', '0'));
            sb.append(String.format(", --%02X", lineNo));
            return sb;
        }

        @Override
        public StringBuilder writeNull(StringBuilder sb, int lineNo) {
            sb.setLength(0);
            sb.append(String.format("\"00000000000000000000000000000000\", --%02X", lineNo));
            return sb;
        }

        @Override
        public StringBuilder writeInst(StringBuilder sb, Integer inst, int lineNo) {
            sb.setLength(0);
            int parsedVal = (inst & OPCODE_MASK) >>> (PAYLOAD_SLOT_WIDTH * PAYLOAD_SLOTS);
            sb.append(String.format("\"%20s\"&", Integer.toBinaryString(parsedVal)).replace(' ', '0'));
            parsedVal = (inst & DR_MASK) >>> (PAYLOAD_SLOT_WIDTH * (PAYLOAD_SLOTS-1));
            sb.append(String.format("\"%4s\"&", Integer.toBinaryString(parsedVal)).replace(' ', '0'));
            parsedVal = (inst & REGA_MASK) >>> (PAYLOAD_SLOT_WIDTH);
            sb.append(String.format("\"%4s\"&", Integer.toBinaryString(parsedVal)).replace(' ', '0'));
            parsedVal = (inst & REGB_MASK);
            sb.append(String.format("\"%4s\"", Integer.toBinaryString(parsedVal)).replace(' ', '0'));
            sb.append(String.format(", --%02X", lineNo));
            return sb;
        }
    };

    public static final int OPCODE_MASK = 0xFFFFF000;
    public static final int DR_MASK = 0x00000F00;
    public static final int REGA_MASK = 0x000000F0;
    public static final int REGB_MASK = 0x0000000F;

    public static final int PAYLOAD_SLOT_WIDTH = 4;
    public static final int PAYLOAD_SLOTS = 3;

    public abstract StringBuilder writeMem(StringBuilder sb, Integer val, int lineNo);
    public abstract StringBuilder writeNull(StringBuilder sb, int lineNo);
    public abstract StringBuilder writeInst(StringBuilder sb, Integer inst, int lineNo);
}
