package Resources;

public record Instruction(int opCode,
                          boolean isBranch,
                          boolean isCompare,
                          boolean isLoad,
                          boolean isStore,
                          boolean logicUnit2Op,
                          int opCount,
                          boolean setFlags,
                          boolean allowReg,
                          boolean allowImm,
                          int instWidth) {
}
