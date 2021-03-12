package mini_c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Coloring {

  Map<Register, Operand> colors = new HashMap<>();
  int nlocals = 0; // nombre d'emplacements sur la pile
  HashMap<Register, LinkedList<Register>> todo = new HashMap<>();

  void print() {
    System.out.println("coloring output:");
    for (Register r : colors.keySet()) {
      Operand o = colors.get(r);
      System.out.println("  " + r + " --> " + o);
    }
  }

  int priorite(Arcs arc, Register r) {
    if (this.todo.get(r) != null && this.todo.get(r).size() == 1) {
      for (Register pref : arc.prefs) {
        if (todo.get(pref) != null && todo.get(pref).contains(todo.get(r).getFirst())) {
          return 1;
        }
      }
      return 2;
    }

    for (Register pref : arc.prefs) {
      if (colors.keySet().contains(pref)) {
        return 3;
      }
    }
    if (this.todo.get(r) != null && this.todo.get(r).size() >= 1) return 4;
    return 5;
  }

  Register choose(Set<Register> R, Map<Register, Arcs> graph) {
    Register r = null;
    int prior = 10;
    int tmp_p = 10;
    for (Register rr : R) {
      tmp_p = this.priorite(graph.get(rr), rr);
      if (prior > tmp_p) {
        prior = tmp_p;
        r = rr;
      }
    }
    return r;
  }

  Coloring(Interference ig) {
    for (Register r : ig.graph.keySet()) {
      if (Register.allocatable.contains(r)) continue;
      /*
			int a = 0; 
			for (Register ralloc : Register.allocatable) {
				if (ig.graph.get(r).intfs.contains(ralloc)) {
					System.out.print("ici");
					colors.put(r, new Reg(ralloc));
					a =1; 
					break;
				}
			}
			*/
      //if(a == 1)
      //	continue;
      todo.put(r, new LinkedList<>());
      for (Register ra : Register.allocatable) {
        if (!(ig.graph.get(r).intfs.contains(ra))) {
          // colors.get(r).add()
          todo.get(r).add(ra);
        }
      }
    }

    while (!todo.isEmpty()) {
      Register r = choose(todo.keySet(), ig.graph);
      //System.out.println("choose " + r);
      LinkedList<Register> tmp = todo.get(r);
      Register rr = null;

	  boolean b = false; 
      if (tmp != null && tmp.size() != 0) {
        //prefs
        for (Register rtmp : ig.graph.get(r).prefs) {
          if (tmp.contains(rtmp)) {
            rr = rtmp;
			b = true; 
          }
		 
        }
		if(!b)
        	rr = (Register) tmp.getFirst();

        for (Register inter : ig.graph.get(r).intfs) {
          if (todo.keySet().contains(inter)) {
            todo.get(inter).remove(rr);
          }
        }
        Reg o = new Reg(rr);
        todo.remove(r);
        colors.put(r, o);
      } else {
        todo.remove(r);
        colors.put(r, new Spilled(nlocals));
        nlocals += Memory.word_size;
      }
    }
  }
}
