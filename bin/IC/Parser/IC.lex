package IC.Parser;

%%
%cup
%line
%column
%public
%type Token
%class Lexer
%function next_token
%scanerror LexicalError

%state YYINITIAL
%state SINGLE_LINE_COMMENT
%state MULTI_LINE_COMMENT
%state IN_QUOTE

%{
	private StringBuffer string = new StringBuffer();
	
	public int getLineNumber() { return yyline + 1; }
%}

%eofval{ 
	return new Token(sym.EOF, yytext(), yyline, yycolumn);
%eofval}

WHITE_SPACE = ([ \n\r\t])+

ALPHA=[A-Za-z]
DIGIT=[0-9]
ALPHA_NUMERIC={ALPHA}|{DIGIT}|_
IDENT={ALPHA}({ALPHA_NUMERIC})*

BAD_NAME=([0-9])+({ALPHA})+
INTEGER=[1-9]({DIGIT})* | 0
NON_INTEGER=0+([1-9])+({DIGIT})*
CLASS_NAME=([A-Z])+({ALPHA_NUMERIC})*

%%
<YYINITIAL> {WHITE_SPACE}		{ }

<YYINITIAL> "//" 				{ yybegin(SINGLE_LINE_COMMENT); }
<SINGLE_LINE_COMMENT> [^\n] 	{ }
<SINGLE_LINE_COMMENT> [\n]		{ yybegin(YYINITIAL); }

<YYINITIAL> { 
			"/*" 				{ yybegin(MULTI_LINE_COMMENT); }
			"*/"				{ throw new LexicalError("Token recognition error", yyline, yycolumn); }
}
<MULTI_LINE_COMMENT> {
	[\n]						{ }
	[^\n]						{ }
	"*/"						{ yybegin(YYINITIAL); }
	<<EOF>>						{ throw new LexicalError("Unclosed comment"); }
}

<YYINITIAL> {
	/* Operators */
	"+" 	{ return new Token(sym.PLUS, yytext(), yyline, yycolumn); }
	"-" 	{ return new Token(sym.MINUS, yytext(), yyline, yycolumn); }
	"*" 	{ return new Token(sym.MULTIPLY, yytext(), yyline, yycolumn); }
	"/" 	{ return new Token(sym.DIVIDE, yytext(), yyline, yycolumn); }
	"%" 	{ return new Token(sym.MOD, yytext(), yyline, yycolumn); }
	
	"==" 	{ return new Token(sym.EQUAL, yytext(), yyline, yycolumn); }
	"!=" 	{ return new Token(sym.NEQUAL, yytext(), yyline, yycolumn); }
	">" 	{ return new Token(sym.GT, yytext(), yyline, yycolumn); }
	">=" 	{ return new Token(sym.GTE, yytext(), yyline, yycolumn); }
	"<" 	{ return new Token(sym.LT, yytext(), yyline, yycolumn); }
	"<=" 	{ return new Token(sym.LTE, yytext(), yyline, yycolumn); }
	"!" 	{ return new Token(sym.LNEG, yytext(), yyline, yycolumn); }
	"&&" 	{ return new Token(sym.LAND, yytext(), yyline, yycolumn); }
	"||"	{ return new Token(sym.LOR, yytext(), yyline, yycolumn); }
	"=" 	{ return new Token(sym.ASSIGN, yytext(), yyline, yycolumn); }
	
	/* Brackets */
	"(" 				{ return new Token(sym.LP, yytext(), yyline, yycolumn); }
	")" 				{ return new Token(sym.RP, yytext(), yyline, yycolumn); }
	"[" 				{ return new Token(sym.LB, yytext(), yyline, yycolumn); }
	"]" 				{ return new Token(sym.RB, yytext(), yyline, yycolumn); }
	"{" 				{ return new Token(sym.LCBR, yytext(), yyline, yycolumn); }
	"}" 				{ return new Token(sym.RCBR, yytext(), yyline, yycolumn); }
	
	/* Other Symbols */
	"," 				{ return new Token(sym.COMMA, yytext(), yyline, yycolumn); }
	"." 				{ return new Token(sym.DOT, yytext(), yyline, yycolumn); }
	";" 				{ return new Token(sym.SEMI, yytext(), yyline, yycolumn); }
	
	/* String Quote */
	\"					{ string.setLength(0); yybegin(IN_QUOTE); }
	
	/* Saved Words */
	"extends" 			{ return new Token(sym.EXTENDS, yytext(), yyline, yycolumn); }
	"static"			{ return new Token(sym.STATIC, yytext(), yyline, yycolumn); }
	"void" 				{ return new Token(sym.VOID, yytext(), yyline, yycolumn); }
	"int" 				{ return new Token(sym.INT, yytext(), yyline, yycolumn); }
	"boolean" 			{ return new Token(sym.BOOLEAN, yytext(), yyline, yycolumn); }
	"string" 			{ return new Token(sym.STRING, yytext(), yyline, yycolumn); }
	"return" 			{ return new Token(sym.RETURN, yytext(), yyline, yycolumn); }
	"if" 				{ return new Token(sym.IF, yytext(), yyline, yycolumn); }
	"else" 				{ return new Token(sym.ELSE, yytext(), yyline, yycolumn); }
	"while" 			{ return new Token(sym.WHILE, yytext(), yyline, yycolumn); }
	"break" 			{ return new Token(sym.BREAK, yytext(), yyline, yycolumn); }
	"continue" 			{ return new Token(sym.CONTINUE, yytext(), yyline, yycolumn); }
	"this" 				{ return new Token(sym.THIS, yytext(), yyline, yycolumn); }
	"new" 				{ return new Token(sym.NEW, yytext(), yyline, yycolumn); }
	"length" 			{ return new Token(sym.LENGTH, yytext(), yyline, yycolumn); }
	"true" 				{ return new Token(sym.TRUE, yytext(), yyline, yycolumn); }
	"false" 			{ return new Token(sym.FALSE, yytext(), yyline, yycolumn); }
	"null"				{ return new Token(sym.NULL, yytext(), yyline, yycolumn); }
	"class" 			{ return new Token(sym.CLASS, yytext(), yyline, yycolumn); }
	{BAD_NAME}			{ throw new LexicalError("Invalid identifier or class name", yyline, yycolumn); }
	{INTEGER} 			{ 
							try {
								int num = Integer.parseInt(yytext());
								return new Token(sym.INTEGER, "INTEGER", num, yyline, yycolumn);
							} catch (NumberFormatException ex) {
								throw new LexicalError("Number format exception", yyline, yycolumn);
							}
						}
	{NON_INTEGER}		{ throw new LexicalError("Number format exception", yyline, yycolumn); }
}

<IN_QUOTE> {
	\"					{ yybegin(YYINITIAL); return new Token(sym.QUOTE, "QUOTE", string.toString(), yyline, yycolumn); }
	\\t					{ string.append("\\t"); }
	\\n					{ string.append("\\n"); }
	\\\"				{ string.append("\\\""); }
	\\\\				{ string.append("\\\\"); }
	[\n\r]				{ throw new LexicalError("String must close quote before end of line", yyline); }
	[^\n\r\"\\]+		{
							char[] quote = yytext().toCharArray();
							for (char c : quote) {
								if ((c >= 32) && (c <= 126)) {
									string.append(c);
							  	} else {
									throw new LexicalError("illegal character '" + c + "'", yyline, yycolumn);
							  	}
						  	}
						}
}

<YYINITIAL> {CLASS_NAME}	{ return new Token(sym.CLASS_ID, "CLASS_ID", yytext(), yyline, yycolumn); }

<YYINITIAL>	{IDENT}			{ return new Token(sym.ID, "ID", yytext(), yyline, yycolumn); }
<YYINITIAL> [^]				{ throw new LexicalError("Token recognition error", yyline, yycolumn); }