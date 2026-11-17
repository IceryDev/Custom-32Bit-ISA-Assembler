package Funcs;

import Resources.FlagType;
import Resources.ImmType;
import Resources.Instruction;
import Resources.MemType;

import java.util.ArrayList;

public class ISetParser {
    public ArrayList<Instruction> iset = new ArrayList<>();
    public final ImmType immType;
    public final FlagType flagType;
    public final MemType memType;

    public ISetParser (String configFilePath, ImmType immType, FlagType flagType, MemType memType) {

        this.immType = immType;
        this.flagType = flagType;
        this.memType = memType;
    }
}
