package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
	
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
	private static String extractFromBrackets(String expression, int index) {
		String result = "";
		int moreBrackets = 0;
		while (index<expression.length()) {
			if (expression.substring(index, index+1).equals("[")) {
				moreBrackets++;
				result += expression.substring(index,index+1);
				index++;
				continue;
			} else if (expression.substring(index, index+1).equals("]") && moreBrackets<=0) {
				break;
			} else {
				String add = expression.substring(index,index+1);
				result +=add;
				if (add.equals("]")) {
					moreBrackets--;
				}
				index++;
			}
		}
		return result;
	}
	private static String extractValue(String expr,int p) {
		String result = "";
		boolean negate = false;
		while (p<expr.length()) {
			if (p==0 && expr.charAt(p)=='-') {
				negate = true;
				p++;
			}
			if (Character.isDigit(expr.charAt(p))||Character.isLetter(expr.charAt(p)) ) {
				result +=expr.charAt(p);
				p++;
			} else if (expr.charAt(p)=='.' && Character.isDigit(expr.charAt(p+1))) {
					result += expr.charAt(p);
					p++;
			} else if (expr.charAt(p)=='[' && result.length()>0) { 
				String h = extractFromBrackets(expr,p+1);
				result =result+"[" +h +"]";
				break;
			} else {
				if (result.length()==0) {
					continue;
				} else {
					break;
				}
			}
		}
		if (negate) {
			result = "-"+result;
		}
			return result;
	}
	private static int findInArrays(ArrayList<Variable>vars,ArrayList<Array> arrays, String name,String expression) {
		int result = 0;
		float exp = evaluate(expression,vars,arrays);
		float k = Math.round(exp);
		int expr = 0;
		if (k>exp) {
			expr = (int)k-1;
		} else {
			expr = (int)k;
		}
		for (int i = 0; i<arrays.size();i++) {
			if (arrays.get(i).name.equals(name)) {
				result = arrays.get(i).values[expr];
			}
		}
		return result;
	}
	private static int valOf(String s, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		int result = 0;
		if (Character.isDigit(s.charAt(0))) {
			result = Integer.valueOf(s);
		} else if (s.indexOf("[")>=0) {
			int ind = s.indexOf("[");
			String arrName = s.substring(0,ind);
			String insideExpr = s.substring(ind+1,s.length()-1);
			result = findInArrays(vars, arrays, arrName, insideExpr);
			return result ; //need to evaluate array's value here 
		} else {
			for (int i = 0; i<vars.size();i++) {
				if (vars.get(i).name.equals(s)) {
					result = vars.get(i).value;
					break;
				}
			}
		}
		return result;
	}
	
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	String tempVar = "";
    	for (int i = 0; i<expr.length();i++) {
    		if (Character.isLetter(expr.charAt(i))) {
    			if (i==expr.length()-1) {
    				tempVar+=expr.charAt(i);
    				Variable b= new Variable(tempVar);
    				if (!(vars.indexOf(b)>=0) ) {
    					vars.add(b);
    				}
    				break;
    			}
    			tempVar+=expr.charAt(i);
    		} else if (expr.charAt(i)=='[' ) {
    			Array a = new Array(tempVar);
    			if (!(arrays.indexOf(a)>=0)) {
    				arrays.add(a);
    			} 
    			tempVar="";
    		} else if (tempVar.length()>0) {
    			Variable c = new Variable(tempVar);
    			if (!(vars.indexOf(c)>=0)) {
    				vars.add(c);
    			}
    			tempVar = "";
    		} else {
    			continue;
    		}
    	}
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    private static Stack<Float> calculate(Stack<Float>values, Stack<String>operators) {
    	if (values.size()==1) {
    		return values;
    	}
    	String nextSign = operators.pop();
    	System.out.println("Operator is: "+nextSign);
    	while (true) {
    		if (nextSign.equals("+") || nextSign.equals("-")) {
    			operators.push(nextSign);
    			return values;
    		} else {
    			float value;
    			if (nextSign.equals("*")) {
    				value = values.pop()*calculate(values,operators).pop();
    				values.push(value);
    			} else if (nextSign.equals("/")) {
    				float firstValue = values.pop();
    				float numerator = calculate(values,operators).pop();
    				System.out.println("Values size: "+values.size()+" ,operator's size: "+operators.size());
    				value = numerator/firstValue;
    				values.push(value);
    				
    				}
    			if (operators.size()>0) {
    				nextSign = operators.pop();
    			} else {
    				break;
    			}
    		}
    	}
    	return values;
    }
    
    private static float performEvaluation(Stack<Float> values, Stack<String> operators) {
       	float result =values.pop();
    	while (!operators.isEmpty()) {
    		String sign = operators.pop();
    		switch (sign) {
    		case ("+"):
    			float b = performEvaluation(values,operators);
    			result = result+b;
    			continue;
    		case "-" :
    			float c = performEvaluation(values,operators);
    			result = c-result;
    			continue;
    		case "*" :
    			values = calculate(values,operators);
    			result = result*values.pop();
    			continue;
    		case "/" :
    			values = calculate(values,operators);
    			result = (float)values.pop()/result;
    			continue;
    		}
    		
    	}
    	return result;
    }
    private static String extractExpression(int ind,String expr) { //need to modify
    	String result = "";		
    	int moreCB = 0;//for the case with more than 
    	for (int i = ind; i<expr.length();i++) {	// one pair of braces
    		if (expr.charAt(i)=='(') {
    			moreCB++;
    			result +=expr.charAt(i);
    			continue;
    		}
    		if (!(expr.charAt(i)==')')) {
    			result+=expr.charAt(i);
    		} else if(moreCB>0){
    			result += expr.charAt(i);
    			moreCB--;
    		} else {
    			break;
    		}
    	}
    	return result;
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	System.out.println("Evaluating expression: "+expr);
    	float result = 0;
    	if (expr.equals("")) {
    		return 0;
    	}
    	Stack<Float> myValues = new Stack<>();
    	Stack<String> operators = new Stack<>();
    	Stack<Variable> myVars = new Stack<>();
    	String ops = "+-/*";
    	expr = expr.replaceAll("\\s","");
    	for (int i=0;i<expr.length();i++) {
    		if (expr.substring(i,i+1).equals("(")) {
    			String nextVal = extractExpression(i+1,expr);
    			System.out.println("Extracted expression is: "+nextVal+" ,its length: "+nextVal.length());
    			int start = i+1;
				int end = i+1+nextVal.length();
				float insideValue = evaluate(expr.substring(i+1,end),vars,arrays);
				System.out.println("Inside value: "+insideValue);
				String newExpr = expr.substring(0,i)+insideValue+expr.substring(end+1);
				result = evaluate(newExpr,vars, arrays);
				return result;
    		}
    		String e = extractValue(expr,i);
    		Float a;
    		if (Character.isDigit(e.charAt(e.length()-1))) {
    			a = Float.parseFloat(e);
    		} else {
    			a = (float)valOf(e,vars,arrays);
    		}
    		myValues.push(a);
		
    		int skipChars = e.length();
    			i+=skipChars;
    			if (i<expr.length()) {
    				String newOperator = expr.substring(i,i+1);
    				if (ops.indexOf(newOperator)>=0) {
    					if (expr.substring(i+1,i+2).equals("-")) {
    						float needToNegate = myValues.pop();
    						float negated = 0-needToNegate;
    						myValues.push(negated);
    						i++;
    					}
    					operators.push(newOperator);
    				} 
    			}
    		
    	}
    	result = performEvaluation(myValues,operators);

    	// following line just a placeholder for compilation
    	return result;
    }
}

//d+a*A[b+A[d-25*b-4]*B[2]-1]/3