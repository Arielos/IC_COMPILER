package IC;

import java.io.File;

import static org.junit.Assert.*;

public class CompilerTest {

    @org.junit.Test
    public void testMain() throws Exception {

        String[] args = new String[3];
        Compiler compiler;
        String fileName;
        //fileName = "Calculations.ic";
        fileName = "BubbleSort.ic";
        //fileName = "Quicksort.ic";
        //fileName = "Comparison.ic";
        //fileName = "field_access.ic";
        //fileName = "inheritance_depth.ic";
        //fileName = "method_invocation_test.ic";
        //fileName = "method_parameters_order_test.ic";
        //fileName = "Unary.ic";
        //fileName = "FarmersLife.ic";
        //fileName = "Sieve.ic";

        File testFilesSourse = new File("C:\\Users\\LiorAr\\Documents\\Java SE intellij Projects\\ICCompiler\\test\\");
        args[1] = "-LC:\\Users\\LiorAr\\Documents\\Java SE intellij Projects\\ICCompiler\\test\\libic.sig";
        args[2] = "-dump-symtab";
        //args[2] = "-print-lir";

        args[0] = testFilesSourse.getAbsolutePath() + "\\" + fileName;
        System.out.println(args[0]);
        compiler = new Compiler(args);
        assertTrue(compiler.compile());

        /*
        File[] filesToTest = testFilesSourse.listFiles();

        if (filesToTest != null) {
            for (File fileToTest : filesToTest) {
                try {
                    if (fileToTest.getName().endsWith(".ic")) {
                        args[0] = testFilesSourse.getAbsolutePath() + "\\" + fileToTest.getName();
                        System.out.println(args[0]);
                        compiler = new Compiler(args);
                        compiler.compile();
                        System.out.println("");
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
        }
        */
    }
}

/*
BubbleSort.ic
Calculations.ic
CircularClasses.ic
Comparison.ic
DuplicateDeclarations.ic
errorsTests
example1.ic
example2.ic
field_access.ic
good_2_extenders.ic
good_array_refer.ic
good_arr_length.ic
good_decl.ic
good_init_vars.ic
good_method_call.ic
good_method_overriding.ic
good_stmt_while.ic
good_str_concatenation.ic
good_typecheck.ic
good_typecheck1.ic
good_typecheck2.ic
good_typecheck3.ic
good_typecheck4.ic
HidingChecks.ic
inheritance_depth.ic
InvalidMain.ic
KeywordsInBadPositions.ic
libic.sig
MergeSort.ic
method_invocation_test.ic
method_parameters_order_test.ic
microLirTests
myExample.ic
NoMain.ic
OrderOfThings.ic
output
Quicksort.ic
removed
scoping_test.ic
Sieve.ic
test.ic
test2.ic
TestBonus2Case.ic
testT10.lir
TwoMains.ic
TypeChecks.ic
Unary.ic
UndeclaredSymbols.ic
VirtualTests.ic
*/