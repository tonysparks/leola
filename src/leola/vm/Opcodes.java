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
    public static final int ARG1_SIZE = 12;     // number of bits
    public static final int ARG2_SIZE = 12;     // number of bits
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
    public static final int OPCODE(int instr) {
//      return (instr >> OP_POS) & MAX_OP;
        return instr & MAX_OP;
    }
    
    /**
     * Returns the first argument
     * @param instr
     * @return arg1 one value
     */
    public static final int ARG1(int instr) {
        return (instr >>>  ARG1_POS) & MAX_ARG1;
    }

    /**
     * Returns the second argument
     * @param instr
     * @return arg2 value
     */
    public static final int ARG2(int instr) {
        return (instr >>> ARG2_POS) & MAX_ARG2;     
    }
    
    /**
     * Returns the x argument
     * @param instr 
     * @return the x argument
     */
    public static final int ARGx(int instr) {
        return ((instr >>> ARGx_POS) & MAX_ARGx);
    }
    
    /**
     * Returns the signed x argument
     * @param instr
     * @return the signed x argument
     */
    public static final int ARGsx(int instr) {
        return ((instr >> ARGx_POS) & MAX_ARGx) - MAX_ARGsx;
    }
    
    /**
     * Sets the signed x value on the instruction
     * @param instr
     * @param x
     * @return the instruction with the set value
     */
    public static final int SET_ARGsx(int instr, int sx) {
        return (instr & (NOT_ARGx_MASK)) | (( (sx + MAX_ARGsx) << ARGx_POS) & ARGx_MASK);
    }
    
    /**
     * Sets the x value on the instruction
     * @param instr
     * @param x
     * @return the instruction with the set value
     */
    public static final int SET_ARGx(int instr, int argx) {
        return (instr & (NOT_ARGx_MASK)) | (( argx << ARGx_POS) & ARGx_MASK);
    }
    
    /**
     * Sets the arg1 value on the instruction
     * @param instr
     * @param x
     * @return the instruction with the set value
     */
    public static final int SET_ARG1(int instr, int arg1) {
        return (instr & NOT_ARG1_MASK) | (( arg1 << ARG1_POS) & ARG1_MASK);
    }
    
    /**
     * Sets the arg2 value on the instruction
     * @param instr
     * @param x
     * @return the instruction with the set value
     */
    public static final int SET_ARG2(int instr, int arg2) {
        return (instr & NOT_ARG2_MASK) | (( arg2 << ARG2_POS) & ARG2_MASK);
    }
    
        
    /**
     * String to integer opcode
     * @param opcode
     * @return the opcode represented by the string, or -1 if not found.
     */
    public static final int str2op(String opcode) {
        Integer op = opcodes.get(opcode.toUpperCase());
        return (op != null) ? op : -1;
    }
    
    /**
     * Converts the byte opcode to a readable string
     * 
     * @param opcode
     * @return a string
     */
    public static final String op2str(int opcode) {
        String op = "";
        switch(opcode) {
            /* stack operators */            
            case POP:   {
                op = "POP";                 
                break;
            }  
            case DUP: {
                op = "DUP";
                break;
            }
            case OPPOP: {
                op = "OPPOP";                   
                break;
            }        
        
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
            
            case LOAD_NAME: {
                op = "LOAD_NAME";              
                break;
            }
            case PARAM_END: {
                op = "PARAM_END";              
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
            
            case JMP:    {
                op = "JMP";                
                break;
            }
            case IFEQ:    {                   
                op = "IFEQ";
                break;
            }            
            
            case IS_A: {
                op = "IS_A";
                break;
            }           


            case NEW_ARRAY: {
                op = "NEW_ARRAY";
                break;
            }
            case NEW_MAP: {
                op = "NEW_MAP";
                break;
            }     
            case NEW_OBJ:   {
                op = "NEW_OBJ";
                break;
            }

            
            case FUNC_DEF: {
                op = "FUNC_DEF";
                break;
            }            
            case GEN_DEF: {
                op = "GEN_DEF";
                break;
            }    
            case CLASS_DEF: {
                op = "CLASS_DEF";
                break;
            }            
            case NAMESPACE_DEF: {
                op = "NAMESPACE_DEF";
                break;
            }            
            
            case YIELD: {
                op = "YIELD";
                break;
            }           
            case RET:   {
                op = "RET";
                break;
            }            
            
            case INVOKE:    {        
                op = "INVOKE";
                break;
            }
            case TAIL_CALL: {
                op = "TAIL_CALL";
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
            case EGETK: {
                op = "EGETK";
                break;
            }
            case GETK: {
                op = "GETK";
                break;
            }
            case SETK: {
                op = "SETK";
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
                        
            case INIT_CATCH_BLOCK: {
                op = "INIT_BLOCK";
                break;
            }            
            case INIT_FINALLY_BLOCK: {
                op = "INIT_FINALLY_BLOCK";
                break;
            }
            case END_BLOCK: {
                op = "END_BLOCK";
                break;
            }            
            case THROW: {
                op = "THROW";
                break;
            }            
            
            
            /* arithmetic operators */
            case ADD:    {
                op = "ADD";
                break;
            }
            case SUB:    {
                op = "SUB";
                break;
            }
            case MUL:    {
                op = "MUL";
                break;
            }
            case DIV:    {
                op = "DIV";
                break;
            }
            case MOD:    {
                op = "MOD";
                break;
            }
            case NEG: {
                op = "NEG";
                break;
            }
            
            case BSL:    {
                op = "BSL";
                break;
            }
            case BSR:    {
                op = "BSR";
                break;
            }
            case BNOT:    {
                op = "BNOT";
                break;
            }
            case XOR:    {
                op = "XOR";
                break;
            }
            case LOR:    {
                op = "LOR";
                break;
            }
            case LAND:    {
                op = "LAND";
                break;
            }
            
            case OR:    {
                op = "OR";
                break;
            }
            case AND:    {
                op = "AND";
                break;
            }
            case NOT:    {
                op = "NOT";
                break;
            }
            case REQ:    {
                op = "REQ";
                break;
            }
            case RNEQ:    {
                op = "RNEQ";
                break;
            }
            case EQ:    {
                op = "EQ";
                break;
            }
            case NEQ:    {
                op = "NEQ";
                break;
            }
            case GT:    {
                op = "GT";
                break;
            }
            case GTE:    {
                op = "GTE";
                break;
            }
            case LT:    {
                op = "LT";
                break;
            }
            case LTE:    {
                op = "LTE";
                break;
            }
            case IDX:    {
                op = "IDX";
                break;
            }
            case SIDX:    {
                op = "SIDX";
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
        POP = 1,                      /*      */
        DUP = 2,                      /*      */
        OPPOP = 3,                    /*      */        
                
        /* loading of values */
        LOAD_CONST = 4,               /* ARGx */
        LOAD_LOCAL = 5,               /* ARGx */
        LOAD_OUTER = 6,               /* ARGx */
        
        LOAD_NULL = 7,                /*      */
        LOAD_TRUE = 8,                /*      */
        LOAD_FALSE = 9,               /*      */
        
        /* named parameters */
        LOAD_NAME = 10,               /* ARGx */
        PARAM_END = 11,               /*      */
                        
        /* storage of values */
        STORE_LOCAL = 12,             /* ARGx */
        STORE_OUTER = 13,             /* ARGx */
        
        /* pseudo bytecodes */
        xLOAD_OUTER = 14,             /* ARGx */
        xLOAD_LOCAL = 15,             /* ARGx */
                
        /* jump instructions */
        JMP = 16,                     /* ARGsx */
        IFEQ = 17,                    /* ARGsx */
        
        IS_A = 18,                    /*      */
        
        /* value creation */
        NEW_ARRAY = 19,               /* ARGx */
        NEW_MAP = 20,                 /* ARGx */
        NEW_OBJ = 21,                 /* ARGx */        
        
        /* type declarations */
        FUNC_DEF = 22,                /* ARGx */
        GEN_DEF = 23,                 /* ARGx */
        CLASS_DEF = 24,               /* ARGx */
        NAMESPACE_DEF = 25,           /* ARGx */
                
        /* statement branching */
        YIELD = 26,                   /*      */
        RET = 27,                     /*      */
                
        /* method invocation */
        INVOKE = 28,                  /* ARG1, ARG2 */          
        TAIL_CALL = 29,               /* ARG1, ARG2 */
        
        /* member access */
        GET = 30,                     /*      */
        SET = 31,                     /*      */
        EGETK = 32,                   /* ARGx */
        GETK = 33,                    /* ARGx */
        SETK = 34,                    /* ARGx */
        
        GET_GLOBAL = 35,              /* ARGx */
        SET_GLOBAL = 36,              /* ARGx */
        
        GET_NAMESPACE = 37,           /* ARGx */

        IDX = 38,                     /*      */
        SIDX = 39,                    /*      */
        
        /* Exception handling */        
        INIT_CATCH_BLOCK = 40,        /* ARGsx*/    
        INIT_FINALLY_BLOCK = 41,      /* ARGsx*/
        END_BLOCK = 42,               /* ARG1 (0=pop index;1=clear error;2=exit if error)*/
        THROW = 43,                   /*      */
                        
        /* arithmetic operators */
        ADD = 44,                     /*      */
        SUB = 45,                     /*      */
        MUL = 46,                     /*      */
        DIV = 47,                     /*      */
        MOD = 48,                     /*      */
        NEG = 49,                     /*      */
    
        BSL = 50,                     /*      */
        BSR = 51,                     /*      */
        BNOT = 52,                    /*      */
        XOR = 53,                     /*      */
        LOR = 54,                     /*      */
        LAND = 55,                    /*      */
        
        OR = 56,                      /*      */
        AND = 57,                     /*      */
        NOT = 58,                     /*      */
        
        REQ = 59,                     /*      */
        RNEQ = 60,                    /*      */
        EQ = 61,                      /*      */
        NEQ = 62,                     /*      */
        GT = 63,                      /*      */
        GTE = 64,                     /*      */
        LT = 65,                      /*      */
        LTE = 66,                     /*      */
        
        /* debug */
        LINE = 67                     /* ARGx */
        ;
    
    
    private static final Map<String, Integer> opcodes = new HashMap<String, Integer>();
    static {        

        /* stack operators */
        opcodes.put("POP", POP);        
        opcodes.put("DUP", DUP);
        opcodes.put("OPPOP", OPPOP);
                
        opcodes.put("LOAD_CONST", LOAD_CONST);
        opcodes.put("LOAD_LOCAL", LOAD_LOCAL);
        opcodes.put("LOAD_OUTER", LOAD_OUTER);        
        opcodes.put("LOAD_NULL", LOAD_NULL);
        opcodes.put("LOAD_TRUE", LOAD_TRUE);
        opcodes.put("LOAD_FALSE", LOAD_FALSE);
        
        opcodes.put("LOAD_NAME", LOAD_NAME);
        opcodes.put("PARAM_END", PARAM_END);
        
        opcodes.put("STORE_LOCAL", STORE_LOCAL);
        opcodes.put("STORE_OUTER", STORE_OUTER);
                        
        opcodes.put("xLOAD_OUTER", xLOAD_OUTER);
        opcodes.put("xLOAD_LOCAL", xLOAD_LOCAL);        

        opcodes.put("JMP", JMP);
        opcodes.put("IFEQ", IFEQ);
        
        opcodes.put("IS_A", IS_A);
        
        opcodes.put("NEW_ARRAY", NEW_ARRAY);
        opcodes.put("NEW_MAP", NEW_MAP);
        opcodes.put("NEW_OBJ", NEW_OBJ);
        
        opcodes.put("FUNC_DEF", FUNC_DEF);
        opcodes.put("GEN_DEF", GEN_DEF);
        opcodes.put("CLASS_DEF", CLASS_DEF);
        opcodes.put("NAMESPACE_DEF", NAMESPACE_DEF);
        
        opcodes.put("YIELD", YIELD);
        opcodes.put("RET", RET);
        
                
        opcodes.put("INVOKE", INVOKE);
        opcodes.put("TAIL_CALL", TAIL_CALL);        
        
        
        /* object access */
        opcodes.put("GET", GET);
        opcodes.put("SET", SET);
        opcodes.put("EGETK", EGETK);
        opcodes.put("GETK", GETK);
        opcodes.put("SETK", SETK);
        opcodes.put("GET_GLOBAL", GET_GLOBAL);
        opcodes.put("SET_GLOBAL", SET_GLOBAL);
        opcodes.put("GET_NAMESPACE", GET_NAMESPACE);        
        
        
        /* exception handling */        
        opcodes.put("INIT_CATCH_BLOCK", INIT_CATCH_BLOCK);
        opcodes.put("INIT_FINALLY_BLOCK", INIT_FINALLY_BLOCK);  
        opcodes.put("END_BLOCK", END_BLOCK);        
        opcodes.put("THROW", THROW);


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
        opcodes.put("RNEQ", RNEQ);
        opcodes.put("EQ", EQ);
        opcodes.put("NEQ", NEQ);
        opcodes.put("GT", GT);
        opcodes.put("GTE", GTE);
        opcodes.put("LT", LT);
        opcodes.put("LTE", LTE);
        
        opcodes.put("IDX", IDX);
        opcodes.put("SIDX", SIDX);        
        
        opcodes.put("LINE", LINE);    
    }

}

