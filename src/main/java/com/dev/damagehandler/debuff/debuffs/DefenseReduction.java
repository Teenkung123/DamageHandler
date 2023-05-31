package com.dev.damagehandler.debuff.debuffs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DefenseReduction extends DebuffStatus {

    private final double amount;

    public DefenseReduction(double amount, long duration) {
        super("defense_reduction", duration);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public List<DebuffStatus> getCurrentDebuff(List<DebuffStatus> allDebuff) {
        List<DebuffStatus> output = new ArrayList<>();

        List<DefenseReduction> l = new ArrayList<>();
        for (DebuffStatus debuffStatus : allDebuff) {
            DefenseReduction defenseReduction = (DefenseReduction) debuffStatus;

            l.add(defenseReduction);
        }

        l.sort(Comparator.comparingDouble(DefenseReduction::getAmount).thenComparingDouble(DefenseReduction::getDuration));
        Collections.reverse(l);

        output.add(l.get(0));
        return output;
    }
}
