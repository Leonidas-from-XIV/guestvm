package com.sun.guestvm.tools.ajtrace.viewer;
import java.io.*;
import java.util.*;

public class Tokenize {

  public static void main(String[] args) {
    try {
    new Tokenize(args[0]).doTestTokenize();
    } catch (Exception ex) {
      System.err.println(ex);
      ex.printStackTrace();
    }
  }

  private String dataFile;
  private StreamTokenizer st = null;

  private Tokenize(String dataFile) throws IOException {
    this.st = new StreamTokenizer(new BufferedReader(new FileReader(dataFile)));
    resetStreamTokenizer(st);
    this.dataFile = dataFile;
  }

  private void doTestTokenize() throws IOException {
    st.nextToken();
    while (st.ttype != StreamTokenizer.TT_EOF) {
      switch (st.ttype) {
      case StreamTokenizer.TT_WORD:
	System.out.println("TT_WORD: " + st.sval);
	break;
      case StreamTokenizer.TT_EOL:
	System.out.println("TT_EOL");
	break;
      case StreamTokenizer.TT_NUMBER:
	System.out.println("TT_NUMBER: " + st.nval);
	break;
      default:
	System.out.println("OTHER: " + st.ttype + " " + st.sval);
      }
      st.nextToken();
    }
  }

  public static void resetStreamTokenizer(StreamTokenizer xst) {
      xst.resetSyntax();
      xst.wordChars('a', 'z');
      xst.wordChars('A', 'Z');
      xst.wordChars(128 + 32, 255);
      xst.whitespaceChars(' ', ' '); xst.whitespaceChars('\t', '\t');
      xst.whitespaceChars('\t', '\t'); xst.whitespaceChars(',', ',');
      xst.whitespaceChars('=', '='); xst.whitespaceChars(';', ';'); 
      xst.whitespaceChars(';', ';'); xst.whitespaceChars(':', ':'); 
      xst.commentChar('#');
      xst.quoteChar('"');
      xst.quoteChar('\'');
      xst.eolIsSignificant(true);
      xst.wordChars('0', '9');
      xst.wordChars('[', '['); xst.wordChars(']', ']'); xst.wordChars('@', '@');
      xst.wordChars('.', '.'); xst.wordChars('-', '-');
      xst.wordChars('$', '$'); xst.wordChars('_', '_'); 
  }
}
