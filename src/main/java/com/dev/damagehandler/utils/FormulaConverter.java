package com.dev.damagehandler.utils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaConverter {
    public static double convert(String formula, Map<String, String> variables, Map<String, String> placeholders) {
        return b(a(formula, variables, placeholders), placeholders);
    }

    public static double convert(String formula, ConfigurationSection variables, Map<String, String> placeholders) {

        Map<String, String> map = new HashMap<>();
        for (String key : variables.getKeys(false)) {
            map.put(key, variables.getString(key));
        }

        return b(a(formula, map, placeholders), placeholders);
    }

    private static String a(String formula, Map<String, String> variables, Map<String, String> placeholders) {
        String patternString = "\\$(.*?)\\$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(formula);
        StringBuilder sb = new StringBuilder(formula);

        while (matcher.find()) {
            String variableName = matcher.group(1);

            String variableValue = variables.get(variableName);
            if (hasVariable(variableValue)) {
                String replacement = String.valueOf(b(a(variableValue, variables, placeholders), placeholders));
                sb.replace(matcher.start(), matcher.end(), replacement);
                matcher.region(matcher.start(), sb.length());
            } else {
                String replacement = String.valueOf(b(variableValue, placeholders));
                sb.replace(matcher.start(), matcher.end(), replacement);
                matcher.region(matcher.start(), sb.length());
            }
        }
        return sb.toString();
    }


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
