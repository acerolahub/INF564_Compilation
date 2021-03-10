package mini_c;

import java.io.IOException;
import java.io.InputStream;

public class Main {

  static boolean parse_only = false;
  static boolean type_only = false;
  static boolean interp_rtl = false;
  static boolean interp_ertl = true;
  static boolean debug = false;
  //static String file=null;
 static String file="test.c";
  //static String file = "tests/exec/spilled1.c";
  //file = "tests/exec/spilled1.c";

  static void usage() {
    System.err.println("mini-c [--parse-only] [--type-only] file.c");
    System.exit(1);
  }

  public static void main(String[] args) throws Exception {
    
	for (String arg: args)
      if (arg.equals("--parse-only"))
        parse_only= true;
      else if (arg.equals("--type-only"))
        type_only = true;
      else if (arg.equals("--interp-rtl"))
        interp_rtl = true;
      else if (arg.equals("--debug"))
        debug = true;
      else {
        if (file != null) usage();
        if (!arg.endsWith(".c")) usage();
        file = arg;
      }
    if (file == null) usage();

    java.io.Reader reader = new java.io.FileReader(file);
    Lexer lexer = new Lexer(reader);
    MyParser parser = new MyParser(lexer);
    Pfile f = (Pfile) parser.parse().value;
    if (parse_only) System.exit(0);
    Typing typer = new Typing();
    typer.visit(f);
    File tf = typer.getFile();
    
    if(debug) System.out.println(tf+"\n\n\n\n\n\t\tRTL_FILE");
    
    if (type_only) System.exit(0);
    RTLfile rtl = (new ToRTL()).translate(tf);
    ERTLfile ertl = (new ToERTL()).translate(rtl);
    if (debug) rtl.print();
    if (debug) ertl.print();
    if(debug) {
    System.out.println("MOIUIIII"); 
    int i = 0 ; 
    for (RTLfun ff: rtl.funs) {
    	System.out.println(i++); 
    	for(Label ll: ff.body.graph.keySet()) {
    		System.out.println(ll + " : "+ff.body.graph.get(ll) ); 
    	}
    	}
    }
    for (ERTLfun ff: ertl.funs) {
    
    		Liveness LL = new Liveness(ff.body); 
    		LL.print(ff.entry);; 
    	
    }
    if (interp_rtl) { new RTLinterp(rtl); System.exit(0); }
    if (interp_ertl) { new ERTLinterp(ertl); System.exit(0); }
    
  }

}
