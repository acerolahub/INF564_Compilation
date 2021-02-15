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
	final private static int IDENT_DECL= 0, IDENT_PBLOC = 1, IDENT_SRETURN = 2; 
	private  int ident_block = 0; 
	//Expr 
	final private static int IDENT_ECONST=0, IDENT_EACCES_LOCAL=1, IDENT_EACCES_FIELS=2, IDENT_EASSIGN_LOCAL = 3, 
			IDENT_EASSIGN_FIELD = 4, IDENT_EUNOP = 5, IDENT_EBINOP = 6, IDENT_ECALL = 7, IDENT_ESIZEOF = 8; 
	
	private  int ident_expr = 0; 
	private int cursor_binop=0; 
	
	public Stack<Integer> stack_expr = new Stack<>(); 
	public Stack<Integer> stack_block = new Stack<>(); 
	public Stack<Expr> stack_addr_expr = new Stack<>(); 
	
	
	
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
	}

	@Override
	public void visit(PTint n) {
		// TODO Auto-generated method stub
		
		//System.out.println("la"); 
		if(cursor==0)
			file.funs.add(new Decl_fun(new Tint(), null, null, null));
		else if(cursor <=nb_args) {
			file.funs.getLast().fun_formals.add(new Decl_var(new Tint(), null)); 
		}
		else {
			System.out.println("la"); 
			((Sblock)file.funs.getLast().fun_body).dl.add(new Decl_var(new Tint(), null)); 
		}
		
	}

	@Override
	public void visit(PTstruct n) {
		// TODO Auto-generated method stub
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
		case IDENT_SRETURN:
			switch(ident_expr) {
			case IDENT_ECONST: 
				//((Sblock)file.funs.getLast().fun_body).sl.add(new Sreturn(new Econst(n.n))) ;
				((Sreturn)((Sblock)file.funs.getLast().fun_body).sl.getLast()).e = new Econst(n.n); 
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
		System.out.println(" stack size "+stack_block.size()); 
		System.out.println("cursor = "+ cursor); 
		
		if(cursor==0)
			file.funs.getLast().fun_name = n.id; 
		else if(cursor <=nb_args) {
			file.funs.getLast().fun_formals.getLast().name = n.id; 
		}
		else {
			//System.out.println("ident_block = "+ident_sblock); 
			
			
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
				((Sblock)file.funs.getLast().fun_body).dl.getLast().name = n.id; 
				break; 
			case IDENT_SRETURN:
				switch(ident_expr) {
				case IDENT_EACCES_LOCAL: 
					//((Sblock)file.funs.getLast().fun_body).sl.add(new Sreturn(new Eaccess_local(n.id))); 
					((Sreturn)((Sblock)file.funs.getLast().fun_body).sl.getLast()).e = new Eaccess_local(n.id); 
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
					throw new Error("visit Pint "+n.loc);
				}
				
				break; 
			default:
				throw new Error("visit Pident "+n.loc);
			}
			
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
		((Sreturn)((Sblock)file.funs.getLast().fun_body).sl.getLast()).e = new Eunop(n.op, null); 
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
		
		

	}

	@Override
	public void visit(Pbinop n) {
		// TODO Auto-generated method stub
		System.out.println("Binop Deb"); 
		stack_expr.push(IDENT_EBINOP); 
		ident_block = stack_block.peek();
		Expr e = stack_addr_expr.pop(); 
		cursor_binop = 0;
		switch(ident_block)	{
		case IDENT_SRETURN: 
			
			 e = new Ebinop(n.op, null, null); 

			 stack_addr_expr.push(e); 
	
			 //System.out.println("e = "+e);
			 //System.out.println(stack_addr_expr.size()); 
			 stack_addr_expr.push(((Ebinop)e).e1); 
			 this.visit(n.e1);
			 ((Ebinop)e).e1 = stack_addr_expr.pop(); 
			 cursor_binop = 1; 
			 stack_addr_expr.push(((Ebinop)e).e2); 
			 this.visit(n.e2);
			 ((Ebinop)e).e2 = stack_addr_expr.pop(); 
			  
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

	}

	@Override
	public void visit(Pif n) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Pwhile n) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(Pbloc n) {
		// TODO Auto-generated method stub
		file.funs.getLast().fun_body = new Sblock(new LinkedList<>(), new LinkedList<>()); 
		
		//((Sblock)file.funs.getLast().fun_body).dl = new LinkedList<>();
		//same for sl
		//file.funs.
		stack_block.push(IDENT_DECL); 
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
		
	}

	@Override
	public void visit(Preturn n) {
		// TODO Auto-generated method stub
		System.out.println("Preturn deb"); 
		//int prev = ident_expr; 
		stack_block.push(IDENT_SRETURN);
		System.out.println("oui "+ stack_block.peek());
		Pexpr tmp = n.e; 
		((Sblock)file.funs.getLast().fun_body).sl.add(new Sreturn(null)); 
		// A revoir
		stack_addr_expr.push(((Sreturn)((Sblock)file.funs.getLast().fun_body).sl.getLast()).e); 
		this.visit(tmp);
		((Sblock)file.funs.getLast().fun_body).sl.removeLast(); 
		((Sblock)file.funs.getLast().fun_body).sl.add(new Sreturn(stack_addr_expr.pop())); 
		//System.out.println(((Sblock)file.funs.getLast().fun_body).sl.removeLast() == null); 
		System.out.println("Preturn fin"); 

	}

	@Override
	public void visit(Pstruct n) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(Pfun n) {
		// TODO Auto-generated method stub
		
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
			}
			else if(tmp.typ instanceof PTstruct) {
				this.visit((PTstruct)tmp.typ );
			} 
			else {
				throw new Error("visit Pfun Pdeclvar "+tmp.loc); 
			}
			this.visit(new Pident(new Pstring(tmp.id, tmp.loc)));
			cursor++; 
		}
		
		this.visit(n.b);
		cursor = 0;
	}
	
	
	
	
	public void visit(Pexpr tmp) {
		// TODO Auto-generated method stub	
		
	
		if(tmp instanceof Pint) {
			//file.funs.getLast().fun_body = new Sreturn(new Econst(((Pint)tmp).n));
			this.visit((Pint)tmp);
		}
		else if (tmp instanceof Pident) {
			this.visit((Pident)tmp); 
		}
		else if (tmp instanceof Plvalue) {
			;
		}
		else if (tmp instanceof Parrow) {
			;} 
		 
		else if (tmp instanceof Parrow); 
		else if (tmp instanceof Passign); 
		else if (tmp instanceof Pbinop) {
			this.visit((Pbinop)tmp);
		}
		else if (tmp instanceof Punop) {
			this.visit((Punop)tmp);
		}
		else if (tmp instanceof Pcall); 
		else if (tmp instanceof Psizeof); 
		else; 
	}

	public void visit(Pstmt tmp) {
		// TODO Auto-generated method stub	
		if(tmp instanceof Pbloc) {
			this.visit((Pbloc)tmp);}
		else if(tmp instanceof Pskip) {
			this.visit((Pskip)tmp); }
		else if(tmp instanceof Preturn) {
			this.visit((Preturn)tmp);
		} 
		else if(tmp instanceof Pif) {
			this.visit((Pif)tmp); }
		else if(tmp instanceof Peval) {
			this.visit((Peval)tmp); }
		else if(tmp instanceof Pwhile) {
			this.visit((Pwhile)tmp); }
		else{
			throw new Error("Visit Pstmt "+tmp.loc);} 
	} 
		
	
}
