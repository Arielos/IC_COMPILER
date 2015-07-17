package IC;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import IC.LirTranslate.Translator;
import microLIR.Main;
import IC.AST.ICClass;
import IC.AST.Program;
import IC.Parser.Lexer;
import IC.Parser.Parser;
import IC.AST.PrettyPrinter;
import IC.Parser.SyntaxError;
import IC.TypeTable.TypeTable;
import IC.Parser.LexicalError;
import IC.Parser.LibraryParser;
import java_cup.runtime.Symbol;
import IC.SemanticAnalysis.Tester;
import IC.SemanticAnalysis.TypeAnalyzer;
import IC.SemanticAnalysis.ScopeChecker;
import IC.SemanticAnalysis.SemanticError;
import IC.LirTranslate.OptimizedTranslator;
import IC.LirTranslate.SethiUllmanAlgorithm;
import IC.SemanticAnalysis.TableConstructor;
import IC.SemanticAnalysis.TypeTableConstructor;
import IC.SemanticAnalysis.SemanticTablePrinter;

public class Compiler {

    private static final boolean debug = false;
    private static final boolean debugLir = true;

    private final String[] programArgs;

	public static void main(String[] args) {
        new Compiler(args).compile();
	}

    public Compiler(String[] programArgs) {
        this.programArgs = programArgs;
    }

    public boolean compile() {
        String programPath = "";
        boolean printAst = false, dumpSymtab = false, printLir = false, result = false;

        if (programArgs.length > 0) {
            try {
                programPath = programArgs[0];
                Program textRoot = createAST(programPath);

                for (String arg : programArgs) {
                    if (arg.equals("-print-ast")) {
                        printAst = true;
                    } else if (arg.equals("-dump-symtab")) {
                        dumpSymtab = true;
                    } else if (arg.equals("-print-lir")) {
                        printLir = true;
                    } else if (arg.startsWith("-L")) {
                        /* adding the library to the main tree */
                        textRoot.getClasses().add(0, addLibrary(arg));
                    }
                }

                TypeTableConstructor typeTableConstructor = new TypeTableConstructor(textRoot);
                typeTableConstructor.run();

                TableConstructor tableConstructor = new TableConstructor(programPath, textRoot);
                tableConstructor.construct();

                Tester scopeChecker = new ScopeChecker(textRoot);
                scopeChecker.test();

                Tester analyzer = new TypeAnalyzer(textRoot);
                analyzer.test();

                if (printAst) {
                    PrettyPrinter textPrinter = new PrettyPrinter(programPath);
                    System.out.print(textPrinter.print(textRoot) + "\n");
                } else if (dumpSymtab) {
                    SemanticTablePrinter semanticPrinter = new SemanticTablePrinter(programPath);
                    System.out.print(semanticPrinter.print(textRoot));
                    System.out.print(TypeTable.print(programPath) + "\n");
                } else if (printLir) {
                    if (scopeChecker.isAllGood() && analyzer.isAllGood()) {
                        SethiUllmanAlgorithm sethiUllmanAlgorithm = new SethiUllmanAlgorithm(textRoot);
                        sethiUllmanAlgorithm.analyze();

                        //Translator translator = new Translator(textRoot);
                        OptimizedTranslator translator = new OptimizedTranslator(textRoot);
                        String output = translator.translate(debug);

                        if (debug) {
                            System.out.println("-------------- Debug mode is ON --------------");
                            System.out.println(output);
                            System.out.println("---------------- End of output ---------------");
                        } else {
                            LirFileCreator lirFileCreator = new LirFileCreator(programPath, output);
                            lirFileCreator.createFile();

                            if (debugLir) {
                                Main.main(new String[]{lirFileCreator.getFilePath(), "-verbose:2"});
                            } else {
                                Main.main(new String[]{lirFileCreator.getFilePath()});
                            }
                        }

                        result = true;
                    } else {
                        System.err.println(scopeChecker.getErrors());
                        System.err.println(analyzer.getErrors());
                    }
                }
            } catch (FileNotFoundException fnfException) {
                System.err.println("The file " + programPath + " not found");
            } catch (LexicalError lexicalError) {
                System.err.println("LexicalError: " + lexicalError.getMessage());
            } catch (SyntaxError syntaxError) {
                System.err.println("SyntaxError: " + syntaxError.getMessage());
            } catch (SemanticError semanticError) {
                System.err.println("SemanticError: " + semanticError.getMessage());
            } catch (NullPointerException nullError) {
                nullError.printStackTrace();
                System.err.println("Null Error: " + nullError.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private Program createAST(String programPath) {
        FileReader txtFile = null;
        Symbol textParseSymbol = null;

        try {
            txtFile = new FileReader(programPath);

            /* create a scanning object */
            Lexer textScanner = new Lexer(txtFile);

            /* create a parsing object */
            Parser textParser = new Parser(textScanner);
            textParseSymbol = textParser.parse();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("IO Error (brutal exit) " + ex.getMessage() + " " + textParseSymbol.value);
        } finally {
            try {
                if (txtFile != null) {
                    txtFile.close();
                }
            } catch (IOException ex) {
                System.err.println("txtFile.close()");
            }
        }

        return (Program) textParseSymbol.value;
    }

    private ICClass addLibrary(String libraryPath) throws Exception {
        FileReader libFile = null;
        Symbol libParseSymbol = null;

        try {
            libFile = new FileReader(libraryPath.substring(2));

            /* create a scanning object */
            Lexer libScanner = new Lexer(libFile);

            /* create a parsing object */
            LibraryParser libParser = new LibraryParser(libScanner);
            libParseSymbol = libParser.parse();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("IO Error (brutal exit) " + ex.getMessage() + " " + libParseSymbol.value);
        } finally {
            try {
                if (libFile != null) {
                    libFile.close();
                }
            } catch (IOException ex) {
                System.err.println("libFile.close()");
            }
        }

        return (ICClass) libParseSymbol.value;
    }
}