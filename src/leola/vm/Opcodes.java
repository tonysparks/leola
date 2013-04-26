/*
	Leola Programming Language
	Author: Tony Sparks
	See license.txt
*/
package leola.vm;

import java.util.HashMap;
import java.util.Map;

import leola.vm.exceptions.LeolaRuntimeException;

/**
 * Instruction Operation Codes
 * 
 * @author Tony
 *
 */
public class Opcodes {

	
	// instruction for ARG1 ARG2
	// 0x222111FF
	
	// instruction for ARGx
	// 0xXXXXXXFF
	
	// instruction for ARGsx (signed)
	// 0x7XXXXXFF
	
	// instruction
	// 0x002211FF
	// FF = opcode
	// 11 = arg1
	// 22 = arg2
	// X = part of ARG(s)x
	// 7 = 0111 last bit for signed value
	
	public static final int OP_SIZE = 8;
	public static final int ARG1_SIZE = 12;		// number of bits
	public static final int ARG2_SIZE = 12;		// number of bits
	public static final int ARGx_SIZE = ARG1_SIZE + ARG2_SIZE;
	public static final int ARGsx_SIZE = ARG1_SIZE + ARG2_SIZE-1;
	public static final int OP_POS = 0;
	public static final int ARG1_POS = OP_POS + OP_SIZE;
	public static final int ARG2_POS = ARG1_POS + ARG1_SIZE;
	public static final int ARGx_POS = ARG1_POS;
	
	public static final int MAX_OP   = ( (1<<OP_SIZE) - 1);
	public static final int MAX_ARG1 = ( (1<<ARG1_SIZE) - 1);
	public static final int MAX_ARG2 = ( (1<<ARG2_SIZE) - 1);
	public static final int MAX_ARGx = ( (1<<ARGx_SIZE) - 1);
	public static final int MAX_ARGsx = (MAX_ARGx>>1);
	
	public static final int OP_MASK   = ((1<<OP_SIZE)-1)<<OP_POS; 
	public static final int ARG1_MASK = ((1<<ARG1_SIZE)-1)<<ARG1_POS;
	public static final int ARG2_MASK = ((1<<ARG2_SIZE)-1)<<ARG2_POS;
	public static final int ARGx_MASK = ((1<<ARGx_SIZE)-1)<<ARGx_POS;
	
	public static final int NOT_OP_MASK   = ~OP_MASK;
	public static final int NOT_ARG1_MASK = ~ARG1_MASK;
	public static final int NOT_ARG2_MASK = ~ARG2_MASK;
	public static final int NOT_ARGx_MASK = ~ARGx_MASK;
	
	
	/**
	 * Strips the opinstr out of the supplied integer
	 * @param instr
	 * @return
	 */
	public static int OPCODE(int instr) {
//		return (instr >> OP_POS) & MAX_OP;
		return instr & MAX_OP;
	}
	
	/**
	 * Returns the first argument
	 * @param instr
	 * @return arg1 one value
	 */
	public static int ARG1(int instr) {
		return (instr >>>  ARG1_POS) & MAX_ARG1;
	}

	/**
	 * Returns the second argument
	 * @param instr
	 * @return arg2 value
	 */
	public static int ARG2(int instr) {
		return (instr >>> ARG2_POS) & MAX_ARG2;		
	}
	
	/**
	 * Returns the x argument
	 * @param instr 
	 * @return the x argument
	 */
	public static int ARGx(int instr) {
		return ((instr >>> ARGx_POS) & MAX_ARGx);
	}
	
	/**
	 * Returns the signed x argument
	 * @param instr
	 * @return the signed x argument
	 */
	public static int ARGsx(int instr) {
		return ((instr >> ARGx_POS) & MAX_ARGx) - MAX_ARGsx;
	}
	
	/**
	 * Sets the signed x value on the instruction
	 * @param instr
	 * @param x
	 * @return the instruction with the set value
	 */
	public static int SET_ARGsx(int instr, int sx) {
		return (instr & (NOT_ARGx_MASK)) | (( (sx + MAX_ARGsx) << ARGx_POS) & ARGx_MASK);
	}
	
	/**
	 * Sets the x value on the instruction
	 * @param instr
	 * @param x
	 * @return the instruction with the set value
	 */
	public static int SET_ARGx(int instr, int argx) {
		return (instr & (NOT_ARGx_MASK)) | (( argx << ARGx_POS) & ARGx_MASK);
	}
	
	/**
	 * Sets the arg1 value on the instruction
	 * @param instr
	 * @param x
	 * @return the instruction with the set value
	 */
	public static int SET_ARG1(int instr, int arg1) {
		return (instr & NOT_ARG1_MASK) | (( arg1 << ARG1_POS) & ARG1_MASK);
	}
	
	/**
	 * Sets the arg2 value on the instruction
	 * @param instr
	 * @param x
	 * @return the instruction with the set value
	 */
	public static int SET_ARG2(int instr, int arg2) {
		return (instr & NOT_ARG2_MASK) | (( arg2 << ARG2_POS) & ARG2_MASK);
	}
	
		
	/**
	 * String to integer opcode
	 * @param opcode
	 * @return the opcode represented by the string, or -1 if not found.
	 */
	public static int str2op(String opcode) {
		Integer op = opcodes.get(opcode.toUpperCase());
		return (op != null) ? op : -1;
	}
	
	/**
	 * Converts the byte opcode to a readable string
	 * 
	 * @param opcode
	 * @return a string
	 */
	public static String op2str(int opcode) {
		String op = "";
		switch(opcode) {
			/* Store operations */
			case LOAD_CONST: {
				op = "LOAD_CONST";
				break;
			}
			case LOAD_LOCAL: {
				op = "LOAD_LOCAL";
				break;
			}
			case LOAD_OUTER: {
				op = "LOAD_OUTER";				
				break;
			}
			case xLOAD_OUTER: {
				op = "xLOAD_OUTER";
				break;
			}
			case xLOAD_LOCAL: {
				op = "xLOAD_LOCAL";
				break;
			}			
			case LOAD_NULL: {
				op = "LOAD_NULL";
				break;
			}
			case LOAD_TRUE: {
				op = "LOAD_TRUE";
				break;
			}
			case LOAD_FALSE: {
				op = "LOAD_FALSE";
				break;
			}
			case STORE_LOCAL: {
				op = "STORE_LOCAL";
				break;
			}
			case STORE_OUTER: {
				op = "STORE_OUTER";
				break;
			}

		
			/* stack operators */
			case SHIFT:	{	
				op = "SHIFT";
				break;
			}
			case POP:	{
				op = "POP";					
				break;
			}
			case OPPOP:	{
				op = "OPPOP";					
				break;
			}		
			case DUP: {
				op = "DUP";
				break;
			}
			case RET:	{
				op = "RET";
				break;
			}
			case MOV: {
				op = "MOV";
				break;
			}
			case MOVN: {
				op = "MOVN";
				break;
			}
			case SWAP: {
				op = "SWAP";
				break;
			}
			
			case JMP:	{
				op = "JMP";				
				break;
			}
			case INVOKE:	{		
				op = "INVOKE";
				break;
			}
			case TAIL_CALL: {
				op = "TAIL_CALL";
				break;
			}
			
			case NEW_NAMESPACE: {
				op = "NEW_NAMESPACE";
				break;
			}
			case NEW:	{
				op = "NEW";
				break;
			}
			case NEW_ARRAY:	{
				op = "NEW_ARRAY";
				break;
			}
			case NEW_MAP: {
				op = "NEW_MAP";
				break;
			}			
			case DEF: {
				op = "DEF";
				break;
			}
			case IS_A: {
				op = "IS_A";
				break;
			}			
			case IF:	{					
				op = "IF";
				break;
			}
			case BREAK: {
				op = "BREAK";
				break;
			}
			case CONTINUE: {
				op = "CONTINUE";
				break;
			}
			case CLASS_DEF: {
				op = "CLASS_DEF";
				break;
			}
			case THROW: {
				op = "THROW";
				break;
			}			
			/* object access */
			case GET: {
				op = "GET";
				break;
			}
			case SET: {
				op = "SET";
				break;
			}

			case GET_GLOBAL: {
				op = "GET_GLOBAL";
				break;
			}
			case SET_GLOBAL: {
				op = "SET_GLOBAL";
				break;
			}
			case GET_NAMESPACE: {
				op = "GET_NAMESPACE";
				break;
			}
			
			
			/* arithmetic operators */
			case ADD:	{
				op = "ADD";
				break;
			}
			case SUB:	{
				op = "SUB";
				break;
			}
			case MUL:	{
				op = "MUL";
				break;
			}
			case DIV:	{
				op = "DIV";
				break;
			}
			case MOD:	{
				op = "MOD";
				break;
			}
			case NEG: {
				op = "NEG";
				break;
			}
			
			case BSL:	{
				op = "BSL";
				break;
			}
			case BSR:	{
				op = "BSR";
				break;
			}
			case BNOT:	{
				op = "BNOT";
				break;
			}
			case XOR:	{
				op = "XOR";
				break;
			}
			case LOR:	{
				op = "LOR";
				break;
			}
			case LAND:	{
				op = "LAND";
				break;
			}
			
			case OR:	{
				op = "OR";
				break;
			}
			case AND:	{
				op = "AND";
				break;
			}
			case NOT:	{
				op = "NOT";
				break;
			}
			case REQ:	{
				op = "REQ";
				break;
			}
			case EQ:	{
				op = "EQ";
				break;
			}
			case NEQ:	{
				op = "NEQ";
				break;
			}
			case GT:	{
				op = "GT";
				break;
			}
			case GTE:	{
				op = "GTE";
				break;
			}
			case LT:	{
				op = "LT";
				break;
			}
			case LTE:	{
				op = "LTE";
				break;
			}
			case LINE: {
				op = "LINE";
				break;
			}
			default: {
				throw new LeolaRuntimeException("Unknown Opcode: " + opcode);
			}
		}
	
		return op;
	}
	
	/**
	 * The opcode is in the range of 0-255 
	 */
	public static final int
	
		/* stack operators */
		SHIFT  = 1,
		POP = 2,
		DUP = 3,
		RET = 4,
		OPPOP = 5,
		MOV = 6,
		MOVN = 7,
		SWAP = 8,
		
		LOAD_CONST = 9,
		LOAD_LOCAL = 10,
		LOAD_OUTER = 11,
		
		LOAD_NULL = 12,
		LOAD_TRUE = 13,
		LOAD_FALSE = 14,		
						
		STORE_LOCAL = 15,
		STORE_OUTER = 16,		
		
		/* pseudo bytecodes */
		xLOAD_OUTER = 17,
		xLOAD_LOCAL = 18,		
		
		JMP = 19,
		NEW = 20,
		IF = 21,
		NEW_ARRAY = 22,
		NEW_MAP = 23,		
		DEF = 24,
		IS_A = 25,
		BREAK = 26,
		CONTINUE = 27,
		CLASS_DEF = 28,
		
		INVOKE = 29,
		NEW_NAMESPACE = 30,
		TAIL_CALL = 31,		
		
		/* member access */
		GET = 32,
		SET = 33,
		
		GET_GLOBAL = 34,
		SET_GLOBAL = 35,
		
		GET_NAMESPACE = 36,
		
		THROW = 37,
						
		/* arithmetic operators */
		ADD = 38,
		SUB = 39,
		MUL = 40,
		DIV = 41,
		MOD = 42,
		NEG = 43,
	
		BSL = 44,
		BSR = 45,
		BNOT = 46,
		XOR = 47,
		LOR = 48,
		LAND = 49,
		
		OR = 50,
		AND = 51,
		NOT = 52,
		
		REQ = 53,
		EQ = 54,
		NEQ = 55,
		GT = 56,
		GTE = 57,
		LT = 58,
		LTE = 59,
		
		/* debug */
		LINE = 60
		;
	
	
	private static final Map<String, Integer> opcodes = new HashMap<String, Integer>();
	static {
		/* Store operations */
		opcodes.put("LOAD_CONST", LOAD_CONST);
		opcodes.put("LOAD_LOCAL", LOAD_LOCAL);
		opcodes.put("LOAD_OUTER", LOAD_OUTER);
		opcodes.put("xLOAD_OUTER", xLOAD_OUTER);
		opcodes.put("xLOAD_LOCAL", xLOAD_LOCAL);		
		opcodes.put("LOAD_NULL", LOAD_NULL);
		opcodes.put("LOAD_TRUE", LOAD_TRUE);
		opcodes.put("LOAD_FALSE", LOAD_FALSE);
		
		opcodes.put("STORE_LOCAL", STORE_LOCAL);
		opcodes.put("STORE_OUTER", STORE_OUTER);
		

		/* stack operators */
		opcodes.put("SHIFT", SHIFT);
		opcodes.put("POP", POP);
		opcodes.put("OPPOP", OPPOP);
		opcodes.put("DUP", DUP);
		opcodes.put("RET", RET);
		opcodes.put("MOV", MOV);
		opcodes.put("MOVN", MOVN);
		opcodes.put("SWAP", SWAP);
		opcodes.put("JMP", JMP);
		opcodes.put("INVOKE", INVOKE);
		opcodes.put("TAIL_CALL", TAIL_CALL);		
		opcodes.put("NEW_NAMESPACE", NEW_NAMESPACE);
		opcodes.put("NEW", NEW);
		opcodes.put("NEW_ARRAY", NEW_ARRAY);
		opcodes.put("NEW_MAP", NEW_MAP);
		opcodes.put("DEF", DEF);
		opcodes.put("IS_A", IS_A);
		opcodes.put("IF", IF);
		opcodes.put("BREAK", BREAK);
		opcodes.put("CONTINUE", CONTINUE);
		opcodes.put("CLASS_DEF", CLASS_DEF);
		opcodes.put("THROW", THROW);

		/* object access */
		opcodes.put("GET", GET);
		opcodes.put("SET", SET);
		opcodes.put("GET_GLOBAL", GET_GLOBAL);
		opcodes.put("SET_GLOBAL", SET_GLOBAL);
		opcodes.put("GET_NAMESPACE", GET_NAMESPACE);		

		/* arithmetic operators */
		opcodes.put("ADD", ADD);
		opcodes.put("SUB", SUB);
		opcodes.put("MUL", MUL);
		opcodes.put("DIV", DIV);
		opcodes.put("MOD", MOD);
		opcodes.put("NEG", NEG);
		opcodes.put("BSL", BSL);
		opcodes.put("BSR", BSR);
		opcodes.put("BNOT", BNOT);
		opcodes.put("XOR", XOR);
		opcodes.put("LOR", LOR);
		opcodes.put("LAND", LAND);
		opcodes.put("OR", OR);
		opcodes.put("AND", AND);
		opcodes.put("NOT", NOT);
		opcodes.put("REQ", REQ);
		opcodes.put("EQ", EQ);
		opcodes.put("NEQ", NEQ);
		opcodes.put("GT", GT);
		opcodes.put("GTE", GTE);
		opcodes.put("LT", LT);
		opcodes.put("LTE", LTE);
		opcodes.put("LINE", LINE);
		
	}

}

