package IC;

import java.io.FileReader;
import java.io.IOException;

import java_cup.runtime.Symbol;
import IC.AST.ICClass;
import IC.AST.PrettyPrinter;
import IC.AST.Program;
import IC.Parser.Lexer;
import IC.Parser.LexicalError;
import IC.Parser.LibraryParser;
import IC.Parser.Parser;
import IC.Parser.SyntaxError;
import IC.SemanticAnalysis.SemanticEvaluator;
import IC.SemanticAnalysis.SemanticTablePrinter;
import IC.SemanticAnalysis.TableConstructor;

public class Compiler {
	public static void main(String[] args) {

		FileReader txtFile = null;
		FileReader libFile = null;

		Symbol textParseSymbol = null;
		Symbol libParseSymbol = null;

		if (args.length > 2) {
			try {
				txtFile = new FileReader(args[0]);
				libFile = new FileReader(args[1].substring(2));

				/* create a scanning object */
				Lexer textScanner = new Lexer(txtFile);
				Lexer libScanner = new Lexer(libFile);

				/* create a parsing object */
				Parser textParser = new Parser(textScanner);
				LibraryParser libParser = new LibraryParser(libScanner);

				textParseSymbol = textParser.parse();
				Program textRoot = (Program) textParseSymbol.value;

				libParseSymbol = libParser.parse();
				ICClass libRoot = (ICClass) libParseSymbol.value;

				textRoot.getClasses().add(0, libRoot);
				TableConstructor textConstr = new TableConstructor(args[0], textRoot);
				textConstr.construct();

				PrettyPrinter textPrinter = new PrettyPrinter(args[0]);
				textPrinter.print(textRoot);

				SemanticTablePrinter semanticPrinter = new SemanticTablePrinter(
						args[0]);

				for (String arg : args) {
					if (arg.equals("-print-ast")) {
						// textPrinter.print(textRoot);
						System.out.println(textRoot.accept(semanticPrinter));
					}
				}

				SemanticEvaluator evaluetor = new SemanticEvaluator(textRoot);
				//evaluetor.evaluate();

			} catch (LexicalError lexicalError) {
				System.out
						.println("LexicalError: " + lexicalError.getMessage());
			} catch (SyntaxError syntaxError) {
				System.out.println("SyntaxError: " + syntaxError.getMessage());
			} catch (NullPointerException nullError) {
				nullError.printStackTrace();
				System.out.println("Null Error: " + nullError.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("IO Error (brutal exit) " + e.getMessage()
						+ " " + textParseSymbol.value);
			} finally {
				try {
					if (txtFile != null) {
						txtFile.close();
					}
				} catch (IOException ex) {
					System.out.println("txtFile.close()");
				}
				try {
					if (libFile != null) {
						libFile.close();
					}
				} catch (IOException ex) {
					System.out.println("libFile.close()");
				}
			}

		}
	}
}