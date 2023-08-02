package com.dev.damagehandler.utils;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String colorize(String s) {
        if (s == null || s.equals(""))
            return "";
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(s);
        while (match.find()) {
            String hexColor = s.substring(match.start(), match.end());
            s = s.replace(hexColor, net.md_5.bungee.api.ChatColor.of(hexColor).toString());
            match = pattern.matcher(s);
        }
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', s);
    }

    public static int generateRandomNumber(int number1, int number2) {
        if (number1 >= number2) {
            throw new IllegalArgumentException("Invalid range: number1 must be less than number2.");
        }

        Random random = new Random();
        return random.nextInt(number2 - number1 + 1) + number1;
    }

    public static String[] splitTextAndNumber(String input) {
        String regex = "(?<=(\\d([.]\\d+)?))(?=[A-Za-z]+)";
        return input.split(regex);
    }
}
