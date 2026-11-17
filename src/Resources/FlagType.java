package Resources;

/**
 * Describes what kind of additional processing the assembler has to do
 * to process status register flag setting.
 * <p>
 * Options:
 *  - {@code Hardware}: All flag related instructions are handled by hardware.
 *  - {@code SBit}: The assembler uses one of the opcode bits as the "set flags" bit
 *  the input opcode size has to be one less if this option is chosen. The S bit always is
 *  the least significant bit of opcode. (Takes priority over immediate bit)
 */
public enum FlagType {
    Hardware,
    SBit;
}
