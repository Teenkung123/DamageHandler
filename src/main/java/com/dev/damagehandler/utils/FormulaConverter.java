package com.dev.damagehandler.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTE: don't dare to read the code of this class
 *
 * This class is used to evaluate String formula to be calculated the damage
 */
public class FormulaConverter {

    /**
     * Calculate the value obtained from the given formula
     *
     * @param formula String formula input
     * @param variables variables
     * @param placeholders built-in placeholders
     * @return the calculated value
     */
    public static double convert(String formula, Map<String, String> variables, Map<String, String> placeholders) {
        return b(a(formula, variables, placeholders), placeholders);
    }

    /**
     * Calculate the value obtained from the given equation
     *
     * @param formula String formula input
     * @param variables variables
     * @param placeholders built-in placeholders
     * @return the calculated value
     */
    public static double convert(String formula, ConfigurationSection variables, Map<String, String> placeholders) {

        Map<String, String> map = new HashMap<>();
        for (String key : variables.getKeys(false)) {
            map.put(key, variables.getString(key));
        }

        return b(a(formula, map, placeholders), placeholders);
    }

    /**
     * Method for solving equations by eliminating variables until the equation becomes free of variables,
     * allowing it to be further calculated in the next step.
     *
     * @param formula String formula input
     * @param variables variables
     * @param placeholders built-in placeholders
     * @return The formula for calculation after variables have been eliminated.
     */
    private static String a(String formula, Map<String, String> variables, Map<String, String> placeholders) {
        String patternString = "\\$(.*?)\\$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(formula);

        while (matcher.find()) {
            String variableName = matcher.group(1);

            String variableValue = variables.get(variableName);
            if (hasVariable(variableValue)) {
                formula = formula.replace(matcher.group(), String.valueOf(b(a(variableValue, variables, placeholders), placeholders)));
            } else {
                formula = formula.replace(matcher.group(), String.valueOf(b(variableValue, placeholders)));
            }
        }
        return formula;
    }


    /**
     * Evaluate string formula to calculated value
     *
     * @param formula String formula input without variables
     * @param placeholders built-in placeholders
     * @return the calculated value
     */
    private static double b(String formula, Map<String, String> placeholders) {

        Pattern pattern1 = Pattern.compile("#(.*?)#");
        Matcher matcher1 = pattern1.matcher(formula);
        while (matcher1.find()) {
            formula = formula.replace(matcher1.group(), placeholders.get(matcher1.group().replace("#", "")));
        }

        Pattern pattern2 = Pattern.compile("if\\(([^,()]+),\\s*([^,()]+),\\s*([^,()]+)\\)");
        Matcher matcher2 = pattern2.matcher(formula);
        while (matcher2.find()) {
            String s = IfFunction(matcher2.group());
            //Bukkit.broadcastMessage(s);
            formula = formula.replace(matcher2.group(), s);
        }

        ExpressionBuilder expressionBuilder = new ExpressionBuilder(formula);

        Expression expression = expressionBuilder.build();

        // Evaluate and return the result
        return expression.evaluate();
    }

    private static boolean hasVariable(String input) {
        Pattern pattern = Pattern.compile("\\$(.*?)\\$");
        Matcher matcher = pattern.matcher(input);
        Set<String> variableSet = new HashSet<>();
        while (matcher.find()) {
            variableSet.add(matcher.group(1));
        }
        return !variableSet.isEmpty();
    }

    /**
     * Method to convert if function in formula
     *
     * @param input match regex "^if\(([\s\S]+),(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)\)$"
     *              Format: "if(<condition>,<double>,<double>)"
     *              Example: "if(some text,1.5,1)"
     * @return a value after process operation
     */
    private static String IfFunction(String input) {
        //Bukkit.broadcastMessage(input);

        Pattern pattern1 = Pattern.compile("^if\\(([^,()]+),\\s*([^,()]+),\\s*([^,()]+)\\)$");
        Matcher matcher1 = pattern1.matcher(input);

        if (matcher1.find()) {
            String condition = matcher1.group(1);  // some_string
            String ifTrue = matcher1.group(2);  // -123.45
            String ifFalse = matcher1.group(3);  // 67.89

            Pattern pattern2 = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)\\s*([><=]|>=|<=|!=)\\s*(-?\\d+(?:\\.\\d+)?)$|^true$|^false$");
            Matcher matcher2 = pattern2.matcher(condition);

            if (matcher2.find()) {

                if (matcher2.group().equals("true")) return ifTrue;
                if (matcher2.group().equals("false")) return ifFalse;

                double arg1 = Double.parseDouble(matcher2.group(1));
                String operator = matcher2.group(2);
                double arg2 = Double.parseDouble(matcher2.group(3));

                boolean a = false;
                switch (operator) {
                    case ">" -> {
                        a = arg1 > arg2;
                    }
                    case "<" -> {
                        a = arg1 < arg2;
                    }
                    case ">=" -> {
                        a = arg1 >= arg2;
                    }
                    case "<=" -> {
                        a = arg1 <= arg2;
                    }
                    case "=" -> {
                        a = arg1 == arg2;
                    }
                    case "!=" -> {
                        a = arg1 != arg2;
                    }
                }

                if (a) {
                    return ifTrue;
                } else {
                    return ifFalse;
                }

            } else {
                return "0";
            }

        } else {
            return "0";
        }
    }
}
