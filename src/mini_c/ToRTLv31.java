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
	
	public Stack<Register> stack_reg;  
	public Stack<Label> stack_lab;
	public Label exit_label; 
	public Label entry_label; 
	public Register exit_reg; 
	public Label last_result;
	public Label last_output;
	RTLgraph current_graph; 
	Stack<Register> last_reg = new Stack<>();
	
	Stack<Register> reg_retour = new Stack<>();
	Stack<Label> reg_rip = new Stack<>();
	
	public int position;
	
	
	
	
	public Stack<Integer> stack_const = new Stack<Integer>();
	public void ab() {
		System.out.println("NOOOOOOOOOOOOOOOOOOOPE"+ stack_reg.peek());
	}
	public boolean debug = false;

	Label next_lab; 
	Register next_reg; 
	boolean deja_first = false; 
	Stack<Eaccess_field> to_visit = new Stack<>(); 
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
		 reg_retour.push(null);
		 reg_rip.push(null);
		 if (debug) System.out.println("File Deb"); 
		 rtlfile = new RTLfile(); 
		 for (Decl_fun d: f.funs) {
			 this.visit(d);
		 }
		 if (debug) System.out.println("File fin"); 	
	 }
	 
	 public void visit(Decl_fun d) {
		 if (debug) System.out.println("Decl_fun Deb" + d.fun_name); 
		 
		 stack_reg = new Stack<>();  
		 stack_lab = new Stack<>();  
		 RTLfun tmp = new RTLfun(d.fun_name); 
		 fun = tmp; 
		 
		 tmp.exit = new Label(); 
		
		 
		 
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
		
		 tmp.body = current_graph; 
		 rtlfile.funs.add(tmp); 
		 if (debug) System.out.println("Decl_fun Fin"); 
	 }
	
	 
	 public void visit(Stmt stmt) {
		 if (debug) System.out.println("Stmt Deb"); 
		 
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
		 if(stmt instanceof Swhile) {
			 this.visit((Swhile)stmt);
		 }
		 if (debug) System.out.println("Stmt Fin"); 
	 }
	 
	 
	 	 
	 public void visit(Sblock sb) {
		 //Rmbinop tmp; 
		 
		 if (debug) System.out.println("Sblock Deb"); 
		
		 Register r; 
		 for(Decl_var v: sb.dl) {
			 r = new Register(); 
			 fun_locals.put(v.name, r); 
			 fun.locals.add(r); 
		 }
		 
		 int ss = sb.sl.size(); 
		 ListIterator<Stmt> iterator =sb.sl.listIterator(ss);
		 
	
	 while(iterator.hasPrevious()){
		 	
		   this.visit(iterator.previous());
		} 
	 
	 
	 for(Decl_var v: sb.dl) {
		
		 
		 r = fun_locals.get(v.name); 
		 //fun.locals.add(r); 
		 
		 /*
		 stack_reg.push(r); 
		 
		 
		 if(v.t instanceof Tint) {
			 this.visit((Tint) v.t);
		 }
		 else {
			 this.visit((Tstructp) v.t);
		 }
		 stack_lab.push(last_result);
		 */
		
	 }
	 
		 if (debug) System.out.println("Sblock Fin"); 
		 return ; 
		 
	 }
	 
	
	 public void visit(Sreturn se) {
		 //Rmbinop tmp; 
		 if (debug) System.out.println("Sreturn Deb"); 
		 stack_lab.push(fun.exit); 
		 stack_reg.push(fun.result); 
		 this.visit(se.e);
		 
		 
		 if (debug) System.out.println("Sreturn fin"); 
		 return ; 
		 
	 }
	 public void visit(Expr e) {
		 if (debug) System.out.println("Expr Deb"); 
		 
		 
		 
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
		 else if(e instanceof Eaccess_field) {
			 this.visit((Eaccess_field)e);
		 }
		 else if(e instanceof Eassign_field) {
			 this.visit((Eassign_field)e);
		 }
		 else if(e instanceof Esizeof) {
			 this.visit((Esizeof)e);
		 }
		 stack_reg.push(new Register());
		 stack_lab.push(last_result); 
		
		 if (debug) System.out.println("Expr fin"); 
	 }
	 
	
	 
	 public void visit(Econst n) {
		 
			
		 if(debug) System.out.println("-----------------Econst Deb"); 
		 
		 Register rd = stack_reg.pop(); 
		 Label Ld = stack_lab.pop();
		
		 Label L1 = new Label(); 
		 
		 current_graph.graph.put(L1 ,new Rconst(n.i,rd , Ld)); 
		 
		 
		 last_result = L1; 
		
		 if(debug) System.out.println("Econst Fin"); 
	 }
	 
	 
	 public Expr reduction(Expr nexpr) {
			if(debug) System.out.println("Deb red");
			if(nexpr instanceof Ebinop) {
				Ebinop n = (Ebinop)nexpr;
				if(((Ebinop)nexpr).e1 instanceof Econst && ((Ebinop)nexpr).e2 instanceof Econst) {
					
					int result=0;
					if(n.b != Binop.Band && n.b != Binop.Bor) {	 
						if(n.b == Binop.Badd) result = ((Econst)n.e1).i+((Econst)n.e2).i;
						if(n.b == Binop.Bsub) result = ((Econst)n.e1).i-((Econst)n.e2).i;
						if(n.b == Binop.Bmul) result = ((Econst)n.e1).i*((Econst)n.e2).i;
						if(n.b == Binop.Bdiv) {
							 if (((Econst)n.e2).i==0)
								 throw new Error("division by zero at location ");
							 result = ((Econst)n.e1).i/((Econst)n.e2).i;
						}
						if(n.b == Binop.Beq) result = ((Econst)n.e1).i==((Econst)n.e2).i ? 1 : 0;
						if(n.b == Binop.Bneq) result = ((Econst)n.e1).i!=((Econst)n.e2).i ? 1 : 0;
						if(n.b == Binop.Bgt) result = ((Econst)n.e1).i>((Econst)n.e2).i ? 1 : 0;
						if(n.b == Binop.Bge) result = ((Econst)n.e1).i>=((Econst)n.e2).i ? 1 : 0;
						if(n.b == Binop.Blt) result = ((Econst)n.e1).i<((Econst)n.e2).i ? 1 : 0;
						if(n.b == Binop.Ble) result = ((Econst)n.e1).i<=((Econst)n.e2).i ? 1 : 0;
					}
					else if(n.b == Binop.Band) {
						if(((Econst)n.e1).i==0) result = 0;
						else {
							if(((Econst)n.e2).i==0) result = 0;
							else result = 1;
						}
					}
					else if(n.b == Binop.Bor) {
						if(((Econst)n.e1).i!=0) result = 1;
						else {
							if(((Econst)n.e2).i!=0) result = 1;
							else result = 0;
						}
					}
					if(debug) System.out.println("Fin1 red");
					return new Econst(result);
				}
				else if(n.e1 instanceof Econst && n.b == Binop.Bdiv && ((Econst)n.e1).i==0) {
					if(reduction(n.e2) instanceof Econst && ((Econst)reduction(n.e2)).i==0)
						throw new Error("division by 0");
				}
				else if(n.b == Binop.Bsub && n.e1.toString().equals(n.e2.toString()))
					return new Econst(0);
				else if(n.e2 instanceof Econst && n.b == Binop.Bdiv && ((Econst)n.e2).i==0)
					throw new Error("division by 0");
				else if(n.e2 instanceof Eunop) {
					
					if(n.b == Binop.Bsub) {
						if(((Eunop)n.e2).u == Unop.Uneg){
							return reduction(new Ebinop(Binop.Badd, n.e1, ((Eunop)n.e2).e));
						}
						else if(((Eunop)n.e2).u == Unop.Unot){
							Expr e = reduction(((Eunop)n.e2).e);
							return reduction(new Ebinop(Binop.Bsub, n.e1, e));
						}
					}
					else if(n.b == Binop.Badd) {
						if(((Eunop)n.e2).u == Unop.Uneg){
							return reduction(new Ebinop(Binop.Bsub, n.e1, ((Eunop)n.e2).e));
						}
						else if(((Eunop)n.e2).u == Unop.Unot){
							
							Expr e = reduction(((Eunop)n.e2));
							if(e.toString().equals(((Eunop)n.e2).toString()))
								return new Ebinop(Binop.Badd, n.e1, e);
							return reduction(new Ebinop(Binop.Badd, n.e1, e));
						}
					}
				}
				else {
					Expr e = reduction(n.e1);
					Expr f = reduction(n.e2);
					if(debug) System.out.println("Fin2 red");
					if(e.toString().equals((n.e1).toString()) && f.toString().equals((n.e2).toString()))
						return new Ebinop(n.b, e, f);	
					return reduction(new Ebinop(n.b, e, f));
				}
			}
			else if(nexpr instanceof Eunop) {
				
				Eunop n = (Eunop)nexpr;
				if(debug) System.out.println("Fin3 red");
				if(n.e instanceof Econst) {
					if(n.u == Unop.Uneg) return new Econst(-((Econst)n.e).i);
					else {
						
						if(((Econst)n.e).i==0) return new Econst(1);
						else return new Econst(0);
					}
				}
				else {
					Expr e = reduction(n.e);
					if(e instanceof Econst)
						return reduction(new Eunop(n.u, e));
					else {
						return new Eunop(n.u, e);
					}
						
				}
			}
			else
				if(debug) System.out.println("Fin4 red");
				return nexpr;
		}
		 
		public void visit(Ebinop nbinop) {
			if (debug) System.out.println("Ebinop Deb");
			Expr temp = reduction(nbinop);
			if (debug) System.out.println(temp);
			if (debug) System.out.println("\n\nreduction = " +temp);
			
			if(temp instanceof Econst) {
				Register rd = stack_reg.pop();
				Label Ld = stack_lab.pop();
				Label L1 = new Label(); 
				current_graph.graph.put(L1 ,new Rconst(((Econst)temp).i, rd, Ld)); 
				last_result = L1;
				return;
			}
			else if(temp instanceof Ebinop) {
				
				Ebinop n = (Ebinop)temp;
				
				Mbinop m = null ;
				if(n.b == Binop.Band);
				if(n.b == Binop.Bor);
				if(n.b == Binop.Badd) m = Mbinop.Madd ;
				if(n.b == Binop.Bsub) m = Mbinop.Msub ;
				if(n.b == Binop.Bmul) m = Mbinop.Mmul;
				if(n.b == Binop.Bdiv) m = Mbinop.Mdiv ;
				if(n.b == Binop.Beq) m = Mbinop.Msete;
				if(n.b == Binop.Bneq) m = Mbinop.Msetne;
				if(n.b == Binop.Bge) m = Mbinop.Msetge;
				if(n.b == Binop.Bgt) m = Mbinop.Msetg; 
				if(n.b == Binop.Ble) m = Mbinop.Msetle;
				if(n.b == Binop.Blt) m = Mbinop.Msetl; 

				

				
				if(n.e1 instanceof Econst && n.e2 instanceof Eaccess_local) {
					
					Register rd = stack_reg.pop();
					Label Ld = stack_lab.pop();
					Label L3 = new Label(); 
					stack_lab.push(L3);
					Register r2 = fun_locals.get(((Eaccess_local)n.e2).i);
					
					Munop um=null;
					if(n.b!=Binop.Band && n.b!=Binop.Bor) {
						if(n.b == Binop.Badd) um = new Maddi(((Econst)n.e1).i);
						else if(n.b == Binop.Bsub) um = new Maddi(-((Econst)n.e1).i);
						else if(n.b == Binop.Beq) um = new Msetei(((Econst)n.e1).i);
						else if(n.b == Binop.Bneq) um = new Msetnei(((Econst)n.e1).i);
						else {
							stack_reg.push(r2);
							this.visit(n.e1);
							stack_reg.push(rd);
							this.visit(n.e2);
							Label L6 = new Label();
							Label L7 = new Label();
							Label L8 = new Label();
							Register r3 = new Register();
							Register r4 = new Register();
							current_graph.graph.put(L3, new Rmbinop(Mbinop.Mmov, r2, r3, L6));
							current_graph.graph.put(L6, new Rmbinop(Mbinop.Mmov, rd, r4, L7));
							current_graph.graph.put(L7, new Rmbinop(m, r4, r3, L8));
							current_graph.graph.put(L8, new Rmbinop(Mbinop.Mmov, r3, rd, Ld));
							reg_retour.push(rd);
							return;
						}
						
						stack_reg.push(rd);
						this.visit(n.e2);
						current_graph.graph.put(L3, new Rmunop(um, rd, Ld));
						reg_retour.push(rd);
						return;
					}
					else {
						if(n.b == Binop.Band) {
							Label L5 = new Label();
							if(((Econst)n.e1).i==0) {
								current_graph.graph.put(L5 ,new Rconst(0, rd , Ld));
								last_result = L5;
								reg_retour.push(rd);
								return;
							}
							Label Lt, Lf; 
							
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(0));
							Lf = last_result;
							
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(1));
							Lt = last_result;
							RTLc(n.e2, Lt, Lf);
							return;
						}
						else if(n.b == Binop.Bor) {
							Label L5 = new Label();
							if(((Econst)n.e1).i!=0) {
								current_graph.graph.put(L5 ,new Rconst(1, rd , Ld));
								reg_retour.push(rd);
								last_result = L5;
								return;
							}
							Label Lt, Lf; 
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(0));
							Lf = last_result;
							
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(1));
							Lt = last_result;
							RTLc(n.e2, Lt, Lf);
							return;
						}
						return;
					}
				}
				else if(n.e2 instanceof Econst  && n.e1 instanceof Eaccess_local) {
					Register rd = stack_reg.pop();
					Label Ld = stack_lab.pop();
					Label L3 = new Label(); 
					stack_lab.push(L3);
					Register r2 = new Register();
					
					Munop um=null;
					if(n.b!=Binop.Band && n.b!=Binop.Bor) {
						if(n.b == Binop.Badd) um = new Maddi(((Econst)n.e2).i);
						else if(n.b == Binop.Bsub) um = new Maddi(-((Econst)n.e2).i);
						else if(n.b == Binop.Beq) um = new Msetei(((Econst)n.e2).i);
						else if(n.b == Binop.Bneq) um = new Msetnei(((Econst)n.e2).i);
						else  {	
							r2 = fun_locals.get(((Eaccess_local)n.e1).i);
							stack_reg.push(rd);
							this.visit(n.e2);
							
							Label L6 = new Label();
							Label L7 = new Label();
							Label L8 = new Label();
							Register r3 = new Register();
							Register r4 = new Register();
							current_graph.graph.put(L3, new Rmbinop(Mbinop.Mmov, r2, r3, L6));
							current_graph.graph.put(L6, new Rmbinop(Mbinop.Mmov, rd, r4, L7));
							
							current_graph.graph.put(L7, new Rmbinop(m, r4, r3, L8));
							current_graph.graph.put(L8, new Rmbinop(Mbinop.Mmov, r3, rd, Ld));
							reg_retour.push(rd);
							return;
						}
						
						stack_reg.push(rd);
						this.visit(n.e1);
						current_graph.graph.put(L3, new Rmunop(um, rd, Ld));
						reg_retour.push(rd);
						return;
					}
					else {
						if(n.b == Binop.Band) {
							Label Lt, Lf; 
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(0));
							Lf = last_result;
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(1));
							Lt = last_result;
							Label L5 = new Label();
							RTLc(n.e1, L5, Lf);
							if(((Econst)n.e2).i==0)
								current_graph.graph.put(L5 ,new Rconst(0, rd , Ld));
							else
								current_graph.graph.put(L5 ,new Rconst(1, rd , Ld));
							reg_retour.push(rd);
							return;
						}
						else if(n.b == Binop.Bor) {
							Label Lt, Lf; 
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(0));
							Lf = last_result;
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(1));
							Lt = last_result;
							Label L5 = new Label();
							RTLc(n.e1, Lt, L5);
							if(((Econst)n.e2).i!=0)
								current_graph.graph.put(L5 ,new Rconst(1, rd , Ld));
							else
								current_graph.graph.put(L5 ,new Rconst(0, rd , Ld));
							reg_retour.push(rd);
							return;
						}
						return;
					}
				}
				else {
					if(n.b!=Binop.Band && n.b!=Binop.Bor) {
						Register rd = stack_reg.pop();
						Label Ld = stack_lab.pop();
						Label L3 = new Label(); 
						stack_lab.push(L3);
						Register r2 = new Register();
						
						stack_reg.push(r2);
						this.visit(n.e1);
						
						stack_reg.push(rd);
						this.visit(n.e2);
						
						Label L6 = new Label();
						Label L7 = new Label();
						Label L8 = new Label();
						Register r3 = new Register();
						Register r4 = new Register();
						current_graph.graph.put(L3, new Rmbinop(Mbinop.Mmov, r2, r3, L6));
						current_graph.graph.put(L6, new Rmbinop(Mbinop.Mmov, rd, r4, L7));
						
						current_graph.graph.put(L7, new Rmbinop(m, r4, r3, L8));
						current_graph.graph.put(L8, new Rmbinop(Mbinop.Mmov, r3, rd, Ld));
						reg_retour.push(rd);
						return;
					}
					else {
						if(n.b == Binop.Band) {
							Label Lt, Lf; 
							Register rd = stack_reg.pop();
							Label Ld = stack_lab.pop();
							
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(0));
							Lf = last_result;
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(1));
							Lt = last_result;
							
							RTLc(n.e2, Lt, Lf);

							RTLc(n.e1, last_result, Lf);
							
							return;
						}
						else if(n.b == Binop.Bor) {
							Label Lt, Lf; 
							Register rd = stack_reg.pop();
							Label Ld = stack_lab.pop();
							
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(0));
							Lf = last_result;
							stack_reg.push(rd);
							stack_lab.push(Ld);
							this.visit(new Econst(1));
							Lt = last_result;
							RTLc(n.e2, Lt, Lf);
							

							RTLc(n.e1, Lt, last_result);
							return;
						}
						
					}
				}
			}
			if (debug) System.out.println("Ebinop Fin");
		}
		 
		 public void visit(Eunop nexpr) {
			if (debug) System.out.println("Eunop Deb");
			Expr temp = reduction(nexpr);
			if(temp instanceof Eunop) {
				Eunop n = (Eunop)temp;
				if(n.u == Unop.Uneg) {
					Ebinop e = new Ebinop(Binop.Bsub, new Econst(0), n);
					this.visit(e);
					return;
				}
				else if(n.u == Unop.Unot) {
					Register rd = stack_reg.pop();
					Label Ld = stack_lab.pop();
					
					Label Lt, Lf; 
					
					stack_reg.push(rd);
					stack_lab.push(Ld);
					this.visit(new Econst(0));
					Lt = last_result;
					
					stack_reg.push(rd);
					stack_lab.push(Ld);
					this.visit(new Econst(1));
					Lf = last_result;
					
					RTLc(n.e, Lt, Lf);
					return;
				}
			}
			else {
				return;
			}
		 }
				
					
					 
	
	 
	 
	public void visit(Unop n) {
		 
	}

		public void visit(Binop n) {
		}

		public void visit(String n) {
		}

		public void visit(Tint n) {
			
			if (debug) System.out.println("Tint Deb"); 
			Register rd = stack_reg.pop(); 
			Label Ld = stack_lab.pop(); 
			//Label Ld = new Label(); 
			Label L1 = new Label(); 
			current_graph.graph.put(L1, new Rconst(0, rd, Ld)); 
			
			last_result = L1;
			if (debug) System.out.println("Tint Fin"); 
		}

		public void visit(Tstructp n) {
			
			if (debug) System.out.println("Tstructp Deb"); 
			Register rd = stack_reg.pop(); 
			Label Ld = stack_lab.pop(); 
			//Label Ld = new Label(); 
			Label L1 = new Label(); 
			
			current_graph.graph.put(L1, new Rconst(0, rd, Ld)); 
			last_result = L1;
			if (debug) System.out.println("Tstructp Fin"); 
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
			
			if (debug) System.out.println("Eaccess_local Deb"); 
			Register r = fun_locals.get(n.i); 
			Register rd = stack_reg.pop(); 
			Label Ld = stack_lab.pop();
	
			Label L1 = new Label(); 
			

			current_graph.graph.put(L1 ,new Rmbinop(Mbinop.Mmov,r, rd , Ld));
			
			 
			 //stack_reg.push(r); 
			 //stack_lab.push(Ld);
			last_result = L1; 
			if (debug) System.out.println("Eaccess_local Fin"); 
		}

		public void visit(Eaccess_field n) {
			if (debug) System.out.println("Eaccess_field Deb"); 
			//if (debug) System.out.println(n.e); 
			
			if(n.e instanceof Eaccess_local){
			
				Register r = fun_locals.get(((Eaccess_local)n.e).i); 
			
				int i = n.f.pos ; 
			 
				Register rd = stack_reg.pop(); 
				Label Ld = stack_lab.pop();
			
				Label L1 = new Label(); 
			 

				current_graph.graph.put(L1 ,new Rload(r, Memory.word_size*i, rd , Ld)); 
			 
				last_result = L1; 
			}
			else if (n.e instanceof Eaccess_field){
				int i = n.f.pos;
				Register rd = stack_reg.pop(); 
				Label Ld = stack_lab.pop();
				
				Label L1 = new Label();
				Register rtemp = new Register();
				stack_lab.push(L1);
				stack_reg.push(rtemp);
				this.visit(n.e);
				//Label temp = last_result;
				current_graph.graph.put(L1 ,new Rload(rtemp, Memory.word_size*i, rd , Ld));
				//last_result = temp;
			}
			 
			
			if (debug) System.out.println("Eaccess_field Fin"); 
		}
		
public void visit(Eassign_local n) {
			
			if(debug) System.out.println("Eassign_local Deb"); 
			
			Register r = fun_locals.get(n.i); 
			
			Register rd = stack_reg.pop(); 
			Label Ld = stack_lab.pop(); 
			Label L2 = new Label() ;
			Label L1 = new Label() ; 
			if(n.e instanceof Econst) {
				current_graph.graph.put(L1, new Rconst(((Econst)n.e).i, r , L2)); 
				current_graph.graph.put(L2, new Rconst(((Econst)n.e).i, rd , Ld)); 
				last_reg.push(r); 
				last_result = L1; 
				return; 
			}
			if(n.e instanceof Eaccess_local) {
				Register r2 = fun_locals.get(((Eaccess_local)n.e).i); 
				current_graph.graph.put(L1, new Rmbinop(Mbinop.Mmov,r2 ,r , L2)); 
				current_graph.graph.put(L2, new Rmbinop(Mbinop.Mmov,r2 ,rd , Ld)); 
				last_result = L1; 
				last_reg.push(r) ; 
				return; 
			}
			if(n.e instanceof Esizeof) {
				current_graph.graph.put(L1, new Rconst(((Esizeof)n.e).s.fields.size(), rd, Ld ));
				last_result = L1; 
				return; 
			}
			Register r2 = new Register(); 
			stack_lab.push(L1); 
			stack_reg.push(r2);
			this.visit(n.e);
			current_graph.graph.put(L1, new Rmbinop(Mbinop.Mmov,r2, r , L2)); 
			current_graph.graph.put(L2, new Rmbinop(Mbinop.Mmov,r2, rd , Ld)); 
			 //last_result = L1; 
			if(debug) System.out.println("(Eassign_local Fin"); 
		}

		

		public void visit(Eassign_field n) {
			if (debug) System.out.println("Eassign field deb"); 
			Eaccess_field tmp = (Eaccess_field)n.e1;
			if(tmp.e instanceof Eaccess_local) {
				Register r = fun_locals.get(((Eaccess_local)tmp.e).i); 
				
				int i = n.f.pos ; 
				
				Register rd = stack_reg.pop(); 
				Label Ld = stack_lab.pop();
				
				Label L1 = new Label(); 
				 
				Register rr = new Register(); 
				stack_reg.push(rr); 
				stack_lab.push(L1); 
				this.visit(n.e2);
				current_graph.graph.put(L1 ,new Rstore(rr, r, Memory.word_size*i , Ld)); 
			}
			else if(tmp.e instanceof Eaccess_field){
				Eaccess_field tmpe = (Eaccess_field)tmp.e;
				int i = n.f.pos;
				
				Register rd = stack_reg.pop();
				Label Ld = stack_lab.pop();
				
				Label L1 = new Label();
				Register rtemp = new Register();
				stack_lab.push(L1);
				stack_reg.push(rtemp);
				this.visit(tmpe);
				Label L2 = last_result;
				
				Register rtemp2 = new Register();
				stack_lab.push(L2);
				stack_reg.push(rtemp2);
				this.visit(n.e2);
				
				current_graph.graph.put(L1 ,new Rstore(rtemp2, rtemp, Memory.word_size*i, Ld)); 
			}
			if (debug) System.out.println("Eassign field fin"); 
		}

public void visit(Ecall n) {
			
			if(debug) System.out.println("Ecall Deb"); 
			List<Register> rl = new LinkedList<>();
			//Register r; 
			//Label l = new Label(); 
			
			//parcours sens inverse: 
			int ss = n.el.size(); 
			//ListIterator<Expr> iterator =n.el.listIterator(ss);
			 
			 

		 
			int i = 0; 
		
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
				
					L1 = new Label(); 
				
				current_graph.graph.put(L1, new Rcall(rd, n.i, rl, Ld)); 
				last_result = L1; 
				return; 
			}
			
			Label L3 = new Label(); 
			Register r2 = new Register(); 
			stack_lab.push(L3);
			stack_reg.push(r2);
			 
			
			rl.add(r2); 
			for(Expr e: n.el) {
				
				/*
				r = new Register(); 
				if(last_result !=null)
					l = last_result;
				stack_lab.push(last_result); 
				stack_reg.push(r); 
				this.visit(e);
				rl.add(r); 
				*/
				Expr ee = reduction(e); 
				this.visit(ee); 
				L2 = last_result; 
				stack_lab.push(L2);
				if(i++ == ss - 1)
					stack_reg.push(rd);
				else {
					Register rr = new Register(); 
					stack_reg.push(rr); 
					rl.add(rr); 
				}
				last_result = L2;
				//l = last_result; 
				
			}
			
			//Label L = new Label(); 
			//Register ress = new Register(); 
			//stack_lab.push(L); 
			//stack_reg.push(ress); 
			current_graph.graph.put(L3, new Rcall(rd, n.i, rl, Ld)); 
			if(debug) System.out.println("Ecall Fin"); 
		}
		
		public void visit(Esizeof n) {
			
			Register rd = stack_reg.pop(); 
			Label Ld = stack_lab.pop(); 
			Label L1 = new Label(); 
			current_graph.graph.put(L1, new Rconst(Memory.word_size * n.s.fields.size(), rd, Ld )); 
			last_result = L1; 
				
		}

		public void visit(Sskip n) {
			
			if (debug) System.out.println("Deb Sskip"); 
			
			//last_result =  stack_lab.pop() ;
			//stack_reg.pop() ;
			
			 
			
			if (debug) System.out.println("Fin Sskip"); 
		}

		public void visit(Sexpr n) {
			if (debug) System.out.println("Sexpr Deb"); 
			
			this.visit(n.e);
			
			if (debug) System.out.println("Sexpr Fin"); 
		}
		
		
		
		
		public Label RTLc(Expr e, Label Lt, Label Lf) {
			Label res = new Label(); 
			Register r = new Register(); 
			Label L2 = new Label(); 
			stack_reg.push(r); 
			stack_lab.push(L2); 
			
		
			if(e instanceof Ebinop) {
				Ebinop tmp = (Ebinop)e; 
				
				if(tmp.b == Binop.Band) {
					Label Ltmp = RTLc(tmp.e2, Lt, Lf); 
					return RTLc(tmp.e1, Ltmp, Lf); 
				}
				if(tmp.b == Binop.Bor){
					Label Ltmp = RTLc(tmp.e2, Lt, Lf); 
					return RTLc(tmp.e1, Lt, Ltmp); 
				}
				
				if(tmp.b == Binop.Ble ) {
					Label LL3 = new Label(); 
					Label LL2;// = new Label(); 
					Register rr2 = new Register(); 
					Register rr1 = new Register(); 
					
					
					stack_lab.push(LL3); 
					stack_reg.push(rr2); 
					this.visit(tmp.e2);
					LL2 = last_result; 
					stack_lab.push(LL2); 
					stack_reg.push(rr1); 
					this.visit(tmp.e1);
					Label LL1 = last_result; 
					
					
					Mbbranch m ; 
					if(tmp.b == Binop.Ble)
						m = Mbbranch.Mjle; 
					else
						m = Mbbranch.Mjl; 
					current_graph.graph.put(LL3, new Rmbbranch(m, rr2, rr1, Lt, Lf)); 
					return LL1; 
					
				}
				
				if(tmp.e2 instanceof Econst) {
					
					if(tmp.b == Binop.Ble) {
						Register rrd = stack_reg.pop(); 
						Label LLd = stack_lab.pop();
						
						 //Label LL1, LL2; 
						  
						 Label LL3 = new Label(); 
						 Register rr2 = new Register(); 
						 
						 stack_lab.push(LL3);
						 stack_reg.push(rr2);
						 Mubranch um; 
						
						 stack_reg.push(rrd);
						 this.visit(tmp.e1);
						 L2 = last_result;
						 um = new Mjlei(((Econst)tmp.e2).i); 
						 //current_graph.graph.put(LL3, new Rmunop(um, rrd, LLd));
						 current_graph.graph.put(LL3, new Rmubranch(um, rrd, Lt, Lf));
						 return LL3;  
						 
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
				
					 stack_reg.push(rrd);
					 this.visit(tmp.e1);
					 L2 = last_result;
					 um = new Mjgi(((Econst)tmp.e2).i); 
					 //current_graph.graph.put(LL3, new Rmunop(um, rrd, LLd));
					 current_graph.graph.put(LL3, new Rmubranch(um, rrd, Lt, Lf));
					 return LL3; 
					}
				}
				 
			}
			
			
			this.visit(e); 
			Mjz m = new Mjz(); 
			current_graph.graph.put(L2, new Rmubranch(m, r, Lf, Lt));
			 
			//System.out.println(new Rmubranch(m, r, Lt, Lf));
			return L2; 
		}

		public void visit(Sif n) {
			if (debug) System.out.println("Deb Sif"); 
			
			Label Lt, Lf; 
			Stack<Label> stack_lab_tmp = (Stack<Label>)stack_lab.clone();
			Stack<Register> stack_reg_tmp = (Stack<Register>)stack_reg.clone();
			this.visit(n.s2);
			Lf = last_result; 
			stack_lab = stack_lab_tmp;
			stack_reg = stack_reg_tmp;
			
			this.visit(n.s1);
			Lt = last_result; 
			
			
			
			RTLc(n.e, Lt, Lf); 
			
			 
			if (debug) System.out.println("Fin Sif"); 
		}

		public void visit(Swhile n) {
			if (debug) System.out.println("Deb Swhile"); 
			Label Ld = stack_lab.pop(); 
			Label L= new Label(); 
			stack_lab.push(L);
			this.visit(n.s);
			Label l = last_result;
			if (debug) System.out.println(n.e+"\n\nUOOOOOOOOOOOOOOOOOOOO");
			Label Le = RTLc(n.e, l, Ld);
			
			 
			current_graph.graph.put(L, new Rgoto(last_result)); 
			if (debug) System.out.println("Fin Swhile"); 
			 
		}

		
		
	 
}
