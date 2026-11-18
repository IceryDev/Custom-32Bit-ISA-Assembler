package Resources;

/**
 * Describes what kind of additional processing the assembler has to do
 * to process status register flag setting.
 * <p>
 * Options: <br>
 *  - {@code Hardware}: All flag related instructions are handled by hardware. <br>
 *  - {@code SBit}: The assembler uses one of the opcode bits as the "set flags" bit
 *  the input opcode size has to be one less if this option is chosen. The S bit always is
 *  the least significant bit of opcode. (Takes priority over immediate bit) <br>
 *  - {@code Incremental}: If no immediate is allowed or immediate is of type Separate,
 *  this mode adds 1 to the instruction opcode, if immediate is also Incremental, this adds 2 to the
 *  opcode. (Note that if the immediate type is ImmBit, the addition is done to the remaining 19 bits.)
 */
public enum FlagType {
    Hardware() {
        @Override
        public int encode(int opCode, boolean setFlags, boolean immAllwAndInc, boolean cmpOverride) {
            return opCode;
        }
    },
    SBit(1) {
        @Override
        public int encode(int opCode, boolean setFlags, boolean immAllwAndInc, boolean cmpOverride) {
            if (cmpOverride) return (opCode << 1) | 1;
            return ((opCode << 1) + (setFlags ? 1 : 0));
        }
    },
    Incremental() {
        @Override
        public int encode(int opCode, boolean setFlags, boolean immAllwAndInc, boolean cmpOverride) {
            if (cmpOverride || !setFlags) return opCode;
            return (opCode + (immAllwAndInc ? 2 : 1));
        }
    };

    public final int OP_SHRINK;
    FlagType() {
        this.OP_SHRINK = 0;
    }

    FlagType(int opShrink) {
        this.OP_SHRINK = opShrink;
    }

    public abstract int encode(int opCode, boolean setFlags, boolean immAllwAndInc, boolean cmpOverride);
}
