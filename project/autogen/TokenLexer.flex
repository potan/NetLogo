// -*- Java -*- (tell Emacs to use Java mode)
package org.nlogo.lex ;

import org.nlogo.api.Token;
import org.nlogo.api.TokenType;

// Mostly the following spec is reasonably straightforward.
// There is some slight weirdness about supporting the triple-underscore
// syntax, the one that lets you type ___foo as a shorthand for
// __magic-open "foo".  This is handled by the MAGIC-OPEN state
// with the aid of the distinction between
// IDENTIFIER_CHAR_NOT_UNDERSCORE and IDENTIFIER_CHAR. - ST 2/5/07

// Since this is automatically generated code it's not surprising
// it'd produce a few warnings - ST 3/6/08
@SuppressWarnings({"unused","fallthrough"})

%%

%{
  private final TokenMapper tokenMapper ;
  private final String fileName ;
  private final boolean allowRemovedPrimitives ;
  private StringBuilder literalBuilder = null ;
  private int literalStart = -1 ;
  private int literalNestingLevel = 0 ;

  // this is very annoying, but I can't figure out any other way to
  // get at the Scala inner objects from Java - ST 7/7/11
  private static final TokenType TokenType_EOF = getTokenType("EOF");
  private static final TokenType TokenType_OPEN_PAREN = getTokenType("OPEN_PAREN");
  private static final TokenType TokenType_CLOSE_PAREN = getTokenType("CLOSE_PAREN");
  private static final TokenType TokenType_OPEN_BRACKET = getTokenType("OPEN_BRACKET");
  private static final TokenType TokenType_CLOSE_BRACKET = getTokenType("CLOSE_BRACKET");
  private static final TokenType TokenType_OPEN_BRACE = getTokenType("OPEN_BRACE");
  private static final TokenType TokenType_CLOSE_BRACE = getTokenType("CLOSE_BRACE");
  private static final TokenType TokenType_CONSTANT = getTokenType("CONSTANT");
  private static final TokenType TokenType_IDENT = getTokenType("IDENT");
  private static final TokenType TokenType_COMMAND = getTokenType("COMMAND");
  private static final TokenType TokenType_REPORTER = getTokenType("REPORTER");
  private static final TokenType TokenType_KEYWORD = getTokenType("KEYWORD");
  private static final TokenType TokenType_COMMA = getTokenType("COMMA");
  private static final TokenType TokenType_COMMENT = getTokenType("COMMENT");
  private static final TokenType TokenType_VARIABLE = getTokenType("VARIABLE");
  private static final TokenType TokenType_BAD = getTokenType("BAD");
  private static final TokenType TokenType_LITERAL = getTokenType("LITERAL");

  private static TokenType getTokenType(String name) {
    try {
      return (TokenType) Class.forName("org.nlogo.api.TokenType$" + name + "$").getField("MODULE$").get(null);
    }
    catch(IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    }
    catch(NoSuchFieldException ex) {
      throw new IllegalStateException(ex);
    }
    catch(ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    }
  }

  void beginLiteral()
  {
    literalStart = yychar ;
    literalBuilder = new StringBuilder() ;
  }

  void addToLiteral()
  {
    literalBuilder.append( yytext() ) ;
  }

  Token endLiteral()
  {
    String text = literalBuilder.toString() ;
    literalBuilder = null ;
    return new Token( text , TokenType_LITERAL , text ,
              literalStart , literalStart + text.length() , fileName ) ;
  }

  Token ident()
  {
    String text = yytext() ;
    if( tokenMapper.isKeyword( text ) )
    {
      return new Token( text , TokenType_KEYWORD , text.toUpperCase() ,
                yychar , yychar + text.length() , fileName ) ;
    }
    else if( tokenMapper.isCommand( text.toUpperCase() )
         && ( allowRemovedPrimitives ||
            ! tokenMapper.wasRemoved( text.toUpperCase() ) ) )
    {
      org.nlogo.api.TokenHolder instr = tokenMapper.getCommand( text ) ;
      Token tok = new Token( text , TokenType_COMMAND , instr ,
                   yychar, yychar + text.length() , fileName ) ;
      instr.token( tok ) ;
      return tok ;
    }
    else if( tokenMapper.isReporter( text )
         && ( allowRemovedPrimitives ||
            ! tokenMapper.wasRemoved( text.toUpperCase() ) ) )
    {
      org.nlogo.api.TokenHolder instr = tokenMapper.getReporter( text ) ;
      Token tok = new Token( text , TokenType_REPORTER , instr ,
                   yychar , yychar + text.length() , fileName ) ;
      instr.token( tok ) ;
      return tok ;
    }
    else if( tokenMapper.isVariable( text ) )
    {
      return new Token( text , TokenType_VARIABLE , text.toUpperCase() ,
                yychar , yychar + text.length() , fileName ) ;
    }
    else if( tokenMapper.isConstant( text ) )
    {
      return new Token( text , TokenType_CONSTANT , tokenMapper.getConstant( text ) ,
                yychar , yychar + text.length() , fileName ) ;
    }
    else
    {
      return new Token( text , TokenType_IDENT , text.toUpperCase() ,
                yychar , yychar + text.length() , fileName ) ;
    }
  }
%}
/* this option decreases code size; see JFlex documentation */
%switch
%class TokenLexer
%ctorarg TokenMapper tokenMapper
%ctorarg String fileName
%ctorarg boolean allowRemovedPrimitives
%init{
  this.tokenMapper = tokenMapper ;
  this.fileName = fileName ;
  this.allowRemovedPrimitives = allowRemovedPrimitives ;
%init}
%unicode
%char
%type Token
%state MAGIC_OPEN
%state LITERAL

STRING_TEXT=(\\\"|\\r|\\n|\\t|\\\\|\\[^\"]|[^\r\n\"\\])*
NONNEWLINE_WHITE_SPACE_CHAR=[ \t\b\012]
LETTER=[:letter:]
DIGIT=[:digit:]
IDENTIFIER_CHAR_NOT_UNDERSCORE={LETTER} | {DIGIT} | [\.?=\*!<>:#\+/%\$\^\'&-]
IDENTIFIER_CHAR={IDENTIFIER_CHAR_NOT_UNDERSCORE} | _

%%

<YYINITIAL> \{\{ {
  yybegin( LITERAL ) ;
  beginLiteral() ;
  addToLiteral() ;
  literalNestingLevel = 0 ;
}

<LITERAL> \}\} {
  addToLiteral() ;
  if( literalNestingLevel == 0 )
  {
    yybegin( YYINITIAL ) ;
    return endLiteral() ;
  }
  literalNestingLevel-- ;
 }

<LITERAL> \{\{ {
  literalNestingLevel++ ;
  addToLiteral() ;
 }

<LITERAL> . {
  addToLiteral() ;
}

<LITERAL> \n|\r {
  yybegin( YYINITIAL ) ;
  return new Token( "" , TokenType_BAD , "End of line reached unexpectedly" ,
            yychar , yychar , fileName ) ;
}

<LITERAL> <<EOF>> {
  yybegin( YYINITIAL ) ;
  return new Token( "" , TokenType_BAD , "End of file reached unexpectedly" ,
            yychar , yychar , fileName ) ;
}


<YYINITIAL> "," { return new Token( yytext() , TokenType_COMMA         , null , yychar , yychar + 1, fileName ) ; }
<YYINITIAL> "{" { return new Token( yytext() , TokenType_OPEN_BRACE    , null , yychar , yychar + 1, fileName ) ; }
<YYINITIAL> "}" { return new Token( yytext() , TokenType_CLOSE_BRACE   , null , yychar , yychar + 1, fileName ) ; }
<YYINITIAL> "[" { return new Token( yytext() , TokenType_OPEN_BRACKET  , null , yychar , yychar + 1, fileName ) ; }
<YYINITIAL> "]" { return new Token( yytext() , TokenType_CLOSE_BRACKET , null , yychar , yychar + 1, fileName ) ; }
<YYINITIAL> "(" { return new Token( yytext() , TokenType_OPEN_PAREN    , null , yychar , yychar + 1, fileName ) ; }
<YYINITIAL> ")" { return new Token( yytext() , TokenType_CLOSE_PAREN   , null , yychar , yychar + 1, fileName ) ; }

<YYINITIAL> {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

<YYINITIAL> \n|\r { }

<YYINITIAL> ;.* {
  String text = yytext() ;
  return new Token( text , TokenType_COMMENT , null ,
            yychar , yychar + text.length() , fileName ) ;
}

<YYINITIAL> -?\.?[0-9]{IDENTIFIER_CHAR}* {
  String text = yytext() ;
  scala.util.Either<String, Double> result = org.nlogo.api.NumberParser.parse( text ) ;
  TokenType resultType =
    result.isLeft() ? TokenType_BAD : TokenType_CONSTANT ;
  Object resultValue =
    result.isLeft() ? result.left().get() : result.right().get() ;
  return new Token(
    text , resultType , resultValue ,
    yychar , yychar + text.length() , fileName ) ;
}

<YYINITIAL> ___ {
  yybegin( MAGIC_OPEN ) ;
  org.nlogo.api.TokenHolder cmd = tokenMapper.getCommand( "__magic-open" ) ;
  Token token = new Token( "__magic-open" , TokenType_COMMAND , cmd , yychar , yychar + 3 , fileName ) ;
  cmd.token( token ) ;
  return token ;
}

<MAGIC_OPEN> \n {
  yybegin( YYINITIAL ) ;
}
<MAGIC_OPEN> {IDENTIFIER_CHAR}* {
  String text = yytext() ;
  yybegin( YYINITIAL ) ;
  return new Token
    ( "\"" + text + "\"" , TokenType_CONSTANT , text ,
      yychar , yychar + text.length() , fileName ) ;
}

<YYINITIAL> {IDENTIFIER_CHAR_NOT_UNDERSCORE}{IDENTIFIER_CHAR}* {
  return ident() ;
}

<YYINITIAL> _{IDENTIFIER_CHAR_NOT_UNDERSCORE}{IDENTIFIER_CHAR}* {
  return ident() ;
}

<YYINITIAL> __{IDENTIFIER_CHAR_NOT_UNDERSCORE}{IDENTIFIER_CHAR}* {
  return ident() ;
}

<YYINITIAL> \"{STRING_TEXT}\" {
  String text = yytext() ;
  try
  {
    return new Token
      ( text , TokenType_CONSTANT ,
        org.nlogo.api.StringUtils.unEscapeString( text.substring( 1 , text.length() - 1 ) ) ,
        yychar , yychar + text.length() , fileName ) ;
  }
  catch( IllegalArgumentException ex )
  {
    return new Token( text , TokenType_BAD , "Illegal character after backslash" ,
              yychar , yychar + text.length() , fileName ) ;
  }
}

<YYINITIAL> \"{STRING_TEXT} {
  String text = yytext() ;
  return new Token( text , TokenType_BAD , "Closing double quote is missing" ,
            yychar , yychar + yytext().length() , fileName ) ;
}

. {
  String text = yytext() ;
  return new Token( text , TokenType_BAD , "This non-standard character is not allowed." ,
            yychar , yychar + 1 , fileName ) ;
}
