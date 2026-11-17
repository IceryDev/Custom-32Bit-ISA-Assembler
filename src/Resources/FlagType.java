package Resources;

/**
 * Describes what kind of additional processing the assembler has to do
 * to process status register flag setting.
 * <p>
 * Options: <br>
 *  - {@code Hardware}: All flag related instructions are handled by hardware. <br>
 *  - {@code SBit}: The assembler uses one of the opcode bits as the "set flags" bit
 *  the input opcode size has to be one less if this option is chosen. The S bit either is
 *  the first or second least significant bit of opcode. (Immediate takes priority) <br>
 *  - {@code Incremental}: If no immediate is allowed or immediate is of type Separate,
 *  this mode adds 1 to the instruction opcode, if immediate is also Incremental, this adds 2 to the
 *  opcode. (Note that if the immediate type is ImmBit, the addition is done to the remaining 19 bits.)
 */
public enum FlagType {
    Hardware(),
    SBit(1),
    Incremental();

    public final int OP_SHRINK;
    FlagType() {
        this.OP_SHRINK = 0;
    }

    FlagType(int opShrink) {
        this.OP_SHRINK = opShrink;
    }
}
