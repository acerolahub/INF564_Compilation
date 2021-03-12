package mini_c;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

class ToLTL extends EmptyERTLERTLVisitor {
	private LTLfile ltlfile;
	public boolean debug = false;
	private Coloring coloring; // coloriage de la fonction en cours de traduction
	int size_locals; // taille pour les variables locales
	LTLgraph graph; // graphe en cours de construction fun;
	LTLgraph current_graph;
	Interference inter;
	Coloring color;
	Stack<Label> stack_label = new Stack<>();

	LTLfile translate(ERTLfile ertl) {
		this.visit(ertl);
		return ltlfile;
	}

	public void visit(ERTLfile ertl) {
		if (debug) System.out.println("ERTLfile Deb");
		ltlfile = new LTLfile();
		for (ERTLfun d : ertl.funs) {
			Liveness LL = new Liveness(d.body);  
    		inter = new Interference(LL);
    		color = new Coloring(inter);
			this.visit(d);
		}
		if (debug) System.out.println("ERTLfile Fin");
	}

	public void visit(ERTLfun d) {
		if (debug) System.out.println("ERTLfun Deb" + d.name);
		LTLfun tmp = new LTLfun(d.name);
		size_locals = d.locals.size();
		tmp.entry = d.entry;
		tmp.body = new LTLgraph();
		current_graph = tmp.body;
		for(Label L : d.body.graph.keySet()) {
			stack_label.push(L);
			ERTL ertl = d.body.graph.get(L);
			this.visit(ertl);
		}
		ltlfile.funs.add(tmp);
		if (debug) System.out.println("ERTLfun Fin" + d.name);
	}
	
	public void visit(ERTL ertl) {
		if (ertl instanceof ERconst)
			this.visit((ERconst) ertl);
		else if (ertl instanceof ERload)
			this.visit((ERload) ertl);
		else if (ertl instanceof ERstore)
			this.visit((ERstore) ertl);
		else if (ertl instanceof ERmunop)
			this.visit((ERmunop) ertl);
		else if (ertl instanceof ERmbinop)
			this.visit((ERmbinop) ertl);
		else if (ertl instanceof ERmubranch)
			this.visit((ERmubranch) ertl);
		else if (ertl instanceof ERmbbranch)
			this.visit((ERmbbranch) ertl);
		else if (ertl instanceof ERgoto)
			this.visit((ERgoto) ertl);
		else if (ertl instanceof ERcall)
			this.visit((ERcall) ertl);
		else if (ertl instanceof ERalloc_frame)
			this.visit((ERalloc_frame)ertl);
	}
	
	public void visit(ERconst ertl) {
		if (debug) System.out.println("ERconst Deb");
		Label L = stack_label.pop();
		current_graph.put(L, new Lconst(ertl.i, color.colors.get(ertl.r), ertl.l));
		if (debug) System.out.println("ERconst Fin");
	}
	
	public void visit(ERload ertl) {
		if (debug) System.out.println("ERload Deb");
		Label L = stack_label.pop();
		if(color.colors.get(ertl.r1) instanceof Reg && color.colors.get(ertl.r2) instanceof Reg)
			current_graph.put(L, new Lload(((Reg)color.colors.get(ertl.r1)).r, ertl.i, ((Reg)color.colors.get(ertl.r2)).r, ertl.l));
		else if(color.colors.get(ertl.r1) instanceof Reg && color.colors.get(ertl.r2) instanceof Spilled) {
			Label temp = new Label();
			Reg reg = new Reg(Register.tmp2);
			current_graph.put(L, new Lload(((Reg)color.colors.get(ertl.r1)).r, ertl.i, reg.r, temp));
			current_graph.put(temp, new Lmbinop(Mbinop.Mmov, reg, color.colors.get(ertl.r2), ertl.l));
		}
		else if(color.colors.get(ertl.r1) instanceof Spilled && color.colors.get(ertl.r2) instanceof Reg) {
			Label temp = new Label();
			Reg reg = new Reg(Register.tmp2);
			current_graph.put(L, new Lmbinop(Mbinop.Mmov, color.colors.get(ertl.r1), reg, temp));
			current_graph.put(temp, new Lload(reg.r, ertl.i, ((Reg)color.colors.get(ertl.r2)).r, ertl.l));
		}
		else {
			Label temp = new Label(), temp2 = new Label();
			Reg reg = new Reg(Register.tmp2);
			Reg reg1 = new Reg(Register.tmp1);
			current_graph.put(L, new Lmbinop(Mbinop.Mmov, color.colors.get(ertl.r1), reg, temp));
			current_graph.put(temp, new Lload(reg.r, ertl.i, reg1.r, temp2));
			current_graph.put(temp2, new Lmbinop(Mbinop.Mmov, reg1, color.colors.get(ertl.r2), ertl.l));
		}
		if (debug) System.out.println("ERload Fin");
	}
	
	public void visit(ERstore ertl) {
		if (debug) System.out.println("ERstore Deb");
		Label L = stack_label.pop();
		current_graph.put(L, new Lstore(((Reg)color.colors.get(ertl.r1)).r, ((Reg)color.colors.get(ertl.r2)).r, ertl.i, ertl.l));
		if (debug) System.out.println("ERstore Fin");
	}
	
	public void visit(ERmunop ertl) {
		if (debug) System.out.println("ERmunop Deb");
		Label L = stack_label.pop();
		current_graph.put(L, new Lmunop(ertl.m, color.colors.get(ertl.r), ertl.l));
		if (debug) System.out.println("ERmunop Fin");
	}
	
	public void visit(ERmbinop ertl) {
		if (debug) System.out.println("ERmbinop Deb");
		Label L = stack_label.pop();
		Operand r1 = color.colors.get(ertl.r1);
		Operand r2 = color.colors.get(ertl.r2);
		/*
		System.out.println(r1);
		System.out.println(ertl.r1);
		System.out.println(r2);
		System.out.println(ertl.r2);
		*/
		if(ertl.m == Mbinop.Mmov && r1 == r2)
			current_graph.put(L, new Lgoto(ertl.l));
		else if(ertl.m == Mbinop.Mmul && r2 instanceof Spilled) {
			Reg reg = new Reg(Register.tmp2);
			Label temp = new Label(), temp2 = new Label();
			current_graph.put(L, new Lmbinop(Mbinop.Mmov, r2, reg, temp));
			current_graph.put(temp, new Lmbinop(ertl.m, r1, reg, temp2));
			current_graph.put(temp, new Lmbinop(Mbinop.Mmov, reg, r2, ertl.l));
		}
		else if(r1 instanceof Spilled && r2 instanceof Spilled) {
			Reg reg = new Reg(Register.tmp2);
			Label temp = new Label(), temp2 = new Label();
			current_graph.put(L, new Lmbinop(Mbinop.Mmov, r2, reg, temp));
			current_graph.put(temp, new Lmbinop(ertl.m, r1, reg, temp2));
			current_graph.put(temp, new Lmbinop(Mbinop.Mmov, reg, r2, ertl.l));
		}
		else
			current_graph.put(L, new Lmbinop(ertl.m, r1, r2, ertl.l));
		if (debug) System.out.println("ERmbinop Fin");
	}
	
	public void visit(ERalloc_frame ertl) {
		if (debug) System.out.println("ERalloc_frame Deb");
		Label L = stack_label.pop();
		current_graph.put(L, new Lpush(new Spilled((size_locals-6)*Memory.word_size), ertl.l));
		if (debug) System.out.println("ERalloc_frame Fin");
	}

}
