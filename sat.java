import java.util.ArrayList;

public class sat {
	static sat s = new sat();
	
	private class Clause {
		//notVals is of the size of the total number of atoms
		//it takes on 1 if the atom is negated within the clause or 0 if not and -1 if the atom is not in this clause
		int[] notVals;
		int numAtoms;
		
		public Clause(int size) {
			notVals = new int[size];
			numAtoms = size;
			for (int i = 0; i < size; i++) {
				notVals[i] = -1;
			}
		}
		
		public void addSymbol(int notVal) {
			numAtoms++;
			int[] tempVals = notVals;
			notVals = new int[numAtoms];
			for (int i = 0; i < numAtoms - 1; i++) {
				notVals[i] = tempVals[i];
			}
			notVals[numAtoms - 1] = notVal;
		}
		
		public void setSymbol(int index, int notVal) {
			notVals[index] = notVal;
		}
		
		//returns 1 if the claus can be resolved to true, 0 if it can be resolved to false, and -1 if it cannot be resolved
		public int resolution(int[] model) {
			int untrueVal = 0;
			for (int i = 0; i < numAtoms; i++) {
				if (notVals[i] != -1) {
					if (model[i] != -1) {
						if ((notVals[i] != 0) ^ (model[i] != 0)) return 1;
					} else {
						untrueVal = -1;
					}
				}
			}
			return untrueVal;
		}
		
		public int[] findUnitSymbol(int[] model) {
			int num = 0;
			int index = -1;
			int value = -1;
			for (int i = 0; i < numAtoms; i++) {
				if (notVals[i] != -1 && model[i] == -1) {
					index = i;
					num++;
				}
			}
			if (num == 1) {
				if (notVals[index] == 0) value = 1;
				else value = 0;
			}
			
			int[] ret = {index, value};
			return ret;
		}
	}

	public static void main(String[] args) {
		boolean pureSymbol = true, unitClause = true;
		for (String arg : args) {
			switch(arg) {
			case "--nounit":
				unitClause = false;
			case "--nopure":
				pureSymbol = false;
			}
		}
		System.out.println(checkSatisfiability(args[0], pureSymbol, unitClause));
	}
	
	public static String checkSatisfiability(String exp, boolean pS, boolean uC) {
		int numAtoms = 0;
		ArrayList<String> vars = new ArrayList<String>();
		ArrayList<Clause> clauses = new ArrayList<Clause>();
		clauses.add(s.new Clause(numAtoms));
		int curClause = 0;
		
		for (int i = 0; i < exp.length(); i++) {
			int notVal = 0;
			if (exp.charAt(i) == '~') {
				i++;
				notVal = 1;
			}
			
			int nextIndex = nextOperator(exp, i);
			String atom = exp.substring(i, nextIndex);
			i += (nextIndex - i);
			if (!vars.contains(atom)) {
				numAtoms++;
				vars.add(atom);
				for (int j = 0; j <= curClause; j++) {
					if (j == curClause) {
						clauses.get(j).addSymbol(notVal);
					} else {
						clauses.get(j).addSymbol(-1);
					}
				}
			} else {
				clauses.get(curClause).setSymbol(vars.indexOf(atom), notVal);
			}
			if (nextIndex != exp.length() && exp.charAt(nextIndex) == '\n') {
				curClause++;
				clauses.add(s.new Clause(numAtoms));
			}
		}
		
		int[] initModel = new int[vars.size()];
		for (int i = 0; i < vars.size(); i++) {
			initModel[i] = -1;
		}
		int[] finalModel = DPLL(vars, clauses, initModel, pS, uC);
		if (finalModel[0] == -2) return "Unsatisfiable";
		else {
			String retStr = "Satisfiable\n";
			for (int i = 0; i < numAtoms; i++) {
				int modelVal = finalModel[i];
				if (modelVal == -1) modelVal = 0;
				retStr += vars.get(i) + " - " + ((modelVal != 0) ? 'T' : 'F') + '\n';
			}
			return retStr;
		}
	}
	
	public static int[] DPLL(ArrayList<String> symbols, ArrayList<Clause> clauses, int[] model, boolean pS, boolean uC) {
		//checks if the current model is accepted
		boolean modelFits = true;
		for (int i = 0; i < clauses.size(); i++) {
			int res = clauses.get(i).resolution(model);
			if (res == 0) {
				int[] fArr = {-2};
				return fArr;
			}
			if (res == -1) modelFits = false;
		}
		if (modelFits) return model;
		
		//pure symbol check
		if (pS) {
			int[] pureSymbol = findPureSymbol(symbols, clauses, model);
			if (pureSymbol[0] != -1) {
				int[] tempModel = model;
				tempModel[pureSymbol[0]] = pureSymbol[1];
				return DPLL(symbols, clauses, tempModel, pS, uC);
			}
		}
		
		//unit clause check
		if (uC) {
			for (int i = 0; i < clauses.size(); i++) {
				int[] unit = clauses.get(i).findUnitSymbol(model);
				if (unit[1] != -1) {
					int[] tempModel = model;
					tempModel[unit[0]] = unit[1];
					return DPLL(symbols, clauses, tempModel, pS, uC);
				}
			}
		}
		
		int first = firstUnset(model, symbols.size());
		int[] firstTrue = model.clone();
		firstTrue[first] = 1;
		int[] firstFalse = model.clone();
		firstFalse[first] = 0;
		
		int[] ret = DPLL(symbols, clauses, firstTrue, pS, uC);
		if (ret[0] != -2) return ret;
		return DPLL(symbols, clauses, firstFalse, pS, uC);
	}
	
	public static int[] findPureSymbol(ArrayList<String> symbols, ArrayList<Clause> clauses, int[] model) {
		for (int i = 0; i < symbols.size(); i++) {
			int val = -1;
			if (model[i] == -1) {
				int j = 0;
				boolean conflictFound = false;
				while (j < clauses.size() && !conflictFound) {
					int symbolVal;
					if (clauses.get(j).notVals[i] == -1) symbolVal = -1;
					else if (clauses.get(j).notVals[i] == 0) symbolVal = 1;
					else symbolVal = 0;
					if (val == -1 || val == symbolVal) {
						val = symbolVal;
					}
					else if (symbolVal != -1) {
						val = -1;
						i++;
						conflictFound = true;
					}
					j++;
				}
				if (val != -1) {
					int[] ret = {i, val};
					return ret;
				}
			}
		}
		int[] ret = {-1, -1};
		return ret;
	}
	
	public static int firstUnset(int[] model, int size) {
		for (int i = 0; i < size; i++) {
			if (model[i] == -1) {
				return i;
			}
		}
		return -1;
	}
	
	private static int nextOperator(String str, int start) {
		for (int i = start; i < str.length(); i++) {
			if (str.charAt(i) == ',' || str.charAt(i) == '\n') {
				return i;
			}
		}
		return str.length();
	}
}
