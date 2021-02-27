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
	final private static int IDENT_DECL= 0, IDENT_PBLOC = 1, IDENT_SRETURN = 2, IDENT_SIF = 3, IDENT_PEVAL = 4, IDENT_PASSIGN=5, IDENT_PCALL=6;
	private int ident_block = 0;
	//Expr 
	final private static int IDENT_ECONST=0, IDENT_EACCES_LOCAL=1, IDENT_EACCES_FIELS=2, IDENT_EASSIGN_LOCAL = 3, 
			IDENT_EASSIGN_FIELD = 4, IDENT_EUNOP = 5, IDENT_EBINOP = 6, IDENT_ECALL = 7, IDENT_ESIZEOF = 8; 
	
	final private static int STRUCT_NAME=1, STRUCT_VARNAME=2, STRUCT_FIELD=3, STRUCT_DECL=4;
	private int ident_expr = 0; 
	private int cursor_binop=0; 
	
	public Stack<Integer> stack_expr = new Stack<>(); 
	public Stack<Integer> stack_block = new Stack<>(); 
	public Stack<Expr> stack_addr_expr = new Stack<>(); 
	public Stack<Sblock> stack_addr_block = new Stack<>(); 
	public Stack<Stmt> stack_addr_stmt = new Stack<>();
	
	public Stack<Integer> stack_struct = new Stack<>();
	
	public String name_struct;
	
	public int nb_main = 0;
	
	public int func = 0;
	
	public int type_field = 0;
	final private static int ENTIER=1, STRUCT=2;  
	
	//public LinkedList<Structure> structure;
	public HashMap<String, Structure> structure;
	// et renvoyé par cette fonction
	File getFile() {
		if (file == null)
			throw new Error("typing not yet done!");
		return file;
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
	public void visit(PTint n) {
		// TODO Auto-generated method stub
		
		System.out.println("Ptint deb "+func); 
		/*if(func == 0) {
			System.out.println("laa"); 
			Sblock b = stack_addr_block.pop(); 
			b.dl.add(new Decl_var(new Tint(), null)); 
			stack_addr_block.push(b); 
		}
		else {*/
		if(stack_struct.peek()==0) {
			if(cursor==0)
				file.funs.add(new Decl_fun(new Tint(), null, null, null));
			else if(cursor <=nb_args) {
				file.funs.getLast().fun_formals.add(new Decl_var(new Tint(), null)); 
			}
			else {
				System.out.println("la"); 
				Sblock b = stack_addr_block.pop(); 
				System.out.println("b = "+b); 
				b.dl.add(new Decl_var(new Tint(), null)); 
				stack_addr_block.push(b); 
			}
		}
		else if(stack_struct.peek()==STRUCT_FIELD) {
			System.out.println("yeah");
			//structure.getLast().fields.put(null, new Field(null, new Tint()));
			type_field = ENTIER;
		}
		System.out.println("Ptint Fin"); 
	//}
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
				System.out.println("la"); 
				Sblock b = stack_addr_block.pop(); 
				System.out.println("b = "+b); 
				b.dl.add(new Decl_var(new Tstructp( structure.get(n.id)), null)); 
				stack_addr_block.push(b); 
			}
		}
		else if(stack_struct.peek()==STRUCT_FIELD) {
			System.out.println("yeah");
			//structure.getLast().fields.put(null, new Field(null, new Tint()));
			type_field = ENTIER;
		}
		System.out.println("Fin PTstruct");
	}

	@Override
	public void visit(Pint n) {
		// TODO Auto-generated method stub
		System.out.println("Deb Pint"); 
		Expr tmp; 
		
		ident_block = stack_block.peek(); 
		try{
			ident_expr = stack_expr.peek(); 
		}
		catch(EmptyStackException e) {
			ident_expr = IDENT_ECONST; 
		}
		System.out.println("ident_expr : "+ident_expr); 
		System.out.println("ident_block : "+ident_block); 
		switch(ident_block) {
		
		case IDENT_SIF:
			switch(ident_expr) {
			case IDENT_ECONST: 
				//((Sif)((Sblock)file.funs.getLast().fun_body).sl.getLast()).e = new Econst(n.n); 
				//Sblock b = stack_addr_block.pop(); 
				tmp = stack_addr_expr.pop(); 
				tmp = new Econst(n.n); 
				stack_addr_expr.push(tmp); 
				//stack_addr_block.push(b); 
				
				break; 
			case IDENT_EUNOP:
			case IDENT_EBINOP: 
				//si première expression du binop
				System.out.println("addr size "+ stack_addr_expr.size());
				/*
				if(cursor_binop==0) {
					tmp = stack_addr_expr.pop();  
					System.out.println("before add e1 "+ tmp);
					((Ebinop)tmp).e1 = new Econst(n.n);
					System.out.println("after add e1 "+ tmp);
				
					tmp = stack_addr_expr.push(tmp); 
				}
				else if(cursor_binop==1) {
					tmp = stack_addr_expr.pop(); 
					System.out.println("before add e2 "+ tmp);
					((Ebinop)tmp).e2 = new Econst(n.n);
					System.out.println("after add e2 "+ tmp);
					tmp = stack_addr_expr.push(tmp); 
					//System.out.println("tmp2 "+ stack_addr_expr.peek());
				}
				else {
					System.out.println("Bad value of cursor_binop: "+cursor_binop + " "  + n.loc); 
				}*/
				
				tmp = stack_addr_expr.pop();  
				System.out.println("before add "+ tmp);
				tmp = new Econst(n.n);
				stack_addr_expr.push(tmp); 
				System.out.println("after add "+ tmp);
				break; 
				
			default:
				throw new Error("visit Pint ident_expr "+n.loc);
			}
			
			break; 
		case IDENT_SRETURN:
			switch(ident_expr) {
			case IDENT_ECONST: 
			case IDENT_EUNOP:
			case IDENT_EBINOP: 
				//si première expression du binop
				System.out.println("addr size "+ stack_addr_expr.size());
				/*
				if(cursor_binop==0) {
					tmp = stack_addr_expr.pop();  
					System.out.println("before add e1 "+ tmp);
					((Ebinop)tmp).e1 = new Econst(n.n);
					System.out.println("after add e1 "+ tmp);
				
					tmp = stack_addr_expr.push(tmp); 
				}
				else if(cursor_binop==1) {
					tmp = stack_addr_expr.pop(); 
					System.out.println("before add e2 "+ tmp);
					((Ebinop)tmp).e2 = new Econst(n.n);
					System.out.println("after add e2 "+ tmp);
					tmp = stack_addr_expr.push(tmp); 
					//System.out.println("tmp2 "+ stack_addr_expr.peek());
				}
				else {
					System.out.println("Bad value of cursor_binop: "+cursor_binop + " "  + n.loc); 
				}*/
				
				tmp = stack_addr_expr.pop();  
				System.out.println("before add "+ tmp);
				tmp = new Econst(n.n);
				stack_addr_expr.push(tmp); 
				System.out.println("after add "+ tmp);
				break; 
				
			default:
				throw new Error("visit Pint ident_expr "+n.loc);
			}
			
			break;
		
		case IDENT_PEVAL:
			System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
			switch(ident_expr) {
				case IDENT_PASSIGN:
					//System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
					tmp = stack_addr_expr.pop();  
					System.out.println("before add "+ tmp);
					((Eassign_local)tmp).e = new Econst(n.n);
					stack_addr_expr.push(tmp);
					System.out.println("after add "+ tmp);
					break;
				case IDENT_PCALL:
					System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
					/*
					tmp = stack_addr_expr.pop();  
					System.out.println("before add "+ tmp);
					tmp = new Econst(n.n);
					stack_addr_expr.push(tmp); 
					System.out.println("after add "+ tmp);
					
					*/
					
					tmp = stack_addr_expr.peek();
					if(tmp instanceof Ecall) {
						//((Ecall)tmp).el.add(new Econst(n.n));
						stack_addr_expr.push(new Econst(n.n));
						
					}
					else if(tmp instanceof Ebinop) {
						tmp = stack_addr_expr.pop();
						System.out.print("BBBBBBBBBBBBBBBBBBBBBBBB\n");
						if(cursor_binop==0)
							((Ebinop)tmp).e1 = new Econst(n.n);
						else
							((Ebinop)tmp).e2 = new Econst(n.n);
						stack_addr_expr.push(tmp);
						System.out.print("BBBBBBBBBBBBBBBBBBBBBBBB\n");
					}
					break;
				default:
					throw new Error("visit Pint ident_sblock "+n.loc);
			}
			
			break;
		default:
			//((Sreturn)((Sblock)file.funs.getLast().fun_body).sl.getLast()).e = new Econst(n.n); 
			throw new Error("visit Pint ident_sblock "+n.loc);
			
		}
		System.out.println("Fin Pint"); 	
	}

	@Override
	public void visit(Pident n) {
		// TODO Auto-generated method stub
		System.out.println("Pident Deb");
		Expr tmp;
		System.out.println("Yosh" + stack_struct.peek());
		if(stack_struct.peek()==0) {
			System.out.println(" stack size "+stack_block.size()); 
			System.out.println("cursor = "+ cursor); 
			
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
				file.funs.getLast().fun_formals.getLast().name = n.id;
			}
			else {
				//System.out.println("ident_block = "+ident_sblock); 
				
				Sblock b = stack_addr_block.pop(); 
				//b.dl.add(new Decl_var(new Tint(), null)); 
				stack_addr_block.push(b); 
				
				
				try{
					ident_block = stack_block.peek();  
				}
				catch(EmptyStackException e) {
					//ident_block = stack_block.peek();  
					System.out.println("catch");
				}
				
				try{
					ident_expr = stack_expr.peek(); 
				}
				catch(EmptyStackException e) {
					ident_expr = IDENT_EACCES_LOCAL; 
				}
				
				System.out.println(""+ident_block);
				
				switch(ident_block) {
					case IDENT_DECL:
						for(Decl_var var : b.dl) {
							if(var.name != null && var.name.equals(n.id)) {
								throw new Error("redefinition of variable at "+n.loc);
							}
						}
						b.dl.getLast().name = n.id;
						break; 
					case IDENT_SIF: 
						
						switch(ident_expr) {
						case IDENT_EACCES_LOCAL: 
							//((Sblock)file.funs.getLast().fun_body).sl.add(new Sreturn(new Eaccess_local(n.id))); 
							//((Sif)b.sl.getLast()).e = new Eaccess_local(n.id); 
							tmp = stack_addr_expr.pop();  
							tmp = new Eaccess_local(n.id);
							stack_addr_expr.push(tmp); 
							
							break; 
						case IDENT_EUNOP:
						case IDENT_EBINOP: 
							//si première expression du binop
							
							tmp = stack_addr_expr.pop();  
							System.out.println("before add "+ tmp);
							tmp = new Eaccess_local(n.id);
							stack_addr_expr.push(tmp); 
							System.out.println("after add "+ tmp);
							break; 
						
						default:
							throw new Error("visit Pident "+n.loc);
						}
						
						break;
						
					case IDENT_SRETURN:
						switch(ident_expr) {
							case IDENT_EACCES_LOCAL: 
							case IDENT_EUNOP:
							case IDENT_EBINOP: 
								//si première expression du binop
								
								tmp = stack_addr_expr.pop();  
								System.out.println("before add "+ tmp);
								tmp = new Eaccess_local(n.id);
								stack_addr_expr.push(tmp); 
								System.out.println("after add "+ tmp);
								break; 
							
							default:
								throw new Error("visit Pident "+n.loc);
							}
						
						break; 
					case IDENT_PEVAL:
						switch(ident_expr) {
							case IDENT_PASSIGN: 
								//si première expression du binop
								tmp = stack_addr_expr.pop();  
								((Eassign_local)tmp).i = n.id;
								stack_addr_expr.push(tmp);
								break;
							case IDENT_PCALL:
								Sblock temp = stack_addr_block.peek();
								int present = 0;
								if(temp != null) {
									for(Decl_var var : temp.dl) {
										if(var.name.equals(n.id)) {
											present = 1;
											break;
										}
									}
								}
								if(present==0)
									throw new Error("unknown variable at " + n.loc);
								
								tmp = stack_addr_expr.peek();
								if(tmp instanceof Ecall) {
									//((Ecall)tmp).el.add(new Econst(n.n));
									stack_addr_expr.add(new Eaccess_local(n.id));
								}
								else if(tmp instanceof Ebinop) {
									tmp = stack_addr_expr.pop();
									System.out.print("BBBBBBBBBBBBBBBBBBBBBBBB\n");
									if(cursor_binop==0)
										((Ebinop)tmp).e1 = new Eaccess_local(n.id);
									else
										((Ebinop)tmp).e2 = new Eaccess_local(n.id);
									stack_addr_expr.push(tmp);
									System.out.print("BBBBBBBBBBBBBBBBBBBBBBBB\n");
								}
								break;
							default:
								throw new Error("visit Pident "+n.loc);
						}
						break;
					default:
						throw new Error("visit Pident "+n.loc);
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
			if(type_field == ENTIER)
				structure.get(name_struct).fields.put(n.id, new Field(n.id, new Tint()));
			System.out.println("Ola, ici ici ici");
		}
		else if(stack_struct.peek()==STRUCT_VARNAME) {
			Sblock b = stack_addr_block.pop(); 
			System.out.println("b = "+b);
			b.dl.getLast().name = n.id;
			stack_addr_block.push(b);
		}
		System.out.println("Pident Fin"); 

	}

	@Override
	public void visit(Punop n) {
		// TODO Auto-generated method stub
	//unop = 1; 
	//int prev = ident_expr; 
	System.out.println("Eunop Deb"); 
	stack_expr.push(IDENT_EUNOP); 
	ident_block = stack_block.peek();
	Expr e = stack_addr_expr.pop(); 
	switch(ident_block)	{
	case IDENT_SRETURN: 
		 e = new Eunop(n.op, null); 
		 stack_addr_expr.push(e); 
		 stack_addr_expr.push(((Eunop)e).e); 
		 this.visit(n.e1);
		 ((Eunop)e).e = stack_addr_expr.pop(); 
		 
		break;
	default:
		throw new Error("visit Punop "+n.loc);
	}
	
	//stack_expr.pop(); 
	System.out.println("Eunop Fin"); 
	}

	@Override
	public void visit(Passign n) {
		// TODO Auto-generated method stub
		System.out.println("Passign Deb"); 
		stack_expr.push(IDENT_PASSIGN); 
		ident_block = stack_block.peek();
		System.out.println(ident_block);
		Expr e = stack_addr_expr.pop();
		System.out.println(e);
		System.out.println("AAAAAAAAAAAAAAAAAA");
		if(e==null) {
			e = new Eassign_local(null, null);
			stack_addr_expr.push(e);
			this.visit(n.e1);
			this.visit(n.e2);
		}
		else {
			stack_addr_expr.push(e);
			stack_addr_expr.push(new Eassign_local(null, null));
			this.visit(n.e1);
			this.visit(n.e2);
			Expr t = stack_addr_expr.pop();
			e = stack_addr_expr.pop();
			((Eassign_local)e).e = t;
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
		Expr e = stack_addr_expr.pop(); 
		cursor_binop = 0;
		switch(ident_block)	{
		case IDENT_SRETURN: 
			if(e instanceof Ecall) {
				stack_addr_expr.push(e);
				System.out.println("YYYYYYYYYYYYYYYYYYYYYY\n\n"+e+"\n");
				 e = new Ebinop(n.op, null, null); 
				 stack_addr_expr.push(e); 
				 System.out.println("YYYYYYYYYYYYYYYYYYYYYY\n\n"+stack_addr_expr.peek()+"\n");
				 //System.out.println("e = "+e);
				 //System.out.println(stack_addr_expr.size()); 
				 stack_addr_expr.push(((Ebinop)e).e1); 
				 this.visit(n.e1);
				 
				 ((Ebinop)e).e1 = stack_addr_expr.pop(); 
				 cursor_binop = 1; 
				 stack_addr_expr.push(((Ebinop)e).e2); 
				 this.visit(n.e2);
				 ((Ebinop)e).e2 = stack_addr_expr.pop(); 
			}
			else {
				System.out.println("YYYYYYYYYYYYYYYYYYYYYY\n\n"+e+"\n");
				e = new Ebinop(n.op, null, null); 
				stack_addr_expr.push(e); 
				System.out.println("YYYYYYYYYYYYYYYYYYYYYY\n\n"+stack_addr_expr.peek()+"\n");
				 //System.out.println("e = "+e);
				 //System.out.println(stack_addr_expr.size()); 
				stack_addr_expr.push(((Ebinop)e).e1); 
				this.visit(n.e1);
				 
				((Ebinop)e).e1 = stack_addr_expr.pop(); 
				cursor_binop = 1; 
				stack_addr_expr.push(((Ebinop)e).e2); 
				this.visit(n.e2);
				((Ebinop)e).e2 = stack_addr_expr.pop(); 
				stack_addr_expr.push(e);
				System.out.println("AAAAAAAAAAAAAAAAAAAAAAA\n\n"+e+"\n");
			}
			 
			  
			break;
		case IDENT_PEVAL:
			stack_addr_expr.push(e);
			stack_expr.pop();
			switch(stack_expr.peek()) {
				case IDENT_PCALL:
					System.out.println("eeeeeeeeeeeeeeeeeee"+stack_addr_expr.peek());
					Expr tmp = stack_addr_expr.peek();
					if(tmp instanceof Ecall) {
						cursor_binop = 0;
						e = new Ebinop(n.op, null, null);
						stack_addr_expr.push(e); 					
						this.visit(n.e1);
						cursor_binop = 1; 
						this.visit(n.e2);
					}
					else if(tmp instanceof Ebinop) {
						
						e = new Ebinop(n.op, null, null);
						stack_addr_expr.push(e);				
						this.visit(n.e1);
						cursor_binop = 1; 
						this.visit(n.e2);
						
						System.out.print("BBBBBBBBBBBBBBBBBBBBBBBB\n");
						e = stack_addr_expr.pop();
						tmp = stack_addr_expr.pop();
						if(cursor_binop==0) {
							
							((Ebinop)tmp).e1 = e;
						}
						else
							((Ebinop)tmp).e2 = e;
						stack_addr_expr.push(tmp);
						System.out.print("BBBBBBBBBBBBBBBBBBBBBBBB\n");
					}
					break;
				default:
					
					throw new Error("visit Pident "+n.loc);
			}
			stack_expr.push(IDENT_EBINOP);
			break;
		
		default:
			throw new Error("visit Pbinop "+n.loc);
		}
		
		
		//stack_addr_expr.pop(); 
		System.out.println("Binop Fin"); 
		
	}

	@Override
	public void visit(Parrow n) {
		// TODO Auto-generated method stub

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
		if(cmp==file.funs.size() && n.f.equals("putchar")==false)
			throw new Error("function " + n.f + " not found");
		int taille = (cmp==file.funs.size()) ? 1 : n.l.size();
		if(cmp < file.funs.size() && file.funs.get(cmp).fun_formals.size() != taille)
			throw new Error("bad arguments number");
		else if(cmp == file.funs.size() && n.l.size()!=1)
			throw new Error("bad arguments number");
		else {
			Expr e;
			
			ident_block = stack_block.peek(); 
			try{
				ident_expr = stack_expr.peek();
			}
			catch(EmptyStackException error) {
				stack_expr.push(IDENT_PCALL);
				ident_expr = IDENT_PCALL;
			}
			System.out.println("ident_expr : "+ident_expr); 
			System.out.println("ident_block : "+ident_block);
			System.out.println("Here " + stack_addr_expr.peek()+"\n\n\n\n");
			if(ident_block==IDENT_PEVAL)
				System.out.println("JOTAROOOO\n");
			switch(ident_block) {
				case IDENT_SRETURN:
					switch(ident_expr) {
						case IDENT_EBINOP:
							e = stack_addr_expr.pop();
							System.out.println("before add "+ e);
							e = new Ecall(n.f, new LinkedList<>());
							stack_addr_expr.push(e);
							
							for(int i=0; i<taille; i++) {
								Pexpr vars = n.l.get(i);
								System.out.println("vars");
								
								if(vars instanceof Pbinop || vars instanceof Punop) {
									if(vars instanceof Pbinop) stack_expr.push(IDENT_EBINOP);
									else stack_expr.push(IDENT_EUNOP);
									stack_block.push(IDENT_SRETURN);
									this.visit(vars);
									Expr ex = stack_addr_expr.pop();
									((Ecall)e).el.add(ex);
									stack_addr_expr.push(e);
									stack_block.pop();
									stack_expr.pop();
		
								}
								
								else {
								stack_expr.add(IDENT_PCALL);
								stack_block.add(IDENT_PEVAL);
									this.visit(vars);
									e = stack_addr_expr.pop();
									Expr t = stack_addr_expr.pop();
									((Ecall)t).el.add(e);
									stack_addr_expr.push(t);
									stack_block.pop();
									stack_expr.pop();
								}
							}
							System.out.println("after add "+ e);
							break;
						default:
							throw new Error("visit Pcall ident_expr "+n.loc);
						}
					break;
				case IDENT_PEVAL:
					
					e = stack_addr_expr.pop();
					if(e instanceof Ecall) {
						
						stack_addr_expr.push(e);
						Ecall ne = new Ecall(n.f, new LinkedList<>());
						stack_addr_expr.push(ne);
						
						
						for(int i=0; i<taille; i++) {
							Pexpr vars = n.l.get(i);
							System.out.println("vars");							
							if(vars instanceof Pbinop || vars instanceof Punop) {
								if(vars instanceof Pbinop) stack_expr.push(IDENT_EBINOP);
								else stack_expr.push(IDENT_EUNOP);
								stack_block.push(IDENT_SRETURN);
								//System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.peek());
								this.visit(vars);
								
								e = stack_addr_expr.pop();
								Expr t = stack_addr_expr.pop();
								((Ecall)t).el.add(e);
								stack_addr_expr.push(t);
								stack_block.pop();
								stack_expr.pop();
								
							}
							else {
								stack_expr.add(IDENT_PCALL);
								stack_block.add(IDENT_PEVAL);
								this.visit(vars);
								e = stack_addr_expr.pop();
								Expr t = stack_addr_expr.pop();
								((Ecall)t).el.add(e);
								stack_addr_expr.push(t);
								stack_block.pop();
								stack_expr.pop();
								
							}
						}
						
//						System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.pop());
//						System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.pop());
						/*
						e = stack_addr_expr.pop();
						Expr t = stack_addr_expr.pop();
						((Ecall)t).el.add(e);
						stack_addr_expr.push(t);
						System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.peek());

						System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
						*/
					}
					else {
				
						e = new Ecall(n.f, new LinkedList<>());
						stack_addr_expr.push(e);
						for(int i=0; i<taille; i++) {
							Pexpr vars = n.l.get(i);
							System.out.println("vars");
							if(vars instanceof Pbinop || vars instanceof Punop) {
								if(vars instanceof Pbinop) stack_expr.push(IDENT_EBINOP);
								else stack_expr.push(IDENT_EUNOP);
								stack_block.push(IDENT_SRETURN);
								this.visit(vars);
								Expr ex = stack_addr_expr.pop();
								((Ecall)e).el.add(ex);
								stack_addr_expr.push(e);
								stack_block.pop();
								stack_expr.pop();
	
							}
							else {
								stack_expr.add(IDENT_PCALL);
								this.visit(vars);
								e = stack_addr_expr.pop();
								Expr t = stack_addr_expr.pop();
								((Ecall)t).el.add(e);
								stack_addr_expr.push(t);
	
							}
							System.out.println(stack_addr_expr.size());
	 /*						
							System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.pop());
							System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.pop());
							System.out.println("NOPEEEEEEEEEEEEEEE " + stack_addr_expr.pop());
	*/						
		
							/* Now verifier que les variables sont du bon type */
						}
					}
					
					break;
				default:
					throw new Error("Not yet...");
			}
			System.out.println("Pcall Fin");
		}
	}

	@Override
	public void visit(Psizeof n) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Pskip n) {
		// TODO Auto-generated method stub

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
		System.out.println("Peval Fin");
	}

	@Override
	public void visit(Pif n) {
		// TODO Auto-generated method stub
		System.out.println("Pif Deb");
		

		//int prev = ident_expr; 
		stack_block.push(IDENT_SIF);
		//System.out.println("oui "+ stack_block.peek());
		Pexpr tmp = n.e; 
		//((Sblock)file.funs.getLast().fun_body).sl.add(new Sif(new Econst(2), null, null)); 
		
		Sblock b = stack_addr_block.pop(); 
		b.sl.add(new Sif(null, null, null)); 
		
		// A revoir
		//int prev = ident_expr; 
		stack_addr_expr.push(null); 
		stack_addr_block.push(b); 
		this.visit(tmp);
		((Sif)b.sl.getLast()).e = stack_addr_expr.pop() ; 
		stack_addr_block.push(b); 
		//System.out.println(((Sif)b.sl.getLast()).e); 
		//stack_addr_block.push(n.s1);
		System.out.println("Pif S1");
		Sblock bb = new Sblock(new LinkedList<>(), new LinkedList<>()); 
		//System.out.println("bb = "+bb); 
		//stack_addr_block.push(bb); 
		
		
		//bb = stack_addr_block.pop(); 
		System.out.println("bb = "+bb); 
		System.out.println("b = "+b); 
		//stack_addr_block.push(bb);
		stack_addr_stmt.push(null); 
		System.out.println("nooooooooon... "+ stack_addr_stmt.size()); 
		this.visit(n.s1);
		System.out.println("ouiiiiiiii... "+ stack_addr_stmt.size()); 
		//stack_addr_block.pop();
		
		((Sif)b.sl.getLast()).s1 = stack_addr_stmt.pop();
		System.out.println("llllllll ");
		
		//System.out.println("llllllll " + stack_addr_stmt.pop());
		/*
		bb = new Sblock(new LinkedList<>(), new LinkedList<>()); 
		stack_addr_block.push(bb); 
		this.visit(n.s2); 
		((Sif)b.sl.getLast()).s2 = stack_addr_block.pop();
		*/
		stack_addr_stmt.push(null);
		System.out.println("nooooooooon... "+ stack_addr_stmt.size()); 
		this.visit(n.s2);
		System.out.println("ouiiiiiiii... "+ stack_addr_stmt.size()); 
		//stack_addr_block.pop();
		
		((Sif)b.sl.getLast()).s2 = stack_addr_stmt.pop();
		System.out.println("llllllll ");
		
		
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
		
		
		
		//((Sblock)file.funs.getLast().fun_body).dl = new LinkedList<>();
		//same for sl
		//file.funs.
		//stack_addr_block.push(null)
		System.out.println("Pbloc deb"); 
		stack_block.push(IDENT_DECL); 
		
		//
		//b.sl.add(new Sblock(new LinkedList<Decl_var>(), new LinkedList<Stmt>())); 
		Sblock b =  new Sblock(new LinkedList<Decl_var>(), new LinkedList<Stmt>()); 
		stack_addr_block.push(b);
		
		for(Pdeclvar tmp: n.vl) {
			//System.out.println("hereee"); 
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
		System.out.println("Pbloc fin"); 
		
		//Sblock b = stack_addr_block.pop(); 
		/*
		System.out.println("bbbbb"); 
		System.out.println(b.sl.getLast());
		*/
		//b.sl.add(bb);
		Stmt s = stack_addr_stmt.pop(); 
		s = stack_addr_block.pop();
		stack_addr_stmt.push(s); 
	
	}

	@Override
	public void visit(Preturn n) {
		// TODO Auto-generated method stub
		System.out.println("Preturn deb"); 
		//int prev = ident_expr; 
		stack_block.push(IDENT_SRETURN);
		//System.out.println("oui "+ stack_block.peek());
		Pexpr tmp = n.e; 
		
		//
		
		
		System.out.println("size: "+  stack_addr_block.size());
		Sblock b = stack_addr_block.pop(); 
		//b.sl.add(new Sreturn(null)); 
		stack_addr_block.push(b); 
		// A revoir
		Expr e = null; 
		stack_addr_expr.push(e);
		if(tmp instanceof Pbinop)
			System.out.println("YBINOPEEEEEEEEEEEEEEEES\n\n\n");
		this.visit(tmp);
		
		b.sl.add(new Sreturn(stack_addr_expr.pop())) ; 
		stack_addr_block.push(b);
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
		else if (tmp instanceof Pident)
			this.visit((Pident)tmp);
		else if (tmp instanceof Parrow)
			this.visit((Parrow)tmp);
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
