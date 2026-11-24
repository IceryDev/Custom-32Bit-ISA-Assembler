# 32-Bit Processor Assembler

### About
A two-pass assembler for the 32-bit processor we are building as part of my coursework. This assembler is modified from my previous assembler ([Orbyte Assembler](https://github.com/IceryDev/Orbyte-Assembler)) and follows the exact same syntax in terms of instructions. Shortly, I did not want to write the instructions in binary, so I made this.

### Properties
- 32-bit instruction set with fixed mnemonics.
- Encoding for 16 32-bit registers R0, R1, R2, ..., R15
- Labels and PC offset calculation for branching
- Memory allocation and pointer loading for immediate load instructions
- Disclaimer: This assembler is not designed to replace the thinking required by our module. The instruction set architecture, choices in operands and operations, and control memory micro-coding all have to be implemented correctly for this assembler to function as intended.
- Warning: This assembler does not check if your ISA's memory layout is valid. That is up to you to specify.

### Configuration
The approach used to represent instructions with immediate operands or flag-setting instructions, and the memory layout can be configured through an XML file, whose example layout is specified below.
```xml
<?xml version="1.0" encoding="UTF-16" ?>

<ISAOptions>
    <ImmType>Mode</ImmType>
    <FlagType>Mode</FlagType>
    <MemType amnt="Optional">Mode</MemType>
    <Start>Value</Start>
</ISAOptions>
```
Allowed modes are as follows; for more information, see the Javadoc at the top of the files under src/Resources:
<br>
- ImmType: Separate, Incremental, ImmBit
- FlagType: Hardware, Incremental, SBit
- MemType: Limited, Inject, Shift
- Start: The start of instruction memory, a positive integer (Applies when MemType is type Inject)

<br> _If Shift is chosen for MemType, the shift amount has to be specified. This is a positive integer that makes the instruction memory start from 1 << ShiftAmnt_
<br> <br> Another note: It is not advised to use bit modes if you have instructions with width more than one that uses those corresponding functions. You can implement separate instructions for these, or use one of the other modes.

### Instruction Set Specification
Additionally, the program must be supplied with a valid instruction set, in CSV format, which includes headers. An example with the headers is given below:
```
INST, OpCode, Branch, Compare, Load, OpCount, SetFlags, RegAllow, ImmAllow, InstWidth
ADD, 0x12345, 0, 0, 0, 3, 1, 1, 1, 1
```
Here is the meaning of each field (Use 0/1 for false/true in Boolean decisions):
<BR>
- `INST`: The instruction mnemonic. Examples from ARM Assembly include `LDR` or `BEQ`
- `OpCode`: The opcode bits of the instruction. By default, this is 20 bits, but the selection ImmBit and SBit decrease this by one each. Must be hexadecimal or decimal without zero-padding.
- `Branch`: Is this instruction a branch instruction?
- `Compare`: Is the only purpose of this instruction setting flags?
- `Load`: Is this instruction used when loading values _from_ memory?
- `OpCount`: Operand count. Supported: 2-3
- `SetFlags`: Does this instruction have variations that can set flags?
- `RegAllow`: Can the last operand be a register operand for this instruction?
- `ImmAllow`: Can the last operand be an immediate operand for this instruction?
- `InstWidth`: The amount of memory locations the instruction takes in control memory.

### General Instruction Structure
- 3-operand instruction general format:
```bash
<op> <Rd> <Rn> <Rm>/#<imm4>
```
- 2-operand instruction general format:
```bash
<op> <Rd> <Rn>/#<imm4>
```
- Load instruction general format:
```bash
<op> <Rd> &<Ra>/#<imm32>
```
- Store instruction general format:
```bash
<op> <Rv> &<Ra>
```
- Label format:
```bash
<name>:
```
- Branch instruction format:
```bash
<op> <Label>
```

Where `<op>`, `<Rd>`, `<Rn>`, `<Rm>`, `<Ra>`, `<Rv>`, and `<imm4/32>` are placeholders for the instruction name, destination register, register operand 1, 
register operand 2, register operand containing address, value register for store, and immediate value with size 4/32 bits.

**NOTE:** This assembler ONLY accepts input with this format, for instance putting an immediate to a store instruction will result in an error.

### Delimiters (Can be configured from the Assembler.java file)
- "#": Precedes an immediate operand
- "&": Precedes a register operand to be considered as an address
- "//": Precedes comments (Multi-line comments not supported)
- ":": Succeeds a label name

### Running the Assembler
To run this assembler, you need JDK 21 or later.

Get the .jar file from the releases part, place all required files in the same folder and run within that folder:
```bash
java -jar assembler.jar <config file path> <isa path> <.oas file> <output file>
```
You need to specify the config, ISA, and .oas file's extension.
