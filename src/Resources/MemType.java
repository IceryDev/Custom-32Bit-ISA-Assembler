package Resources;

/**
 * Describes what kind of additional processing the assembler has to do
 * to process immediate loading from memory.
 * <p>
 * Options: <br>
 *  - {@code Inject}: Injects the loaded values to the first few addresses before the PC reset value. <br>
 *  - {@code Shift}: Places the loaded values at the memory address starting from 1 << ShiftAmount. <br>
 *  - {@code Limited}: Only allow memory load within the payload (keep the immediate version, but make it more like
 *  a move instruction)
 */
public enum MemType {
    Inject,
    Shift,
    Limited;
}
