/*
 * see license.txt 
 */
package leola.vm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import leola.ast.Expr;
import leola.ast.ProgramStmt;
import leola.ast.ReturnStmt;
import leola.ast.Stmt;
import leola.frontend.ErrorCode;
import leola.frontend.ParseException;
import leola.frontend.Parser;
import leola.frontend.Scanner;
import leola.frontend.Source;
import leola.frontend.tokens.Token;
import leola.frontend.tokens.TokenType;
import leola.vm.compiler.Bytecode;
import leola.vm.compiler.Compiler;
import leola.vm.exceptions.LeolaRuntimeException;
import leola.vm.types.LeoObject;
import leola.vm.types.LeoUserFunction;

import static leola.frontend.tokens.TokenType.*;

/**
 * Read Evaluate Print Loop
 * 
 * @author Tony
 *
 */
public class Repl {

    private Leola runtime;
    
    public Repl(Leola runtime) {
        this.runtime = runtime;
    }
    
    /**
     * Delegates to standard in/out/err streams
     * 
     * @throws Exception
     */
    public void execute() throws Exception {
       execute(System.out, System.err, System.in); 
    }

    /**
     * Executes the Read Evaluate Print Loop
     * 
     * @param printStream
     * @param errStream
     * @param inputStream
     * @throws Exception
     */
    public void execute(PrintStream printStream, PrintStream errStream, InputStream inputStream) throws Exception {    
        printStream.println("");
        printStream.println("Leola v" + Leola.VERSION);        
        printStream.println("Date: " + new Date());
        printStream.println("");
        printStream.println("Type `quit()` to exit");
        printStream.println("\n");
        
        InputStreamReader input = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(input);

        runtime.put("quit", new LeoUserFunction() {
            @Override
            public LeoObject xcall() throws LeolaRuntimeException {             
                System.exit(0);
                return super.xcall();
            }
        });
        
        
        Compiler compiler = new Compiler(runtime);
        
        for (;;) {
            try {
                printStream.print(">>> ");
                
                ProgramStmt program = readStmt(reader, printStream);                 
                List<Stmt> stmts = program.getStatements();
                
                boolean isStmt = true;
                if(!stmts.isEmpty()) {
                    Stmt lastStmt = stmts.get(stmts.size()-1);
                    if (lastStmt instanceof Expr) {               
                        stmts.set(stmts.size()-1, new ReturnStmt((Expr)lastStmt));
                        isStmt = false;
                    }
                }
                
                Bytecode bytecode = compiler.compile(program);
                LeoObject result = runtime.execute(bytecode);
                
                if(!isStmt) {
                    printStream.println(result);
                }
            }
            catch(Exception e) {
                errStream.println(e.getMessage());
            }
        }
    }        
    
    private ProgramStmt readStmt(BufferedReader reader, PrintStream out) throws Exception {
        boolean isStatementCompleted = true;
        
        ProgramStmt program = null;
        StringBuilder sourceBuffer = new StringBuilder();
        String line = null;
        do {
            try {
                line = readLine(reader);
                sourceBuffer.append(line).append("\n");
                
                Source source = new Source(new StringReader(sourceBuffer.toString()));
                Scanner scanner = new Scanner(source);
                
                Parser parser = new Parser(scanner);
                program = parser.parse();
                
                isStatementCompleted = true;
            }
            catch(ParseException e) {
                Token token = e.getToken();
                TokenType type = token != null ? token.getType() : null;
                
                // if this is an end of file OR an Error of type Unexpected end of file, the user
                // has submitted a partial statement
                if(type != null && (type.equals(END_OF_FILE) || 
                        (type.equals(ERROR) && token.getValue().equals(ErrorCode.UNEXPECTED_EOF)))) {
                    
                    isStatementCompleted = false;
                    if(!line.trim().isEmpty()) {
                        out.print("> ");
                    }
                }
                else {
                    throw e;
                }
            }
        }
        while(!isStatementCompleted); 
        
        return program;
    }
    
    private String readLine(BufferedReader reader) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String line = reader.readLine();
        buffer.append(line).append("\n");
        
        // if the user pasted in a chunk of code
        // the buffer will be able to read in the 
        // full input, however we still want to
        // block and wait for the user to press 
        // enter for the final input
        boolean pasted = false;
        while(reader.ready()) {
            line = reader.readLine();
            buffer.append(line).append("\n");
            
            pasted = true;
        }
        
        if(pasted) {
            line = reader.readLine();
            buffer.append(line).append("\n");
        }
                
        return buffer.toString();
    }
}
