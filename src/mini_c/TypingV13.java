package mini_c;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

public class Typing implements Pvisitor {

	// le résultat du typage sera mis dans cette variable
	private File file;
	
	private int nb_args; 
	private int cursor = 0;
	/*
	enum Block{
		IDENT_DECL, IDENT_PBLOC, IDENT_SRETURN, IDENT_SIF, IDENT_PEVAL;
	}
	enum Expression{
		IDENT_ECONST, IDENT_EACCES_LOCAL, IDENT_EACCES_FIELDS, IDENT_EASSIGN_LOCAL,	IDENT_EASSIGN_FIELD, IDENT_EUNOP, IDENT_EBINOP, IDENT_ECALL, IDENT_ESIZEOF, IDENT_PARROW, IDENT_PASSIGN;
	}*/
	final private static int IDENT_DECL= 0, IDENT_PBLOC = 1, IDENT_SRETURN = 2, IDENT_SIF = 3, IDENT_PEVAL = 4;
	private int ident_block = 0;
	 
	final private static int IDENT_ECONST=0, IDENT_EACCES_LOCAL=1, IDENT_EACCES_FIELDS=2, IDENT_EASSIGN_LOCAL = 3, 
			IDENT_EASSIGN_FIELD = 4, IDENT_EUNOP = 5, IDENT_EBINOP = 6, IDENT_ECALL = 7, IDENT_ESIZEOF = 8, IDENT_PARROW=9, IDENT_PASSIGN=10; 
	
	final private static int STRUCT_NAME=1, STRUCT_VARNAME=2, STRUCT_FIELD=3, STRUCT_DECL=4;
	private int ident_expr = 0; 
	private int cursor_binop=0; 
	private int cursor_assign=0; 
	
	private String NAME_FIELD;
	private Typ TYP_FIELD;
	
	public Stack<Integer> stack_expr = new Stack<>(); 
	public Stack<Integer> stack_block = new Stack<>(); 
	public Stack<Expr> stack_addr_expr = new Stack<>(); 
	public Stack<Sblock> stack_addr_block = new Stack<>(); 
	public Stack<Stmt> stack_addr_stmt = new Stack<>();
	
	public Stack<Integer> stack_struct = new Stack<>();
	
	public String name_struct;
	
	public int nb_main = 0;
	
	public int func = 0;
	
	public String type_field;
	private String ENTIER="0";  
	
	//public LinkedList<Structure> structure;
	public HashMap<String, Structure> structure;
	// et renvoyé par cette fonction
	File getFile() {
		if (file == null)
			throw new Error("typing not yet done!");
		return file;
	}
	
	public void nop() {
		System.out.println("NOPEEEEEEEEEEEEEEEEEEEEEE " + stack_addr_expr.pop());
	}
	
	public void op() {
		System.out.println("NOPEEEEEEEEEEEEEEEEEEEEEE " + stack_addr_expr.peek());
	}


	// il faut compléter le visiteur ci-dessous pour réaliser le typage

	@Override
	public void visit(Pfile n) {
		// TODO Auto-generated method stub
		//LinkedList<Pdecl> l = n.l;
		file = new File(new LinkedList<Decl_fun>());
		stack_struct.add(0);
		structure = new HashMap<>();
		for (Pdecl tmp: n.l) {
			if(tmp instanceof Pstruct) {
				this.visit((Pstruct) tmp);
			} 
			else if(tmp instanceof Pfun) {
				this.visit((Pfun) tmp);
			}
			else {
				throw new Error("Visit Pfile"); 
			}
		}
		if(nb_main==0)
			throw new Error("main function not found");
	}

	@Override
		public void visit(Pident n) {
			// TODO Auto-generated method stub
			System.out.println("Pident Deb");
			Expr tmp;
	
			if(stack_struct.peek()==0) {
				
				if(cursor==0 && stack_struct.peek()==0) {
					for(Decl_fun funs : file.funs) {
						if(funs.fun_name != null && funs.fun_name.equals(n.id))
							throw new Error("redefinition of function at location "+n.loc);
					}
					file.funs.getLast().fun_name = n.id;
					if(n.id.equals("main")) nb_main++;
					if(nb_main>1)
						throw new Error(nb_main + " functions named main");
				}
				else if(cursor <=nb_args && stack_struct.peek()==0) {
					for(Decl_var var : file.funs.getLast().fun_formals) {
						if(var.name != null && var.name.equals(n.id))
							throw new Error("redefinition of variable at location "+n.loc);
					}
					file.funs.getLast().fun_formals.getLast().name = n.id;
				}
				else {
					
					Sblock b = stack_addr_block.pop(); 
					stack_addr_block.push(b); 
					
					ident_block = stack_block.peek();  
					
					switch(ident_block) {
					
						case IDENT_DECL:
							for(Decl_var var : b.dl) {
								if(var.name != null && var.name.equals(n.id)) {
									throw new Error("redefinition of variable at "+n.loc);
								}
							}
							b.dl.getLast().name = n.id;
							break; 
						default:
							int present = 0;
							for(Sblock blc : stack_addr_block) {
								if(blc != null) {
									for(Decl_var var : blc.dl) {
										if(var.name.equals(n.id)) {
											present = 1;
											break;
										}
									}
									if(present==1) break;
								}
							}
							
							if(present==0) {
								for(Decl_var var : file.funs.getLast().fun_formals) {
									if(var.name.equals(n.id)) {
										present = 1;
										break;
									}
								}
							}
							if(present==0)
								throw new Error("unknown variable at " + n.loc);
							stack_addr_expr.add(new Eaccess_local(n.id));
							
					}
				}
			}
			else if (stack_struct.peek()==STRUCT_NAME) {
				structure.put(n.id,new Structure(n.id));
				name_struct = n.id; 
			}
			else if (stack_struct.peek()==STRUCT_FIELD) {
				if (structure.get(name_struct).fields.containsKey(n.id))
					throw new Error("redefinition of variable at location " + n.loc);
				if(type_field.equals("0"))
					structure.get(name_struct).fields.put(n.id, new Field(n.id, new Tint()));
				else {
					structure.get(name_struct).fields.put(n.id, new Field(n.id, new Tstructp(structure.get(type_field))));
				}
	
			}
			else if(stack_struct.peek()==STRUCT_VARNAME) {
				Sblock b = stack_addr_block.pop(); 
	
				b.dl.getLast().name = n.id;
				stack_addr_block.push(b);
			}
			System.out.println("Pident Fin"); 
	
		}

	@Override
	public void visit(PTint n) {
		// TODO Auto-generated method stub
		
		System.out.println("Ptint deb "+func); 
		if(stack_struct.peek()==0) {
			if(cursor==0)
				file.funs.add(new Decl_fun(new Tint(), null, null, null));
			else if(cursor <=nb_args) {
				file.funs.getLast().fun_formals.add(new Decl_var(new Tint(), null)); 
			}
			else {

				Sblock b = stack_addr_block.pop();  
				b.dl.add(new Decl_var(new Tint(), null)); 
				stack_addr_block.push(b); 
			}
		}
		else if(stack_struct.peek()==STRUCT_FIELD) {
			type_field = "0";
		}
		System.out.println("Ptint Fin"); 
	}

	@Override
	public void visit(PTstruct n) {
		// TODO Auto-generated method stub
		System.out.println("Deb PTstruct");
		/*
		if(stack_struct.peek()==STRUCT_VARNAME) {			
			name_struct = n.id;
			Sblock b = stack_addr_block.pop(); 
			System.out.println("b = "+b); 
			Structure s=null;
			
			s = structure.get(name_struct); 
			b.dl.add(new Decl_var(new Tstructp(s), null));
			stack_addr_block.push(b);
		}
		else {
			System.out.println("Nope");
		}
		*/
		Structure s; 
		if(stack_struct.peek()==0) {
			if(cursor==0) {
				s = structure.get(n.id);
				if(s == null)
					throw new Error("No such struct "+n.loc); 
				else
					file.funs.add(new Decl_fun(new Tstructp(s), null, null, null));
			}	
			else if(cursor <=nb_args) {
				s = structure.get(n.id);
				if(s == null)
					throw new Error("No such struct "+n.loc); 
				else
					file.funs.getLast().fun_formals.add(new Decl_var(new Tstructp(s), null)); 
			}
			else {
				 
				Sblock b = stack_addr_block.pop(); 
				 
				b.dl.add(new Decl_var(new Tstructp( structure.get(n.id)), null)); 
				stack_addr_block.push(b); 
			}
		}
		else if(stack_struct.peek()==STRUCT_FIELD) {

			//structure.getLast().fields.put(null, new Field(null, new Tint()));
			if(structure.get(n.id)==null)
				throw new Error("structure not defined at location " + n.loc);
			type_field = n.id;
		}
		System.out.println("Fin PTstruct");
	}

	@Override
	public void visit(Pint n) {
		// TODO Auto-generated method stub
		System.out.println("Deb Pint");
		stack_addr_expr.push(new Econst(n.n));
		System.out.println("Fin Pint"); 	
	}

	@Override
	public void visit(Punop n) {
		// TODO Auto-generated method stub
	//unop = 1; 
	//int prev = ident_expr; 
	System.out.println("Eunop Deb"); 
	Sblock temp = stack_addr_block.peek();

	if(temp != null) {
		if(n.e1 instanceof Pident) {
			for(Decl_var var : temp.dl) {
				if(var.t instanceof Tstructp && var.name.equals(((Pident)n.e1).id) && n.op.toString().equals("Uneg"))
					throw new Error("incompatible type operation at location " + n.loc);
			}
		}
	}
	else		
		throw new Error("incompatible type");
	try {
		ident_expr = stack_expr.peek();
	}
	catch(EmptyStackException e) {
		ident_expr = IDENT_EUNOP;
	}
	stack_expr.push(IDENT_EUNOP);
	ident_block = stack_block.peek();
	Expr e; 
	switch(ident_block)	{
		case IDENT_SRETURN: 
			switch(ident_expr) {
			case IDENT_EBINOP:
				e = new Eunop(n.op, null); 
				stack_addr_expr.push(e);
				this.visit(n.e1);
				
				break;
			case IDENT_EUNOP:
				e = new Eunop(n.op, null); 
				stack_addr_expr.push(e);
				this.visit(n.e1);
				((Eunop)e).e = stack_addr_expr.pop();
				break;
			case IDENT_ECALL:
				e = new Eunop(n.op, null); 
				stack_addr_expr.push(e);
				this.visit(n.e1);
				((Eunop)e).e = stack_addr_expr.pop();
				break;
			default:
				throw new Error("visit Punop "+n.loc);
			
		}
			break;
		case IDENT_PEVAL:
			switch(ident_expr) {
				case IDENT_EBINOP:
					e = new Eunop(n.op, null); 
					stack_addr_expr.push(e);
					
					this.visit(n.e1);
					
					((Eunop)e).e = stack_addr_expr.pop();
					
					
					break;
				case IDENT_EUNOP:
					e = new Eunop(n.op, null); 
					stack_addr_expr.push(e);
					this.visit(n.e1);
					((Eunop)e).e = stack_addr_expr.pop();
					break;
				case IDENT_ECALL:
					e = new Eunop(n.op, null); 
					stack_addr_expr.push(e);
					this.visit(n.e1);
					((Eunop)e).e = stack_addr_expr.pop();
					break;
				default:
					throw new Error("visit Punop "+n.loc);
			}	
			break;
		case IDENT_SIF:
			e = new Eunop(n.op, null); 
			stack_addr_expr.push(e);
			this.visit(n.e1);
			((Eunop)e).e = stack_addr_expr.pop();
			break;
		default:
			throw new Error("visit Punop "+n.loc);
	}
	
	stack_expr.pop(); 
	System.out.println("Eunop Fin");
	
	}

	@Override
	public void visit(Passign n) {
		// TODO Auto-generated method stub
		System.out.println("Passign Deb"); 
		stack_expr.push(IDENT_PASSIGN); 
		Expr e;
		if(n.e1 instanceof Parrow) {
			e = new Eassign_field(null, null, null);
			this.visit(n.e1);
			
			((Eassign_field)e).e1 = stack_addr_expr.pop();
			
			
			if (TYP_FIELD instanceof Tint) {
				((Eassign_field)e).f = new Field(NAME_FIELD, new Tint());
			}
			else if(TYP_FIELD instanceof Tstructp) {
				((Eassign_field)e).f = new Field(NAME_FIELD, new Tstructp(structure.get(((Tstructp)TYP_FIELD).s.str_name)));
			}
			this.visit(n.e2);
			((Eassign_field)e).e2 = stack_addr_expr.pop();
			stack_addr_expr.push(e);
		}
		else {
			e = new Eassign_local(((Pident)n.e1).id, null);
			this.visit(n.e2);
			((Eassign_local)e).e = stack_addr_expr.pop();
			stack_addr_expr.push(e);
		}
		stack_expr.pop();
		System.out.println("Passign Fin"); 
	}

	@Override
	public void visit(Pbinop n) {
		// TODO Auto-generated method stub
		System.out.println("Binop Deb");
		Sblock temp = stack_addr_block.peek();
		
		if(temp != null) {
			if(n.e1 instanceof Pident) {
				for(Decl_var var : temp.dl) {
					if(var.t instanceof Tstructp && var.name.equals(((Pident)n.e1).id))
						throw new Error("incompatible type operation at location " + n.loc);
				}
			}
			if(n.e2 instanceof Pident) {
				for(Decl_var var : temp.dl) {
					if(var.t instanceof Tstructp && var.name.equals(((Pident)n.e2).id))
						throw new Error("incompatible type operation at location " + n.loc);
				}
			}
		}
		else		
			throw new Error("incompatible type");
			
		
		stack_expr.push(IDENT_EBINOP);
		ident_block = stack_block.peek();
		Expr e; 
		cursor_binop = 0;
		switch(ident_block)	{
			case IDENT_SRETURN:	
				e = new Ebinop(n.op, null, null); 
				//stack_addr_expr.push(e); 				 
				this.visit(n.e1);
				((Ebinop)e).e1 = stack_addr_expr.pop(); 
				cursor_binop = 1; 
				this.visit(n.e2);
				((Ebinop)e).e2 = stack_addr_expr.pop();
				stack_addr_expr.push(e);
				break;
			case IDENT_PEVAL:
				e = new Ebinop(n.op, null, null); 
				//stack_addr_expr.push(e); 
				this.visit(n.e1);
				((Ebinop)e).e1 = stack_addr_expr.pop(); 
				cursor_binop = 1;
				
				this.visit(n.e2);
				
				((Ebinop)e).e2 = stack_addr_expr.pop();
				stack_addr_expr.push(e);
				break;
			case IDENT_SIF:
				e = new Ebinop(n.op, null, null); 
				//stack_addr_expr.push(e); 				 
				this.visit(n.e1);
				((Ebinop)e).e1 = stack_addr_expr.pop(); 
				cursor_binop = 1; 
				this.visit(n.e2);
				
				((Ebinop)e).e2 = stack_addr_expr.pop();
				stack_addr_expr.push(e);
				break;
			default:
				throw new Error("visit Pbinop "+n.loc);
			}			
			
			stack_expr.pop(); 
			System.out.println("Binop Fin"); 
		
	}

	@Override
	public void visit(Parrow n) {
		System.out.println("Parrow Deb");
		Expr e = new Eaccess_field(null, new Field(n.f, null));
		if(n.e instanceof Pident) {
			
//			this.visit(n.e);
			Decl_var vart=null;
			
			((Eaccess_field)e).e = new Eaccess_local(((Pident)n.e).id);

			int present=0;
			for(Sblock blc : stack_addr_block) {
				if(blc != null) {
					for(Decl_var var : blc.dl) {
						if(var.name.equals(((Pident)n.e).id) && var.t instanceof Tstructp) {
							present = 1;
							vart = var;
							break;
						}
					}
					if(present==1) break;
				}
			}
			if(present==0) {
				for(Decl_var var : file.funs.getLast().fun_formals) {
					if(var.name.equals(((Pident)n.e).id) && var.t instanceof Tstructp) {
						present = 1;
						vart = var;
						break;
					}
				}
			}
			if(present==0)
				throw new Error("unknown variable at " + n.loc);
			((Eaccess_field)e).f.field_name = NAME_FIELD = structure.get(((Tstructp)vart.t).s.str_name).fields.get(n.f).field_name;
			((Eaccess_field)e).f.field_typ = TYP_FIELD = structure.get(((Tstructp)vart.t).s.str_name).fields.get(n.f).field_typ;
			stack_addr_expr.push(e);
		}
		else if(n.e instanceof Pint)
			throw new Error("Nope at location "+ n.loc);
		else if(n.e instanceof Pcall) {
			int present = 0;
			for(Decl_fun fun : file.funs) {
				if(fun.fun_name.equals(((Pcall)n.e).f) && fun.fun_typ instanceof Tstructp) {
					present = 1;
					break;
				}
			}
			if(present==1) {
				this.visit(n.e);
				((Eaccess_field)e).e = stack_addr_expr.pop();
				((Eaccess_field)e).f.field_typ = TYP_FIELD;
				((Eaccess_field)e).f.field_name = NAME_FIELD;
				stack_addr_expr.push(e);
			}
			throw new Error("Nope at location " + n.loc);
			
		}
		else{
			this.visit(n.e);
			((Eaccess_field)e).e = stack_addr_expr.pop();
			((Eaccess_field)e).f.field_typ = TYP_FIELD;
			stack_addr_expr.push(e);
		}
		
		
		System.out.println("Parrow Fin");
	}

	@Override
	public void visit(Pcall n) {
		// TODO Auto-generated method stub
		System.out.println("Pcall Deb");

		int cmp=0;
		for(Decl_fun funs: file.funs) {
			if(funs.fun_name.equals(n.f)) break;
			cmp++;
		}
		if(cmp==file.funs.size() && n.f.equals("putchar")==false && n.f.equals("sbrk")==false)
			throw new Error("function " + n.f + " not found");
		int taille = (cmp==file.funs.size()) ? 1 : n.l.size();
		if(cmp < file.funs.size() && file.funs.get(cmp).fun_formals.size() != taille)
			throw new Error("bad arguments number");
		else if(cmp == file.funs.size() && n.l.size()!=1)
			throw new Error("bad arguments number");
		else {
			if(n.f.equals("srbk")) {
				// Maybe supprimer le if
			}
			else {
				Expr e;
				stack_block.push(IDENT_PEVAL);
				stack_expr.push(IDENT_ECALL);
				e = new Ecall(n.f, new LinkedList<>());
				stack_addr_expr.push(e);
				
				for(int i=0; i<taille; i++) {
					Pexpr vars = n.l.get(i);
					//stack_addr_expr.push(null);
					
					this.visit(vars);
					
					// Verifier que les types sont bien faits...
					Expr f = stack_addr_expr.pop();
					

					e = stack_addr_expr.pop();
					
					((Ecall)e).el.add(f);
					stack_addr_expr.push(e);
					
					
				}
				stack_block.pop();
				stack_expr.pop();
				
			}
			System.out.println("Pcall Fin");
		}
	}

	@Override
	public void visit(Psizeof n) {
		// TODO Auto-generated method stub
		System.out.println("Psizeof Deb");
		if(structure.get(n.id)==null)
			throw new Error("Psizeof: structure not found at location " + n.loc);
		Expr e = new Esizeof(structure.get(n.id));
		stack_addr_expr.push(e);
		System.out.println("Psizeof Fin");
	}

	@Override
	public void visit(Pskip n) {
		// TODO Auto-generated method stub
		System.out.println("Pskip Deb");
		Sblock b = new Sblock(new LinkedList<Decl_var>(), new LinkedList<Stmt>()); 
		b.sl.add(new Sskip());
		stack_addr_block.push(b);
		System.out.println("Pskip Fin");
	}

	@Override
	public void visit(Peval n) {
		// TODO Auto-generated method stub
		System.out.println("Peval Deb");
		stack_block.push(IDENT_PEVAL);
		Pexpr tmp = n.e; 
		Sblock b = stack_addr_block.pop(); 
		stack_addr_block.push(b);
		Expr e = null; 
		stack_addr_expr.push(e);
		this.visit(tmp);
		
		b.sl.add(new Sexpr(stack_addr_expr.pop())) ; 
		stack_addr_block.push(b);
		
		stack_block.pop();
		
		System.out.println("Peval Fin");
	}

	@Override
	public void visit(Pif n) {
		// TODO Auto-generated method stub
		System.out.println("Pif Deb");
		 
		stack_block.push(IDENT_SIF);
		
		Pexpr tmp = n.e; 
		 
		
		Sblock b = stack_addr_block.pop(); 
		b.sl.add(new Sif(null, null, null)); 
		
		 
		stack_addr_expr.push(null); 
		stack_addr_block.push(b); 
		this.visit(tmp);
		
		
		((Sif)b.sl.getLast()).e = stack_addr_expr.pop() ; 
		 
		
		//Sblock bb = new Sblock(new LinkedList<>(), new LinkedList<>()); 
		//stack_addr_block.push(bb);
		//stack_addr_stmt.push(null);
		
		
		if(n.s1 instanceof Pbloc) {
			this.visit(n.s1);
			((Sif)b.sl.getLast()).s1 = stack_addr_block.pop();
		}
		else{
			Sblock bb = new Sblock(new LinkedList<>(), new LinkedList<>());
			stack_addr_block.push(bb);
			this.visit(n.s1);
			((Sif)b.sl.getLast()).s1 = stack_addr_block.pop();
		}
		
		//bb = new Sblock(new LinkedList<>(), new LinkedList<>()); 
		//stack_addr_block.push(bb);
		//stack_addr_stmt.push(null);
		
		if(n.s2 instanceof Pbloc) {
			this.visit(n.s2);
			((Sif)b.sl.getLast()).s2 = stack_addr_block.pop();
		}
		else{
			Sblock bb = new Sblock(new LinkedList<>(), new LinkedList<>());
			stack_addr_block.push(bb);
			this.visit(n.s2);
			((Sif)b.sl.getLast()).s2 = stack_addr_block.pop();
		}
		
		
		
		stack_addr_block.pop();
		stack_addr_block.push(b);
		
		System.out.println("Pif fin"); 
	}

	@Override
	public void visit(Pwhile n) {
		// TODO Auto-generated method stub
		System.out.println("Pwhile Deb");
		System.out.println("Pwhile Fin");
		
	}

	@Override
	public void visit(Pbloc n) {
		// TODO Auto-generated method stub
		
		System.out.println("Pbloc deb"); 
		stack_block.push(IDENT_DECL);

		Sblock b = new Sblock(new LinkedList<Decl_var>(), new LinkedList<Stmt>()); 
		stack_addr_block.push(b);
		
		
		for(Pdeclvar tmp: n.vl) {
			if(tmp.typ instanceof PTint) {
				this.visit((PTint)tmp.typ );
			}
			else if(tmp.typ instanceof PTstruct) {
				this.visit((PTstruct)tmp.typ );
			} 
			else {
				throw new Error("visit Pfun Pdeclvar "+tmp.loc); 
			}
			this.visit(new Pident(new Pstring(tmp.id, tmp.loc)));
		} 
		stack_block.pop(); 
		
		
		for(Pstmt tmp: n.sl) {
			this.visit(tmp);
		}
		stack_addr_block.pop();
		System.out.println("Pbloc fin");
	}

	@Override
	public void visit(Preturn n) {
		// TODO Auto-generated method stub
		System.out.println("Preturn deb"); 
		//int prev = ident_expr; 
		stack_block.push(IDENT_SRETURN);
		//System.out.println("oui "+ stack_block.peek());
		Pexpr tmp = n.e; 
		

		Sblock b = stack_addr_block.pop(); 
		//b.sl.add(new Sreturn(null)); 
		stack_addr_block.push(b); 
		// A revoir
		Expr e = null; 
		stack_addr_expr.push(e);
		this.visit(tmp);
		
		b.sl.add(new Sreturn(stack_addr_expr.pop())) ; 
		stack_addr_block.push(b);
		stack_block.pop();
		//((Sblock)file.funs.getLast().fun_body).sl.add(new Sreturn(stack_addr_expr.pop())); 
		//System.out.println(((Sblock)file.funs.getLast().fun_body).sl.removeLast() == null); 
		System.out.println("Preturn fin"); 

	}

	@Override
	public void visit(Pstruct n) {
		// TODO Auto-generated method stub
		System.out.println("Pstruct Deb");
		stack_struct.add(STRUCT_NAME);
		this.visit(new Pident(new Pstring(n.s, n.fl.getFirst().loc)));
		stack_struct.pop();
		for(Pdeclvar tmp : n.fl) {
			type_field="0";
			stack_struct.add(STRUCT_FIELD);
			if (tmp.typ instanceof PTint) {
				this.visit((PTint)tmp.typ);
			} else if (tmp.typ instanceof PTstruct) {
				this.visit((PTstruct)tmp.typ);
			} else {
				throw new Error("visit Pfun Ptype "+tmp.loc);
			}
			
			
			this.visit(new Pident(new Pstring(tmp.id, tmp.loc)));
			stack_struct.pop();
		}
		System.out.println("Pstruct Fin");
	}

	@Override
	public void visit(Pfun n) {
		// TODO Auto-generated method stub
		func=1;
		Ptype ty = n.ty;
		nb_args = n.pl.size(); 
		if (ty instanceof PTint) {
			this.visit((PTint)ty);
		} else if (ty instanceof PTstruct) {
			this.visit((PTstruct)ty);
		} else {
			throw new Error("visit Pfun Ptype "+n.loc); 
		}
		this.visit(new Pident(new Pstring(n.s, n.loc)));
		
		
		cursor = 1;
		file.funs.getLast().fun_formals = new LinkedList<>(); 
		
		for (Pdeclvar tmp : n.pl) {
			
			if(tmp.typ instanceof PTint) {
				this.visit((PTint)tmp.typ );
				this.visit(new Pident(new Pstring(tmp.id, tmp.loc)));
			}
			else if(tmp.typ instanceof PTstruct) {
				
				this.visit((PTstruct)tmp.typ );
				this.visit(new Pident(new Pstring(tmp.id, tmp.loc)));
				
			} 
			else {
				throw new Error("visit Pfun Pdeclvar "+tmp.loc); 
			}
			cursor++; 
		}
		func=1; 
		//file.funs.getLast().fun_body; 
		//stack_addr_block.push(new Sblock(new LinkedList<>(), new LinkedList<>())); 
		stack_addr_stmt.push(null); 
		this.visit(n.b);
		try {
			file.funs.getLast().fun_body = stack_addr_block.pop();
		}
		catch(EmptyStackException e) {
		}
		cursor = 0;
	}

	public void visit(Pexpr tmp) {
		// TODO Auto-generated method stub	
		if(tmp instanceof Pint)
			this.visit((Pint)tmp);
		else if (tmp instanceof Pident) {
			
			this.visit((Pident)tmp);
		}
			
		else if (tmp instanceof Parrow) {
			
			this.visit((Parrow)tmp);
		}
			
		else if (tmp instanceof Plvalue)
			this.visit((Plvalue)tmp);
		else if (tmp instanceof Passign)
			this.visit((Passign)tmp);
		else if (tmp instanceof Pbinop)
			this.visit((Pbinop)tmp);
		else if (tmp instanceof Punop)
			this.visit((Punop)tmp);
		else if (tmp instanceof Pcall)
			this.visit((Pcall)tmp);
		else if (tmp instanceof Psizeof)
			this.visit((Psizeof)tmp);
		else; 
	}

	public void visit(Pstmt tmp) {
		// TODO Auto-generated method stub	
		 
		if(tmp instanceof Pbloc)
			this.visit((Pbloc)tmp);
		else if(tmp instanceof Pskip)
			this.visit((Pskip)tmp);
		else if(tmp instanceof Preturn)
			this.visit((Preturn)tmp);
		else if(tmp instanceof Pif) {
			this.visit((Pif)tmp); }
		else if(tmp instanceof Peval)
			this.visit((Peval)tmp);
		else if(tmp instanceof Pwhile)
			this.visit((Pwhile)tmp);
		else{
			throw new Error("Visit Pstmt "+tmp.loc);} 
	} 
		
	
}
