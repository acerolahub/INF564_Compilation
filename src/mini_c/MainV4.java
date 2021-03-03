package mini_c;

import java.io.IOException;
import java.io.InputStream;

public class Main {
	
	static boolean parse_only = false;
	static boolean type_only = false;
	static String file = null;
	
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
			else {
				if (file != null) usage();
				if (!arg.endsWith(".c")) usage();
				file = arg;
			}
		if (file == null) file = "test.c"; // pour faciliter les tests
		
        java.io.Reader reader = new java.io.FileReader(file);
        Lexer lexer = new Lexer(reader);
        System.out.println("parsing...");
        MyParser parser = new MyParser(lexer);
        
        try {
        	Pfile f = (Pfile) parser.parse().value;
        	System.out.println("Yosh\n\n");
            if (parse_only) System.exit(0);
            System.out.println("typing...");
            Typing typer = new Typing();
            typer.visit(f);
            File tf = typer.getFile();
            System.out.println(tf);
            
            
            /**** Now structs *****/
            System.out.println("\n\nStructs");
            System.out.println("Number of structs " + typer.structure.size());
            for(Structure tmp : typer.structure.values()) {
            	System.out.println("Name: " + tmp.str_name);
            	System.out.println(tmp.fields.size());
            	for(Field fld : tmp.fields.values()) {
            		if(fld.field_typ instanceof Tint)
            			System.out.print("int ");
            		System.out.println(fld.field_name);
            	}
            	System.out.println();
            }
            
            System.out.println(typer.getFile()); 
            
            
                     
            
            if (type_only) System.exit(0);
            
            RTLfile rtl = (new ToRTL()).translate(tf);
            //if (debug) 
            
            rtl.print();
            //if (true) { new RTLinterp(rtl); System.exit(0); }
            
            System.out.println("MOIUIIII"); 
            int i = 0 ; 
            for (RTLfun ff: rtl.funs) {
            	System.out.println(i++); 
            	for(Label ll: ff.body.graph.keySet()) {
            		System.out.println(ll + " : "+ff.body.graph.get(ll) ); 
            	}
            }
            
        } catch (Error e) {
        	System.out.println("error: " + e.getMessage());
        	
        	System.exit(1);
        }
        catch (Exception e) {
        	System.out.println("error: " + e.getMessage());
        	
        	System.exit(1);
		}
	}
	
	static void cat(InputStream st) throws IOException {
	  while (st.available() > 0) {
      System.out.print((char)st.read());
    }
	}
	
}
