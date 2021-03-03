package mini_c;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

class ToRTL extends EmptyVisitor{
	
	private RTLfile rtlfile;
	
	
	RTL res; // RTL result of the last visit 
	RTLgraph g; 
	HashMap<String, Register> fun_locals; 
	Stack <RTLgraph> stack_graph; 
	RTLfun fun; //the current function
	
	private int ident_expr = 0; 
	private int ident_block = 0; 
	final private static int IDENT_DECL= 0, IDENT_PBLOC = 1, IDENT_SRETURN = 2, IDENT_SIF = 3; 
	final private static int IDENT_ECONST=0, IDENT_EACCES_LOCAL=1, IDENT_EACCES_FIELS=2, IDENT_EASSIGN_LOCAL = 3, 
			IDENT_EASSIGN_FIELD = 4, IDENT_EUNOP = 5, IDENT_EBINOP = 6, IDENT_ECALL = 7, IDENT_ESIZEOF = 8; 
	
	public Stack<Expr> stack_addr_expr = new Stack<>(); 
	public Stack<Integer> stack_expr = new Stack<>(); 
	public Stack<Sblock> stack_addr_block = new Stack<>(); 
	public Stack<Integer> stack_block = new Stack<>(); 
	
	
	public Stack<Register> stack_reg;  
	public Stack<Label> stack_lab;
	public Label exit_label; 
	public Label entry_label; 
	public Register exit_reg; 
	public Label last_result; 
	RTLgraph current_graph; 
	boolean first_event; 
	public int n_block =0; 
	Label next_lab; 
	Register next_reg; 
	boolean deja_first = false; 
	
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
		 System.out.println("Decl_fun Deb" + d.fun_name); 
		 
		 stack_reg = new Stack<>();  
		 stack_lab = new Stack<>();  
		 RTLfun tmp = new RTLfun(d.fun_name); 
		 fun = tmp; 
		 first_event = false; 
		 tmp.exit = new Label(); 
		 //tmp.entry = last_result; 
		 
		 
		 Register r; 
		 fun_locals = new HashMap<>();
		 for(Decl_var v: d.fun_formals) {
			 r = new Register(); 
			 tmp.formals.add(r); 
			 fun_locals.put(v.name, r); 
		 }
		 tmp.result = new Register(); 
		 tmp.body = new RTLgraph(); 
		 current_graph = tmp.body; 
		 
		 
		 this.visit(d.fun_body); 
		 tmp.entry = last_result; 
		 //System.out.println("grap "+ tmp.body.graph.size()); 
		 tmp.body = current_graph; 
		 rtlfile.funs.add(tmp); 
		 System.out.println("Decl_fun Fin"); 
	 }
	
	 
	 public void visit(Stmt stmt) {
		 System.out.println("Stmt Deb"); 
		 //ALL
		 if(stmt instanceof Sskip) {
			 this.visit((Sskip)stmt); 
		 } 
		 if(stmt instanceof Sexpr) {
			 this.visit((Sexpr) stmt);
		 } 
		 if(stmt instanceof Sif) {
			 this.visit((Sif)stmt);
		 } 
		 if(stmt instanceof Swhile); 
		 if(stmt instanceof Sblock) {
			 this.visit((Sblock)stmt);
		 } 
		 if(stmt instanceof Sreturn) {
			 this.visit((Sreturn)stmt);
		} 
		 System.out.println("Stmt Fin"); 
	 }
	 
	 
	 /*
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
	 
	 */
	 
	 public void visit(Sblock sb) {
		 //Rmbinop tmp; 
		 
		 System.out.println("Sblock Deb"); 
		 n_block++; 
		 Register r; 
		 for(Decl_var v: sb.dl) {
			 r = new Register(); 
			 fun_locals.put(v.name, r); 
			 fun.locals.add(r); 
		 }
		 
		 int ss = sb.sl.size(); 
		 ListIterator<Stmt> iterator =sb.sl.listIterator(ss);
		 
		 //for(Stmt stmt: sb.sl) {
			 /*
			 if (stmt instanceof Sreturn) {
				 System.out.println(stmt instanceof Sreturn );
				 this.visit((Sreturn)stmt); 
			 }
			 */
			// this.visit(stmt); 
		// }
	 
	int i = 0; 
	 while(iterator.hasPrevious()){
		 	if(i++ == ss-1)
		 		first_event = true; 
		   this.visit(iterator.previous());
		} 
	 n_block--;
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
/*
		 if(se.e instanceof Econst) {
			 System.out.println("return econst");
			 res = new Rconst(((Econst)se.e).i, fun.result, fun.exit); 
			 current_graph.graph.put(stack_lab.pop(), res); 
			 System.out.println("Sreturn fin"); 
			 return; 
		 }*/
		 
		 
		 
		 //stack_reg.push(fun.result);
		 //stack_lab.push(fun.exit); 
		 
		 //exit_label = fun.exit; 
		 
		 //exit_reg = fun.result; 
		 //stack_reg.pop(); 
		 //stack_reg.push(fun.result); 
		 //stack_block.add(IDENT_SRETURN); 
		 stack_lab.push(fun.exit); 
		 stack_reg.push(fun.result); 
		 this.visit(se.e);
		 
		 
		 System.out.println("Sreturn fin"); 
		 return ; 
		 
	 }
	 public void visit(Expr e) {
		 System.out.println("Expr Deb"); 
		 
		 
		 
		 if (e instanceof Econst) {
			 this.visit((Econst)e);
		 }
		 else if(e instanceof Ebinop) {
			 this.visit((Ebinop)e); 
		 }
		 else if(e instanceof Eaccess_local) {
			 this.visit((Eaccess_local)e);
		 }
		 else if(e instanceof Eunop) {
			 this.visit((Eunop)e);
		 }
		 else if(e instanceof Eassign_local) {
			 this.visit((Eassign_local)e);
		 }
		 else if(e instanceof Ecall) {
			 this.visit((Ecall)e);
		 }
		 
		 //stack_reg.push(new Register());
		 stack_lab.push(last_result); 
		 
		 System.out.println("Expr fin"); 
	 }
	 
	
	 
	 public void visit(Econst n) {
		 
		
		 System.out.println("Econst Deb"); 
		 
		 Register rd = stack_reg.pop(); 
		 Label Ld = stack_lab.pop();
		
		 System.out.println("rd : "+ rd); 
		 System.out.println("Ld : "+ Ld); 
		 Label L1 = new Label(); 
		 System.out.println("res : "+ L1); 
		 if (n_block == 1 && first_event ) {
			 L1 = fun.entry; 
			 first_event = false; 
		 }
		 System.out.println(L1); 
		 System.out.println(new Rconst(n.i,rd , Ld)); 
		 current_graph.graph.put(L1 ,new Rconst(n.i,rd , Ld)); 
		 
		 
		 last_result = L1; 
		
		 System.out.println("Econst Fin"); 
	 }
	 
	 
	 public void visit(Ebinop n) {
		 System.out.println("Ebinop Deb");
		 
		 
		 
		 Mbinop m = Mbinop.Madd ;
		 if(n.b == Binop.Badd)
			 m = Mbinop.Madd ;
		 
		 if(n.b == Binop.Band)
			 
			 
		 if(n.b == Binop.Bdiv)
			 m = Mbinop.Mdiv ;
		 if(n.b == Binop.Beq) 
			 m = Mbinop.Msete; 
		 if(n.b == Binop.Bge)
			 m = Mbinop.Msetge;
		 if(n.b == Binop.Bgt)
			 m = Mbinop.Msetg; 
		 if(n.b == Binop.Ble)
			 m = Mbinop.Msetle;
		 if(n.b == Binop.Blt)
			 m = Mbinop.Msetl; 
		 if(n.b == Binop.Bmul)
			 m = Mbinop.Mmul;
		 if(n.b == Binop.Bneq)
			 m = Mbinop.Msetne; 
		 
		 
		 if(n.b == Binop.Bor)
			 ; 
		 
		 
		 if(n.b == Binop.Bsub)
			 m = Mbinop.Msub ;
		 
		 
		 Register rd = stack_reg.pop(); 
		 Label Ld = stack_lab.pop();
		 
		 
		 //Register reg = new Register(); 
		 boolean oui =false;
		 if(first_event) {
			 first_event = false; 
			 oui = true; 
		 }
		 
		 Label L1, L2; 
		  
		 Label L3 = new Label(); 
		 Register r2 = new Register(); 
		 
		 stack_lab.push(L3);
		 stack_reg.push(r2);
		 
		 
		 if(n.e1 instanceof Econst) {
			 Munop um; 
			 if(oui)
				 first_event = true; 
			 stack_reg.push(rd);
			 this.visit(n.e2);
			 L2 = last_result;
			 if(n.b == Binop.Badd) {
				 um = new Maddi(((Econst)n.e1).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld)); 
				 return; 
			 }
				 
			 if(n.b == Binop.Bneq){
				 um = new Msetnei(((Econst)n.e1).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld)); 
				 return; 
			 }
				  
			 if(n.b == Binop.Beq){
				 um = new Msetei(((Econst)n.e1).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld));
				 return; 
			 }
			 
			 
		 }
		 
		 if(n.e2 instanceof Econst) {
			 
			 Munop um; 
			 if(oui)
				 first_event = true; 
			 stack_reg.push(rd);
			 this.visit(n.e1);
			 L2 = last_result;
			 if(n.b == Binop.Badd) {
				 um = new Maddi(((Econst)n.e2).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld)); 
				 return; 
			 }
				 
			 if(n.b == Binop.Bneq)
			 {
				 um = new Msetnei(((Econst)n.e2).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld)); 
				 return; 
			 }
				 
			 if(n.b == Binop.Beq)
			 {
				 um = new Msetei(((Econst)n.e2).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld)); 
				 return; 
			 }
				
			 if(n.b == Binop.Bsub)
			 {
				 um = new Maddi(-((Econst)n.e2).i); 
				 current_graph.graph.put(L3, new Rmunop(um, rd, Ld)); 
				 return; 
			 }
			
		 }
		 
		 
		 
			 this.visit(n.e2);
			 L2 = last_result;
			 stack_lab.push(L2);
			 stack_reg.push(rd);
			 if(oui)
				 first_event = true; 
			 this.visit(n.e1);
			 L1= last_result;
		
		 current_graph.graph.put(L3, new Rmbinop(m, r2, rd, Ld)); 
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
	 
	 
	public void visit(Unop n) {
		 
	}

		public void visit(Binop n) {
		}

		public void visit(String n) {
		}

		public void visit(Tint n) {
		}

		public void visit(Tstructp n) {
		}

		public void visit(Tvoidstar n) {
		}

		public void visit(Ttypenull n) {
		}

		public void visit(Structure n) {
		}

		public void visit(Field n) {
		}

		public void visit(Decl_var n) {
		}

	

		public void visit(Eaccess_local n) {
			
			System.out.println("Eaccess_local Deb"); 
			 Register r = fun_locals.get(n.i); 
			 if(r == null) {
				 throw new Error("No such locals value"); 
			 }
			 
			 
			 Register rd = stack_reg.pop(); 
			 Label Ld = stack_lab.pop();
			
			 System.out.println("rd : "+ rd); 
			 System.out.println("Ld : "+ Ld); 
			 Label L1 = new Label(); 
			 
			 if (n_block == 1 && first_event ) {
				 deja_first = true;
				 L1 = fun.entry; 
				 first_event = false; 
			 }
			

			 current_graph.graph.put(L1 ,new Rmbinop(Mbinop.Mmov,r, rd , Ld)); 
			 
			 stack_reg.push(r); 
			 stack_lab.push(Ld);
			 last_result = L1; 
			 System.out.println("Eaccess_local Fin"); 
		}

		public void visit(Eaccess_field n) {
		}

		public void visit(Eassign_local n) {
			
			System.out.println("Eassign_local Deb"); 
			System.out.println(n); 
			 Register r = fun_locals.get(n.i); 
			 if(r == null) {
				 throw new Error("No such locals value"); 
			 }
			 System.out.println("Ici"); 
			
			 
			 Register rd = stack_reg.pop(); 
			 System.out.println("Ici");
			 Label Ld = stack_lab.pop();
			 System.out.println("là"); 
			 Label  L2, L3; 
			 L3 = new Label(); 
			 L2 = new Label(); 
			 Register r2 = new Register(); 
			 Register r1 = new Register(); 
			 //current_graph.graph.put(L3 ,new Rstore(r2,rd,0, Ld));
			 //current_graph.graph.put(L2 ,new Rload(r1,0, r, Ld));
			 
			 
			 //System.out.println(L3); 
			 //System.out.println(new Rstore(r2,rd,0, Ld)); 
			 System.out.println("L2 = " + L2); 
			 
			 System.out.println(new Rmbinop(Mbinop.Mmov,r1, r , Ld)); 
			 stack_reg.push(r); 
			 stack_lab.push(Ld);
			 
			 /*
			 if(n.e instanceof Econst) {
				 return; 
			 }
			 */
			 this.visit(n.e);
			 
			
			 System.out.println("(Eassign_local Fin"); 
			 
		}

		public void visit(Eassign_field n) {
		}

		public void visit(Eunop n) {
			System.out.println("Eunop Deb"); 
			if(n.u == Unop.Uneg) {
				Ebinop tmp = new Ebinop(Binop.Bsub, new Econst(0), n.e ); // A revoir
				this.visit(tmp); 
			}
			else if(n.u == Unop.Unot)
				;
			else
				throw new Error("ToRTL Eunop"); 
			System.out.println("Eunop Fin"); 
		}

	

		public void visit(Ecall n) {
			
			System.out.println("Ecall Deb"); 
			List<Register> rl = new LinkedList<>();
			//Register r; 
			//Label l = new Label(); 
			
			//parcours sens inverse: 
			int ss = n.el.size(); 
			//ListIterator<Expr> iterator =n.el.listIterator(ss);
			 
			 

		 
			int i = 0; 
			boolean oui = false;
			/*
			
			while(iterator.hasPrevious()){
				if(i++ == ss-1 && oui)
			 		first_event = true; 
				r = new Register(); 
				stack_lab.push(l); 
				stack_reg.push(r); 
				System.out.println("ici reg: "+r);
			   this.visit(iterator.previous());
			   rl.add(r); 
			   l = last_result; 
			} 
			*/
			
			Register rd = stack_reg.pop(); 
			Label Ld = stack_lab.pop(); 
			
			
			Label L1, L2; 
			  
			
			if(ss == 0) {
				if(first_event && n_block==1) {
					L1 = fun.entry; 
				}
				else {
					L1 = new Label(); 
				}
				current_graph.graph.put(L1, new Rcall(rd, n.i, rl, Ld)); 
				last_result = L1; 
				return; 
			}
			
			Label L3 = new Label(); 
			Register r2 = new Register(); 
			stack_lab.push(L3);
			stack_reg.push(r2);
			 
			if(first_event && n_block==1) {
				first_event = false; 
				oui = true; 
			}
			rl.add(r2); 
			for(Expr e: n.el) {
				if(i == ss - 1 && oui)
					first_event = true; 
				/*
				r = new Register(); 
				if(last_result !=null)
					l = last_result;
				stack_lab.push(last_result); 
				stack_reg.push(r); 
				this.visit(e);
				rl.add(r); 
				*/
				this.visit(e); 
				L2 = last_result; 
				stack_lab.push(L2);
				if(i++ == ss - 1)
					stack_reg.push(rd);
				else {
					Register rr = new Register(); 
					stack_reg.push(rr); 
					rl.add(rr); 
				}
				//l = last_result; 
				
			}
			
			Label L = new Label(); 
			Register ress = new Register(); 
			//stack_lab.push(L); 
			//stack_reg.push(ress); 
			current_graph.graph.put(L3, new Rcall(rd, n.i, rl, Ld)); 
			System.out.println("Ecall Fin"); 
		}

		
		public void visit(Esizeof n) {
		}

		public void visit(Sskip n) {
			
			System.out.println("Deb Sskip"); 
			
			//last_result =  stack_lab.pop() ;
			//stack_reg.pop() ;
			
			System.out.println(last_result ); 
			System.out.println("first " + first_event ); 
			System.out.println("Fin Sskip"); 
		}

		public void visit(Sexpr n) {
			System.out.println("Sexpr Deb"); 
			
			this.visit(n.e);
			
			System.out.println("Sexpr Fin"); 
		}

		public void visit(Sif n) {
			System.out.println("Deb Sif"); 
			
			
			//if(n.e instanceof Ebinop && ((Ebinop)n.e).b == Binop.Bor);
			//if(n.e instanceof Ebinop && ((Ebinop)n.e).b == Binop.Ble); 
			boolean oui = false; 
			if(first_event) {
				oui = true; 
				first_event = false; 
			}
			Label Lt, Lf; 
			Stack<Label> stack_lab_tmp = (Stack)stack_lab.clone();
			Stack<Register> stack_reg_tmp = (Stack)stack_reg.clone();
			this.visit(n.s2);
			Lf = last_result; 
			stack_lab = stack_lab_tmp;
			stack_reg = stack_reg_tmp;
			//stack_reg.pop(); 
			//stack_lab.pop(); 
			
			this.visit(n.s1);
			Lt = last_result; 
			
			//throw new Error("Lt: "+Lt + " Lf : "+ Lf);
			
			Register r = new Register(); 
			Label L2 = new Label(); 
			stack_reg.push(r); 
			stack_lab.push(L2); 
			
			if(oui) {
				first_event =true; 
			}
			
			if(n.e instanceof Ebinop) {
				Ebinop tmp = (Ebinop)n.e; 
				if(tmp.e2 instanceof Econst) {
					
					if(tmp.b == Binop.Ble) {
						Register rrd = stack_reg.pop(); 
						Label LLd = stack_lab.pop();
						
						 Label LL1, LL2; 
						  
						 Label LL3 = new Label(); 
						 Register rr2 = new Register(); 
						 
						 stack_lab.push(LL3);
						 stack_reg.push(rr2);
						 Mubranch um; 
						 if(oui)
							 first_event = true; 
						 stack_reg.push(rrd);
						 this.visit(tmp.e1);
						 L2 = last_result;
						 um = new Mjlei(((Econst)tmp.e2).i); 
						 //current_graph.graph.put(LL3, new Rmunop(um, rrd, LLd));
						 current_graph.graph.put(LL3, new Rmubranch(um, rrd, Lt, Lf));
						 return; 
						 
					}
						
					if(tmp.b == Binop.Bgt) {
						Register rrd = stack_reg.pop(); 
					Label LLd = stack_lab.pop();
					
					 Label LL1, LL2; 
					  
					 Label LL3 = new Label(); 
					 Register rr2 = new Register(); 
					 
					 stack_lab.push(LL3);
					 stack_reg.push(rr2);
					 Mubranch um; 
					 if(oui)
						 first_event = true; 
					 stack_reg.push(rrd);
					 this.visit(tmp.e1);
					 L2 = last_result;
					 um = new Mjgi(((Econst)tmp.e2).i); 
					 //current_graph.graph.put(LL3, new Rmunop(um, rrd, LLd));
					 current_graph.graph.put(LL3, new Rmubranch(um, rrd, Lt, Lf));
					 return; 
					}
				}
				 
			}
			
			
			this.visit(n.e); 
			Mjz m = new Mjz(); 
			current_graph.graph.put(L2, new Rmubranch(m, r, Lt, Lf));
			System.out.println(L2); 
			System.out.println(new Rmubranch(m, r, Lt, Lf));
			System.out.println("Fin Sif");
			//L1 = last_result; 
		}

		public void visit(Swhile n) {
		}

		
		
	 
}
