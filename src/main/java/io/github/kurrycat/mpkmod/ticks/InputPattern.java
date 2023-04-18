package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.util.Tuple;

import java.util.List;

public class InputPattern {
    private List<Tuple<TickInput, Integer>> pattern;

    public InputPattern(List<Tuple<TickInput,Integer>> pattern) {
        this.pattern = pattern;
    }

    private List<Tuple<TickInput, Integer>> workerPattern() {
        return null;
    }


    /**
     * @param inputList List that is checked for pattern
     * @return -1 if no match, else offset from the end of the list
     */
    public int match(List<TickInput> inputList) {
        int start = -1;

        for(int i = 0; i < inputList.size(); i++) {

        }
        return 0;
    }
}
