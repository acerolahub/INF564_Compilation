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

	LTLfile translate(ERTLfile ertl) {
		this.visit(ertl);
		return ltlfile;
	}

	public void visit(ERTLfile ertl) {
		if (debug)
			System.out.println("ERTLfile Deb");
		ltlfile = new LTLfile();
		for (ERTLfun d : ertl.funs) {
			this.visit(d);
		}
		if (debug)
			System.out.println("ERTLfile Fin");
	}

	public void visit(ERTLfun d) {
		if (debug)
			System.out.println("ERTLfun Deb" + d.name);
		LTLfun tmp = new LTLfun(d.name);

		tmp.body = new LTLgraph();
		current_graph = tmp.body;

		ltlfile.funs.add(tmp);
		if (debug)
			System.out.println("ERTLfun Fin" + d.name);
	}

}
