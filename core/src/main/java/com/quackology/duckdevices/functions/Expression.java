package com.quackology.duckdevices.functions;

import com.quackology.duckdevices.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Expression extends Differentiable {
    private static final String[] OPERATORS = Utils.array("Cos", "Sin", "Tan", "Arccos", "Arcsin", "Arctan", "^", "Log", "*", "/", "+", "-", ",");
    private static final Map<String, Integer> PRECEDENT = Map.of(
            ",", 0,
            "+", 1,
            "-", 1,
            "*", 2,
            "/", 2,
            "^", 3
    );
    private String[] postfix;

    public Expression(String infix, int derivativeCount) {
        this(infixToPostfix(infix), derivativeCount);
    }

    public Expression(Expression old, int derivativeCount) {
        super(derivativeCount);
        this.postfix = old.postfix;

        derivatives[0] = this;
        for(int i = 0; i < old.derivatives.length; i++) {
            derivatives[i] = old.derivatives[i];
        }
        for(int i = old.derivatives.length; i < derivatives.length; i++) {
            ((Expression) derivatives[i-1]).simplify();
            derivatives[i] = new Expression(differentiate((Expression) derivatives[i-1]), 0);
            ((Expression) derivatives[i]).simplify();
        }
    }

    private Expression(String[] postfix, int derivativeCount) {
        super(derivativeCount);
        this.postfix = postfix;

        derivatives[0] = this;
        for(int i = 1; i < derivatives.length; i++) {
            ((Expression) derivatives[i-1]).simplify();
            derivatives[i] = new Expression(differentiate((Expression) derivatives[i-1]), 0);
            ((Expression) derivatives[i]).simplify();
        }
    }

    public Expression(String[] expression, String[] derivative) {
        super(1);
        this.postfix = expression;
        this.derivatives[1] = new Expression(derivative, 0);
    }

    @Override
    public Double apply(Double x) {
        ArrayList<Double> stack = new ArrayList<>();
        for(String token : postfix) {
            double a, b;
            switch(token) {
                case "Cos":
                    //Cos(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.cos(a));
                    break;
                case "Sin":
                    //Sin(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.sin(a));
                    break;
                case "Tan":
                    //Tan(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.tan(a));
                    break;
                case "Arccos":
                    //Arccos(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.acos(a));
                    break;
                case "Arcsin":
                    //Arcsin(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.asin(a));
                    break;
                case "Arctan":
                    //Arctan(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.atan(a));
                    break;
                case "^":
                    //a^b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.pow(a, b));
                    break;
                case "Log":
                    //log b (a) = ln(a) / ln(b)
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(Math.log(a) / Math.log(b));
                    break;
                case "*":
                    //a * b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(a * b);
                    break;
                case "/":
                    //a / b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(a / b);
                    if(b == 0) {
                        return (this.apply(1e-9) + this.apply(-1e-9)) / 2;
                    }
                    break;
                case "+":
                    //a + b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(a + b);
                    break;
                case "-":
                    //a - b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(a - b);
                    break;
                default:
                    if(token.equals("x")) stack.add(x);
                    else if(token.equals("E")) stack.add(Math.E);
                    else stack.add(Double.parseDouble(token));
            }
        }
        return stack.get(0);
    }

    public void simplify() {
        String[] simplified = simplify(this);
        while(!Arrays.toString(simplified).equals(Arrays.toString(postfix))) {
            this.postfix = simplified;
            simplified = simplify(this);
        }
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String s : postfix) {
            out.append(s).append(" ");
        }
        return out.toString();
    }

    public String toInfix() {
        ArrayList<String> stack = new ArrayList<>();
        for(String token : postfix) {
            String a, b;
            switch(token) {
                case "Cos":
                    //Cos(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Cos(" + a + ")");
                    break;
                case "Sin":
                    //Sin(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Sin(" + a + ")");
                    break;
                case "Tan":
                    //Tan(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Tan(" + a + ")");
                    break;
                case "Arccos":
                    //Arccos(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Arccos(" + a + ")");
                    break;
                case "Arcsin":
                    //Arcsin(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Arcsin(" + a + ")");
                    break;
                case "Arctan":
                    //Arctan(a)
                    a = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Arctan(" + a + ")");
                    break;
                case "^":
                    //a^b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("(" + a + " ^ " + b + ")");
                    break;
                case "Log":
                    //log b (a) = ln(a) / ln(b)
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("Log(" + a + " , " + b + ")");
                    break;
                case "*":
                    //a * b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("(" + a + " * " + b + ")");
                    break;
                case "/":
                    //a / b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("(" + a + " / " + b + ")");
                    break;
                case "+":
                    //a + b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("(" + a + " + " + b + ")");
                    break;
                case "-":
                    //a - b
                    a = stack.get(stack.size()-2);
                    b = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add("(" + a + " - " + b + ")");
                    break;
                default:
                    stack.add(token);
            }
        }
        return stack.get(0);
    }

    private static String[] simplify(Expression expression) {
        ArrayList<Expression> stack = new ArrayList<>();
        for(String token : expression.postfix) {
            Expression u, v;
            String[] out;
            switch(token) {
                case "Cos":
                    u = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    out = u.toString().matches(".*x.*") ? Utils.concat(u.postfix, Utils.single(token)) : Utils.single(String.valueOf(Math.cos(Double.parseDouble(u.toString()))));
                    stack.add(new Expression(out, 0));
                    break;
                case "Sin":
                    u = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    out = u.toString().matches(".*x.*") ? Utils.concat(u.postfix, Utils.single(token)) : Utils.single(String.valueOf(Math.sin(Double.parseDouble(u.toString()))));
                    stack.add(new Expression(out, 0));
                    break;
                case "Tan":
                    u = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    out = u.toString().matches(".*x.*") ? Utils.concat(u.postfix, Utils.single(token)) : Utils.single(String.valueOf(Math.tan(Double.parseDouble(u.toString()))));
                    stack.add(new Expression(out, 0));
                    break;
                case "Arccos":
                    u = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    out = u.toString().matches(".*x.*") ? Utils.concat(u.postfix, Utils.single(token)) : Utils.single(String.valueOf(Math.cos(Double.parseDouble(u.toString()))));
                    stack.add(new Expression(out, 0));
                    break;
                case "Arcsin":
                    u = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    out = u.toString().matches(".*x.*") ? Utils.concat(u.postfix, Utils.single(token)) : Utils.single(String.valueOf(Math.sin(Double.parseDouble(u.toString()))));
                    stack.add(new Expression(out, 0));
                    break;
                case "Arctan":
                    u = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    out = u.toString().matches(".*x.*") ? Utils.concat(u.postfix, Utils.single(token)) : Utils.single(String.valueOf(Math.tan(Double.parseDouble(u.toString()))));
                    stack.add(new Expression(out, 0));
                    break;
                case "^":
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    if(v.toString().equals("0 ")) {
                        out = Utils.single("1");
                    } else if (v.toString().equals("1 ")) {
                        out = u.postfix;
                    } else if(u.toString().matches(".*x.*") || v.toString().matches(".*x.*")) {
                        out = Utils.concat(u.postfix, v.postfix, Utils.single(token));
                    } else {
                        out = Utils.single(String.valueOf(Math.pow(Double.parseDouble(u.toString()), Double.parseDouble(v.toString()))));
                    }
                    stack.add(new Expression(out, 0));
                    break;
                case "Log":
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    if(u.toString().equals("1 ")) {
                        out = Utils.single("0");
                    } else if(u.toString().matches(".*x.*") || v.toString().matches(".*x.*")) {
                        out = Utils.concat(u.postfix, v.postfix, Utils.single(token));
                    } else {
                        out = Utils.single(String.valueOf(Math.log(Double.parseDouble(u.toString())) / Math.log(Double.parseDouble(v.toString()))));
                    }
                    stack.add(new Expression(out, 0));
                    break;
                case "*":
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    if(u.toString().equals("0 ") || v.toString().equals("0 ")) {
                        out = Utils.single("0");
                    } else if(v.toString().equals("1 ")) {
                        out = u.postfix;
                    } else if(u.toString().equals("1 ")) {
                        out = v.postfix;
                    } else if(u.toString().matches(".*x.*") || v.toString().matches(".*x.*")){
                        out = Utils.concat(u.postfix, v.postfix, Utils.single(token));
                    } else {
                        out = Utils.single(String.valueOf(Double.parseDouble(u.toString()) * Double.parseDouble(v.toString())));
                    }
                    stack.add(new Expression(out, 0));
                    break;
                case "/":
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    if(u.toString().equals("0 ")) {
                        out = Utils.single("0");
                    } else if(v.toString().equals("1 ")) {
                        out = u.postfix;
                    } else if(u.toString().matches(".*x.*") || v.toString().matches(".*x.*")){
                        out = Utils.concat(u.postfix, v.postfix, Utils.single(token));
                    } else {
                        out = Utils.single(String.valueOf(Double.parseDouble(u.toString()) / Double.parseDouble(v.toString())));
                    }
                    stack.add(new Expression(out, 0));
                    break;
                case "+":
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    if(u.toString().equals("0 ")) {
                        out = v.postfix;
                    } else if(v.toString().equals("0 ")) {
                        out = u.postfix;
                    } else if(u.toString().matches(".*x.*") || v.toString().matches(".*x.*")){
                        out = Utils.concat(u.postfix, v.postfix, Utils.single(token));
                    } else {
                        out = Utils.single(String.valueOf(Double.parseDouble(u.toString()) + Double.parseDouble(v.toString())));
                    }
                    stack.add(new Expression(out, 0));

                    break;
                case "-":
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    if(u.toString().equals("0 ")) {
                        out = v.postfix;
                    } else if(v.toString().equals("0 ")) {
                        out = u.postfix;
                    } else if(u.toString().matches(".*x.*") || v.toString().matches(".*x.*")){
                        out = Utils.concat(u.postfix, v.postfix, Utils.single(token));
                    } else {
                        out = Utils.single(String.valueOf(Double.parseDouble(u.toString()) - Double.parseDouble(v.toString())));
                    }
                    stack.add(new Expression(out, 0));
                    break;
                default:
                    if(token.equals("E")) stack.add(new Expression(String.valueOf(Math.E), 0));
                    else stack.add(new Expression(token, 0));
            }
        }
        return stack.get(0).postfix;
    }

    private static String[] infixToPostfix(String infix) {
        String[] tokens = infix.split("\\s+");
        int len = tokens.length;
        for(String token : tokens) len -= token.equals("(") || token.equals(")") || token.equals(",") ? 1 : 0;
        String[] out = new String[len];
        int ind = 0;
        ArrayList<String> stack = new ArrayList<>();
        for (String token : tokens) {
            if (token.matches("[^(]*\\([^(]*")) {
                stack.add(token);
            } else if (token.equals(")")) {
                String curr = "";
                if(!stack.isEmpty()) curr = stack.get(stack.size() - 1);
                while (!stack.isEmpty() && !curr.matches("[^(]*\\([^(]*")) {
                    out[ind++] = curr;
                    stack.remove(stack.size() - 1);
                    if(!stack.isEmpty()) curr = stack.get(stack.size() - 1);
                }
                if (!curr.equals("(")) out[ind++] = curr.substring(0, curr.length() - 1);
                stack.remove(stack.size() - 1);
            } else if (isOperator(token)) {
                String curr = "";
                if(!stack.isEmpty()) curr = stack.get(stack.size() - 1);
                while (!stack.isEmpty() && precedent(curr, token)) {
                    out[ind++] = curr;
                    stack.remove(stack.size() - 1);
                    if(!stack.isEmpty()) curr = stack.get(stack.size() - 1);
                }
                if (!token.equals(",")) stack.add(token);
            } else {
                out[ind++] = token;
            }
        }
        while(!stack.isEmpty()) {
            out[ind++] = stack.get(stack.size() - 1);
            stack.remove(stack.size() - 1);
        }
        return out;
    }

    private static String[] differentiate(Expression expression) {
        //base case: x' = 1, c = 0
        if(expression.postfix.length == 1 && !isOperator(expression.postfix[0])) {
            return expression.postfix[0].equals("x") ? Utils.single("1") : Utils.single("0");
        }

        ArrayList<Expression> stack = new ArrayList<>();
        for(String token : expression.postfix) {
            Expression u, v, du, dv;
            switch(token) {
                case "Cos":
                    //d/dx Cos(u) = -Sin[u] * u' => u' u Sin * -1 *
                    u = stack.get(stack.size()-1);
                    du = (Expression) u.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, u.postfix, Utils.array("Sin", "*", "-1", "*"))));
                    break;
                case "Sin":
                    //d/dx Sin(u) = Cos[u] * u' => u' u Cos *
                    u = stack.get(stack.size()-1);
                    du = (Expression) u.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, u.postfix, Utils.array("Cos", "*"))));
                    break;
                case "Tan":
                    //d/dx Tan(u) = u' / (1 + u^2) => u' u 2 ^ 1 + /
                    u = stack.get(stack.size()-1);
                    du = (Expression) u.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, u.postfix, Utils.array("2", "^", "1", "+", "/"))));
                    break;
                case "Arccos":
                    //d/dx Arccos(u) = -1 / (1 - x ^ 2) ^ 0.5 * u' => u' -1 1 u 2 ^ - 0.5 ^ / *
                    u = stack.get(stack.size()-1);
                    du = (Expression) u.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, Utils.array("-1", "1"), u.postfix, Utils.array("2", "^", "-", "0.5", "^", "/", "*"))));
                    break;
                case "Arcsin":
                    //d/dx Arcsin(u) = 1 / (1 - x ^ 2) ^ 0.5 * u' => u' 1 u 2 ^ - 0.5 ^ /
                    u = stack.get(stack.size()-1);
                    du = (Expression) u.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, Utils.single("1"), u.postfix, Utils.array("2", "^", "-", "0.5", "^", "/"))));
                    break;
                case "Arctan":
                    //d/dx Arctan(u) = 1 / (1 + x ^ 2) ^ 0.5 * u' => u' 1 u 2 ^ + 0.5 ^ /
                    u = stack.get(stack.size()-1);
                    du = (Expression) u.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, Utils.single("1"), u.postfix, Utils.array("2", "^", "+", "0.5", "^", "/"))));
                    break;
                case "^":
                    //d/dx u ^ v = u^v (v' ln u + v u' / u) => u v ^ v' u Ln * v u' * u / + *
                    u = stack.get(stack.size()-2);
                    du = (Expression) u.getDerivative(1);
                    v = stack.get(stack.size()-1);
                    dv = (Expression) v.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, v.postfix, Utils.single(token)),
                            Utils.concat(u.postfix, v.postfix, Utils.single("^"), dv.postfix, u.postfix, Utils.array("E", "Log", "*"), v.postfix, du.postfix, Utils.single("*"), u.postfix, Utils.array("/", "+", "*"))));
                    break;
                case "Log":
                    //d/dx log v (u) = (u' / u - v' log v (u) / v)/ln(v) => u' u / v' v / u v Log * - v ln /
                    u = stack.get(stack.size()-2);
                    du = (Expression) u.getDerivative(1);
                    v = stack.get(stack.size()-1);
                    dv = (Expression) v.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, v.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, u.postfix, Utils.single("/"), dv.postfix, v.postfix, Utils.single("/"), u.postfix, v.postfix, Utils.array("Log", "*", "-"), v.postfix, Utils.array("E", "Log", "/"))));
                    break;
                case "*":
                    //d/dx u * v = u dv + v du => u v' * v u' * +
                    u = stack.get(stack.size()-2);
                    du = (Expression) u.getDerivative(1);
                    v = stack.get(stack.size()-1);
                    dv = (Expression) v.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, v.postfix, Utils.single(token)),
                            Utils.concat(u.postfix, dv.postfix, Utils.single("*"), v.postfix, du.postfix, Utils.array("*", "+"))));
                    break;
                case "/":
                    //d/dx u / v = d/dx u (v^-1) => d/dx u v -1 ^ *
                    u = stack.get(stack.size()-2);
                    v = stack.get(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(Utils.concat(u.postfix, v.postfix, Utils.array("-1", "^", "*")), 1));
                    break;
                case "+":
                    //d/dx u + v = du + dv
                    u = stack.get(stack.size()-2);
                    du = (Expression) u.getDerivative(1);
                    v = stack.get(stack.size()-1);
                    dv = (Expression) v.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, v.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, dv.postfix, Utils.single("+"))));
                    break;
                case "-":
                    //d/dx u - v = du - dv
                    u = stack.get(stack.size()-2);
                    du = (Expression) u.getDerivative(1);
                    v = stack.get(stack.size()-1);
                    dv = (Expression) v.getDerivative(1);
                    stack.remove(stack.size()-1);
                    stack.remove(stack.size()-1);
                    stack.add(new Expression(
                            Utils.concat(u.postfix, v.postfix, Utils.single(token)),
                            Utils.concat(du.postfix, dv.postfix, Utils.single("-"))));
                    break;
                default:
                    stack.add(new Expression(token, 1));
            }
        }
        return ((Expression) stack.get(0).getDerivative(1)).postfix;
    }

    private static boolean isOperator(String element) {
        boolean out = false;
        for(String operator : OPERATORS) {
            if (element.equals(operator)) {
                out = true;
                break;
            }
        }
        return out;
    }

    private static boolean precedent(String a, String b) {
        if(a.matches("[^(]*\\([^(]*")) return false;
        int valueA = PRECEDENT.get(a);
        int valueB = PRECEDENT.get(b);
        if(valueA == 3 && valueB == 3) {
            return false;
        } else if(valueA == valueB) {
            return true;
        }
        return valueA > valueB;
    }
}
