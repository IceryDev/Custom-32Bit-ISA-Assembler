package Resources;

public record Instruction(int opCode,
                          boolean isBranch,
                          boolean isLoad,
                          int opCount,
                          boolean setFlags,
                          boolean allowReg,
                          boolean allowImm) {
}
