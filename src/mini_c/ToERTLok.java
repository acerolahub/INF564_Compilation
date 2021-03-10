package mini_c;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

class ToERTL extends EmptyRTLVisitor {

	private ERTLfile ertlfile;
	public boolean debug = false;
	Label last_result, label_sortie;
	ERTLgraph current_graph;
	Stack<Label> stack_labelrtl = new Stack<>();;

	ERTLfun fun;

	ERTLfile translate(RTLfile rtl) {
		this.visit(rtl);
		return ertlfile;
	}

	public void visit(RTLfile rtl) {

		if (debug)
			System.out.println("RTL Deb");
		ertlfile = new ERTLfile();
		for (RTLfun d : rtl.funs) {
			this.visit(d);
		}
		if (debug)
			System.out.println("RTL fin");
	}

	public void visit(RTLfun d) {
		if (debug)
			System.out.println("RTLfun Deb" + d.name);

		ERTLfun tmp = new ERTLfun(d.name, d.formals.size());
		fun = tmp;
		for (Register r : d.locals) {
			tmp.locals.add(r);
		}
		tmp.body = new ERTLgraph();
		current_graph = tmp.body;
		
		Label tmp_e, tmp_s;
		tmp_e = new Label();
		tmp_s = new Label();
		tmp.entry = tmp_e;

		// Allocate frame

		current_graph.put(tmp_e, new ERalloc_frame(tmp_s));
		int i;
		List<Register> CALLEE_SAVED = new LinkedList<>();
		Register tmp_r;
		int n = d.formals.size();
		int n_cs = Register.callee_saved.size();
		for (i = 0; i < n_cs; i++) {
			tmp_r = new Register();
			CALLEE_SAVED.add(tmp_r);

			tmp_e = tmp_s;
			if (i == (n_cs - 1) && n == 0)
				tmp_s = d.entry;
			else
				tmp_s = new Label();
			current_graph.put(tmp_e, new ERmbinop(Mbinop.Mmov, Register.callee_saved.get(i), tmp_r, tmp_s));

		}

		// Save callee saved registers into temporary registers

			//System.out.println("gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg "+  n);
		int taille = (n > 6) ? 6 : n;

		for (i = 0; i < taille; i++) {
			tmp_e = tmp_s;
			if (i == n - 1)
				tmp_s = d.entry;
			else
				tmp_s = new Label();
			current_graph.put(tmp_e, new ERmbinop(Mbinop.Mmov, Register.parameters.get(i), d.formals.get(i), tmp_s));
		}
		for (i = 6; i < n; i++) {
			tmp_e = tmp_s;
			if (i == n - 1)
				tmp_s = d.entry;
			else
				tmp_s = new Label();
			current_graph.put(tmp_e, new ERget_param(((n -5) - (i - 6))*Memory.word_size, d.formals.get(i), tmp_s));
		}

		this.visit(d.body);

		tmp_e = d.exit;
		tmp_s = new Label();
		current_graph.put(tmp_e, new ERmbinop(Mbinop.Mmov, d.result, Register.result, tmp_s));

		for (i = 0; i < Register.callee_saved.size(); i++) {

			tmp_r = CALLEE_SAVED.get(i);
			tmp_e = tmp_s;
			tmp_s = new Label();
			current_graph.put(tmp_e, new ERmbinop(Mbinop.Mmov, tmp_r, Register.callee_saved.get(i), tmp_s));

		}
		tmp_e = tmp_s;
		tmp_s = new Label();
		current_graph.put(tmp_e, new ERdelete_frame(tmp_s));
		current_graph.put(tmp_s, new ERreturn());
	
		
		// point to body(last_result);
		ertlfile.funs.add(tmp);
		if (debug)
			System.out.println("RTLfun Fin");
	}

	public void visit(RTLgraph body) {
		if (debug)
			System.out.println("RTLgraph Deb");
		// stack_labelrtl.push(null);
		for (Label L : body.graph.keySet()) {
			RTL rtl = body.graph.get(L);
			stack_labelrtl.push(L);
			this.visit(rtl);
		}

		if (debug)
			System.out.println("RTLgraph Fin");
	}

	public void visit(RTL rtl) {
		if (rtl instanceof Rconst)
			this.visit((Rconst) rtl);
		else if (rtl instanceof Rload)
			this.visit((Rload) rtl);
		else if (rtl instanceof Rstore)
			this.visit((Rstore) rtl);
		else if (rtl instanceof Rmunop)
			this.visit((Rmunop) rtl);
		else if (rtl instanceof Rmbinop)
			this.visit((Rmbinop) rtl);
		else if (rtl instanceof Rmubranch)
			this.visit((Rmubranch) rtl);
		else if (rtl instanceof Rmbbranch)
			this.visit((Rmbbranch) rtl);
		else if (rtl instanceof Rgoto)
			this.visit((Rgoto) rtl);
		else if (rtl instanceof Rcall)
			this.visit((Rcall) rtl);
	}

	public void visit(Rconst rtl) {
		if (debug)
			System.out.println("Rconst Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERconst(rtl.i, rtl.r, rtl.l));
		last_result = rtl.l;
		if (debug)
			System.out.println("Rconst Fin");
	}

	public void visit(Rload rtl) {
		if (debug)
			System.out.println("Rload Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERload(rtl.r1, rtl.i, rtl.r2, rtl.l));
		last_result = rtl.l;
		if (debug)
			System.out.println("Rload Fin");
	}

	public void visit(Rstore rtl) {
		if (debug)
			System.out.println("Rstore Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERstore(rtl.r1, rtl.r2, rtl.i, rtl.l));
		last_result = rtl.l;
		if (debug)
			System.out.println("Rstore Fin");
	}

	public void visit(Rmunop rtl) {
		if (debug)
			System.out.println("Rmunop Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERmunop(rtl.m, rtl.r, rtl.l));
		last_result = rtl.l;
		if (debug)
			System.out.println("Rmunop Fin");
	}

	public void visit(Rmubranch rtl) {
		if (debug)
			System.out.println("Rmubranch Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERmubranch(rtl.m, rtl.r, rtl.l1, rtl.l2));

		if (debug)
			System.out.println("Rmubranch Fin");
	}

	public void visit(Rmbbranch rtl) {
		if (debug)
			System.out.println("Rmbbranch Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERmbbranch(rtl.m, rtl.r1, rtl.r2, rtl.l1, rtl.l2));

		if (debug)
			System.out.println("Rmbbranch Fin");
	}

	public void visit(Rgoto rtl) {
		if (debug)
			System.out.println("Rgoto Deb");
		Label L = stack_labelrtl.pop();
		current_graph.put(L, new ERgoto(rtl.l));

		if (debug)
			System.out.println("Rgoto Fin");
	}

	public void visit(Rmbinop rtl) {
		if (debug)
			System.out.println("Rmbinop Deb");
		Label L = stack_labelrtl.pop();
		if (rtl.m == Mbinop.Mdiv) {
			Label L2 = new Label();
			Label L3 = new Label();
			current_graph.put(L, new ERmbinop(Mbinop.Mmov, rtl.r2, Register.rax, L2));
			current_graph.put(L2, new ERmbinop(rtl.m, rtl.r1, Register.rax, L3));
			current_graph.put(L3, new ERmbinop(Mbinop.Mmov, Register.rax, rtl.r2, rtl.l));
		} else
			current_graph.put(L, new ERmbinop(rtl.m, rtl.r1, rtl.r2, rtl.l));

		if (debug)
			System.out.println("Rmbinop Fin");
	}

	public void visit(Rcall rtl) {
		if (debug)
			System.out.println("Rcall Deb");
		Label L = stack_labelrtl.pop();
		last_result = L;
		int n = rtl.rl.size();
		int taille = (n > 6) ? 6 : n;
		Label temp;
		for (int i = 0; i < taille; i++) {
			temp = new Label();
			current_graph.put(L, new ERmbinop(Mbinop.Mmov, rtl.rl.get(i), Register.parameters.get(i), temp));
			L = temp;
		}

		for (int i = 6; i < n; i++) {
			temp = new Label();
			current_graph.put(L, new ERpush_param(rtl.rl.get(i), temp));
			L = temp;
		}
		temp = new Label();
		current_graph.put(L, new ERcall(rtl.s, n, temp));
		L = temp;
		if(n<=6) temp = rtl.l; 
		else temp = new Label();
		
		current_graph.put(L, new ERmbinop(Mbinop.Mmov, Register.result, rtl.r, temp));
		// int taille_stack = (n>6) ? n-6 : 0;
		if (n > 6) {
			//System.out.print("dd");
			current_graph.put(temp, new ERmunop(new Maddi((n - 6) * Memory.word_size), Register.rsp, rtl.l));
		}

		if (debug)
			System.out.println("Rcall Fin");
	}
}
