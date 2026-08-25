package com.project.tasktracker.weekly499;

import java.util.*;

public class SortVowelsByFrequency {
    public String sortVowels(String s){
        Map<Character, Integer> freq = new HashMap<>();
        Map<Character, Integer> first = new HashMap<>();

        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (isVowel(c)){
                freq.put(c, freq.getOrDefault(c, 0) + 1);
                first.putIfAbsent(c, i);
            }
        }

        List<Character> vowels = new ArrayList<>(freq.keySet());
        vowels.sort((a,b) -> {
            if (!freq.get(a).equals(freq.get(b))){
                return freq.get(b) - freq.get(a);
            } else {
                return first.get(a) - first.get(b);
            }
        });

        List<Character> ordered = new ArrayList<>();
        for (char c: vowels){
            int count = freq.get(c);
            for (int i = 0; i < count; i++){
                ordered.add(c);
            }
        }

        StringBuilder result = new StringBuilder();
        int idx = 0;
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (isVowel(c)){
                result.append(ordered.get(idx++));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public boolean isVowel(char c){
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
    }
}
