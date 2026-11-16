# 32-Bit Processor Assembler

### About
A two-pass assembler for the 32-bit processor we are building as part of my coursework. This assembler is modified from my previous assembler (Orbyte Assembler \<link to be inserted here\>) , and follows the exact same syntax in terms of instructions.

### Properties
- 32-bit instruction set with mostly fixed mnemonics.
- Encoding for 32 16-bit registers R0, R1, R2, ..., R15
- Labels and PC offset calculation for branching
- It reads in opcodes from a separate file to not fully disclose the coursework ISA

### General Instruction Structure
- 3-operand instruction general format:
```bash
<op> <Rd> <Rn> <Rm>/#<imm3>
```
- 2-operand instruction general format:
```bash
<op> <Rd> <Rn>/#<imm6>
```
- Memory instruction general format (WRM/FRM):
```bash
<op> <Rd> <Rn>/&<Ra>/#<imm8>
```
- Label format:
```bash
<name>:
```
- Branch instruction format:
```bash
B<cond> <Label>
```

Where <\op>, <\Rd>, <\Rn>, <\Rm>, <\Ra>, <\imm3/6/8>, and <\cond> are placeholders for the instruction name, destination register, register operand 1, 
register operand 2, register operand containing address, immediate value with size 3/6/8 bits, and the condition for branching.

### Delimiters
- "#": Precedes an immediate operand
- "&": Precedes a register operand to be considered as an address
- "//": Precedes comments (Multi-line comments not supported)
- ":": Succeeds a label name

### Running the Assembler
Make sure that you are in the src directory and run:
```bash
java Assembler.java <.oas file> <output file>
```
You only need to specify the .oas file's extension.
