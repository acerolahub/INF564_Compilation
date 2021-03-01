package mini_c;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

class ToRTL extends EmptyVisitor{
	
	private RTLfile rtlfile;
	
	
	RTL res; // RTL result of the last visit 
	RTLgraph g; 
	HashMap<String, Register> fun_locals; 
	Stack <RTLgraph> stack_graph; 
	RTLfun fun; //the current function
	final private static int IDENT_ECONST=0, IDENT_EACCES_LOCAL=1; 
	
	boolean first_event; 
	
	RTLfile getRTLFile() {
		if (rtlfile == null)
			throw new Error("typing not yet done!");
		return rtlfile;
	}
	
	 RTLfile translate(File f){
		 this.visit(f);
		 return rtlfile; 
	 }
	 
	 public void visit(File f) {
		 System.out.println("File Deb"); 
		 rtlfile = new RTLfile(); 
		 for (Decl_fun d: f.funs) {
			 this.visit(d);
		 }
		 System.out.println("File fin"); 	
	 }
	 
	 public void visit(Decl_fun d) {
		 System.out.println("Decl_fun Deb"); 
		 RTLfun tmp = new RTLfun(d.fun_name); 
		 fun = tmp; 
		 first_event = true; 
		 tmp.exit = new Label(); 
		 tmp.entry = new Label(); 
		 
		 tmp.result = new Register(); 
		 Register r; 
		 fun_locals = new HashMap<>();
		 for(Decl_var v: d.fun_formals) {
			 r = new Register(); 
			 tmp.formals.add(r); 
			 fun_locals.put(v.name, r); 
		 }
		 tmp.body = this.visit(d.fun_body); 
		 System.out.println("grap "+ tmp.body.graph.size()); 
		 rtlfile.funs.add(tmp); 
		 System.out.println("Decl_fun Fin"); 
	 }
	
	 
	 
	 public RTLgraph visit(Stmt stmt) {
		 System.out.println("Stmt Deb"); 
		 RTLgraph tmp = new RTLgraph(); 
		 
		 if (stmt instanceof Sblock) {
				
			 this.visit((Sblock)stmt); 
			 
			 if(first_event) {
				 tmp.graph.put(fun.entry,res ); 
				 first_event = false; 
			 }
			 else {
				 tmp.graph.put(new Label(),res ); 
				 first_event = false; 
			 }

		 } 
		 
		 if (stmt instanceof Sreturn) {
			
			 this.visit((Sreturn)stmt);  
			 if(first_event) {
				 tmp.graph.put(fun.entry,res ); 
				 first_event = false; 
			 }
			 else {
				 tmp.graph.put(new Label(),res ); 
				 first_event = false; 
			 }
		 }
		 
		 System.out.println("grap "+ tmp.graph.size()); 
		 System.out.println("Stmt Fin"); 
		 
		 return tmp; 
	 }
	 
	 
	 public void visit(Sblock sb) {
		 //Rmbinop tmp; 
		 
		 System.out.println("Sblock Deb"); 
		 Register r; 
		 for(Decl_var v: sb.dl) {
			 r = new Register(); 
			 fun_locals.put(v.name, r); 
			 fun.locals.add(r); 
		 }
		 for(Stmt stmt: sb.sl) {
			 if (stmt instanceof Sreturn) {
				 System.out.println(stmt instanceof Sreturn );
				 this.visit((Sreturn)stmt); 
			 }
		 }
		 System.out.println("Sblock Fin"); 
		 return ; 
		 
	 }
	 
	 /*
	 
	 public void visit(Sreturn se) {
		 //Rmbinop tmp; 
		 System.out.println("Sreturn Deb"); 
		 if(se.e instanceof Econst) {
			 System.out.println("return econst");
			 res = new Rconst(((Econst)se.e).i, fun.result, fun.exit); 
		 }
		 if(se.e instanceof Eaccess_local) {
			 System.out.println("return eacces");
			 Register r = fun_locals.get(((Eaccess_local)se.e).i); 
			 if(r == null) {
				 throw new Error("No such locals value"); 
			 }
			 res = new Rload(r, 0 , fun.result, fun.exit); 
		 }
		 if (se.e instanceof Ebinop) {
			 Ebinop tmp = (Ebinop) se.e; 
			 
			 //operation ??
			 res = new Rmbinop(Mbinop.Madd, new Register(), new Register(), new Label()); 
		 }
		 System.out.println("Sreturn fin"); 
		 return ; 
		 
	 }
	*/
	
	 public void visit(Sreturn se) {
		 //Rmbinop tmp; 
		 System.out.println("Sreturn Deb"); 
		 if(se.e instanceof Econst) {
			 System.out.println("return econst");
			 res = new Rconst(((Econst)se.e).i, fun.result, fun.exit); 
		 }
		 if(se.e instanceof Eaccess_local) {
			 System.out.println("return eacces");
			 Register r = fun_locals.get(((Eaccess_local)se.e).i); 
			 if(r == null) {
				 throw new Error("No such locals value"); 
			 }
			 res = new Rload(r, 0 , fun.result, fun.exit); 
		 }
		 if (se.e instanceof Ebinop) {
			 Ebinop tmp = (Ebinop) se.e; 
			
			 //res = new Rmbinop(Mbinop.Madd, new Register(), new Register(), new Label()); 
		 }
		 System.out.println("Sreturn fin"); 
		 return ; 
		 
	 }
	 
	 public void visit(Ebinop n) {
		 System.out.println("Ebinop Deb"); 
		 
		 System.out.println("Ebinop Fin"); 
	 }
	/*
	 public void visit(Eaccess_local a) {
		 Register r = fun_locals.get(a.i); 
		 if(r == null) {
			 throw new Error("No such locals value"); 
		 }
		 
	 }
	 */
	 /*
	 
	 public void visit(Sreturn se) {
		 //Rmbinop tmp; 
		 System.out.println("Sreturn Deb"); 
		 stack_block.push(IDENT_SRETURN); 
		 if(se.e instanceof Econst) {
			 System.out.println("return econst");
			 res = new Rconst(((Econst)se.e).i, fun.result, fun.exit); 
		 }
		 if(se.e instanceof Eaccess_local) {
			 System.out.println("return eacces");
			 res = new Rload(fun_locals.get(((Eaccess_local)se.e).i), 0 , fun.result, fun.exit); 
		 }
		 if (se.e instanceof Ebinop) {
			 Ebinop tmp = (Ebinop) se.e; 
			 
			 //operation ??
			 res = new Rmbinop(Mbinop.Madd, new Register(), new Register(), new Label()); 
		 }
		 System.out.println("Sreturn fin"); 
		 return ; 
		 
	 }
	 */
}
