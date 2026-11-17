package Resources;

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
