package Resources;

public record Instruction(int opCode,
                          boolean isBranch,
                          boolean isCompare,
                          boolean isLoad,
                          boolean isStore,
                          int opCount,
                          boolean setFlags,
                          boolean allowReg,
                          boolean allowImm,
                          int instWidth) {
}
