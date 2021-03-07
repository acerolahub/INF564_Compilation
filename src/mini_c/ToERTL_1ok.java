package mini_c;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

class ToERTL extends EmptyRTLVisitor{
	
	private ERTLfile ertlfile;
	public boolean debug = false;
	Label last_result, label_sortie;
	ERTLgraph current_graph;
	Stack<Label> stack_labelrtl = new Stack<>();;
	
	
	ERTLfun fun;
	
	ERTLfile translate(RTLfile rtl){
		 this.visit(rtl);
		 return ertlfile; 
	}
	
	public void visit(RTLfile rtl) {
		 
		 if (debug) System.out.println("RTL Deb"); 
		 ertlfile = new ERTLfile(); 
		 for (RTLfun d: rtl.funs) {
			 this.visit(d);
		 }
		 if (debug) System.out.println("RTL fin"); 	
	}
	
	 public void visit(RTLfun d) {
		 if (debug) System.out.println("RTLfun Deb" + d.name); 
		 
		 ERTLfun tmp = new ERTLfun(d.name, d.formals.size()); 
		 fun = tmp; 
		 for(Register r: d.locals) {
			 tmp.locals.add(r);
		 }
		 tmp.body = new ERTLgraph(); 
		 current_graph = tmp.body; 
		 label_sortie = d.exit;
		 this.visit(d.body);
		 tmp.entry = last_result; 
		 tmp.body = current_graph; 
		 ertlfile.funs.add(tmp); 
		 if (debug) System.out.println("RTLfun Fin"); 
	 }
	 
	 public void visit(RTLgraph body) {
		 if (debug) System.out.println("RTLgraph Deb"); 
		 stack_labelrtl.push(null);
		 for(Label L : body.graph.keySet()) {
			 RTL rtl = body.graph.get(L);
			 stack_labelrtl.push(L);
			 this.visit(rtl);
		 }
		 current_graph.put(label_sortie, new ERreturn());
		 if (debug) System.out.println("RTLgraph Fin");
	 }

	 public void visit(RTL rtl) {
		 if(rtl instanceof Rconst) this.visit((Rconst)rtl);
		 else if(rtl instanceof Rload) this.visit((Rload)rtl);
		 else if(rtl instanceof Rstore) this.visit((Rstore)rtl);
		 else if(rtl instanceof Rmunop) this.visit((Rmunop)rtl);
		 else if(rtl instanceof Rmbinop) this.visit((Rmbinop)rtl);
		 else if(rtl instanceof Rmubranch) this.visit((Rmubranch)rtl);
		 else if(rtl instanceof Rmbbranch) this.visit((Rmbbranch)rtl);
		 else if(rtl instanceof Rgoto) this.visit((Rgoto)rtl);
	 }
	 
	 public void visit(Rconst rtl) {
		 if (debug) System.out.println("Rconst Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERconst(rtl.i, rtl.r, rtl.l));
		 last_result = L;
		 if (debug) System.out.println("Rconst Fin");
	 }
	 
	 public void visit(Rload rtl) {
		 if (debug) System.out.println("Rload Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERload(rtl.r1, rtl.i, rtl.r2, rtl.l));
		 last_result = L;
		 if (debug) System.out.println("Rload Fin");
	 }
	 
	 public void visit(Rstore rtl) {
		 if (debug) System.out.println("Rstore Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERstore(rtl.r1, rtl.r2, rtl.i, rtl.l));
		 last_result = L;
		 if (debug) System.out.println("Rstore Fin");
	 }
	 
	 public void visit(Rmunop rtl) {
		 if (debug) System.out.println("Rmunop Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERmunop(rtl.m, rtl.r, rtl.l));
		 last_result = L;
		 if (debug) System.out.println("Rmunop Fin");
	 }
	 
	 public void visit(Rmubranch rtl) {
		 if (debug) System.out.println("Rmubranch Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERmubranch(rtl.m, rtl.r, rtl.l1, rtl.l2));
		 last_result = L;
		 if (debug) System.out.println("Rmubranch Fin");
	 }
	 
	 public void visit(Rmbbranch rtl) {
		 if (debug) System.out.println("Rmbbranch Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERmbbranch(rtl.m, rtl.r1, rtl.r2, rtl.l1, rtl.l2));
		 last_result = L;
		 if (debug) System.out.println("Rmbbranch Fin");
	 }
	 
	 public void visit(Rgoto rtl) {
		 if (debug) System.out.println("Rgoto Deb");
		 Label L = stack_labelrtl.pop();
		 current_graph.put(L, new ERgoto(rtl.l));
		 last_result = L;
		 if (debug) System.out.println("Rgoto Fin");
	 }
	 
	 public void visit(Rmbinop rtl) {
		 if (debug) System.out.println("Rmbinop Deb");
		 Label L = stack_labelrtl.pop();
		 if(rtl.m == Mbinop.Mdiv) {
			 Label L2 = new Label();
			 Label L3 = new Label();
			 current_graph.put(L, new ERmbinop(Mbinop.Mmov, rtl.r2, Register.rax, L2));
			 current_graph.put(L2, new ERmbinop(rtl.m, rtl.r1, Register.rax, L3));
			 current_graph.put(L3, new ERmbinop(Mbinop.Mmov, Register.rax, rtl.r2, rtl.l));
		 }
		 else
			 current_graph.put(L, new ERmbinop(rtl.m, rtl.r1, rtl.r2, rtl.l));
		 last_result = L;
		 if (debug) System.out.println("Rmbinop Fin");
	 }
}
