package com.glow.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Module 6 — Calculator Tools.
 * Demonstrates that tools can do precise computation the LLM might get wrong.
 */
@Slf4j
@Component
public class CalculatorTools {

    @Tool(description = "Evaluate a basic arithmetic expression. Supports +, -, *, / and parentheses. Example: '(3 + 5) * 2'.")
    public String evaluate(String expression) {
        log.info("[TOOL] evaluate called with expression={}", expression);
        try {
            double result = new ExpressionEvaluator().evaluate(expression);
            return "Result: " + result;
        } catch (Exception e) {
            return "Error evaluating expression: " + e.getMessage();
        }
    }

    @Tool(description = "Calculate the percentage of a value. E.g. 'What is 15% of 200?'")
    public String percentage(double value, double percent) {
        log.info("[TOOL] percentage called with value={}, percent={}", value, percent);
        double result = value * percent / 100.0;
        return "%.2f%% of %.2f = %.2f".formatted(percent, value, result);
    }

    /** Minimal recursive-descent evaluator for +−×÷ with parentheses. */
    private static class ExpressionEvaluator {
        private String expr;
        private int pos;

        double evaluate(String expression) {
            this.expr = expression.replaceAll("\\s+", "");
            this.pos = 0;
            return parseExpression();
        }

        private double parseExpression() {
            double result = parseTerm();
            while (pos < expr.length() && (expr.charAt(pos) == '+' || expr.charAt(pos) == '-')) {
                char op = expr.charAt(pos++);
                result = op == '+' ? result + parseTerm() : result - parseTerm();
            }
            return result;
        }

        private double parseTerm() {
            double result = parseFactor();
            while (pos < expr.length() && (expr.charAt(pos) == '*' || expr.charAt(pos) == '/')) {
                char op = expr.charAt(pos++);
                result = op == '*' ? result * parseFactor() : result / parseFactor();
            }
            return result;
        }

        private double parseFactor() {
            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++; // skip '('
                double result = parseExpression();
                pos++; // skip ')'
                return result;
            }
            int start = pos;
            if (pos < expr.length() && expr.charAt(pos) == '-') pos++;
            while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) pos++;
            return Double.parseDouble(expr.substring(start, pos));
        }
    }
}
