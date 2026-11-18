package Resources;

/**
 * Describes what kind of additional processing the assembler has to do
 * to process immediate operands.
 * <p>
 * Options: <br>
 *  - {@code Separate}: Hardware ISA provides separate instructions for imm and non-imm operations. <br>
 *  - {@code ImmBit}: The assembler uses one of the opcode bits as the "immediate" bit
 *  the input opcode size has to be one less if this option is chosen. The imm bit either is
 *  always the least significant bit in the opcode. (Takes priority over flag bit) <br>
 *  - {@code Incremental}: If an immediate operand is allowed for the instruction, increments the opcode
 *  by one to generate the immediate version of the operation.
 *  <br>
 *  Note: Both implementations have to be implemented by an ASM by the hardware in all cases. These
 *  concern only the placement of the bits and the corresponding locations in the control memory.
 *  Also, if an instruction takes more than one state for execute cycle, Incremental and ImmBit probably
 *  will not work.
 */
public enum ImmType {
    ImmBit(1),
    Incremental,
    Separate();

    public final int OP_SHRINK;
    ImmType() {
        this.OP_SHRINK = 0;
    }

    ImmType(int opShrink) {
        this.OP_SHRINK = opShrink;
    }
}
