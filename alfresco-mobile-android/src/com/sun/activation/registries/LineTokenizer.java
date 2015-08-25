package com.sun.activation.registries;

import java.util.NoSuchElementException;
import java.util.Vector;

class LineTokenizer {
    private int currentPosition;
    private final int maxPosition;
    private final String str;
    private final Vector<String> stack = new Vector<>();
    private static final String singles = "=";    // single character tokens

    /**
     * Constructs a tokenizer for the specified string.
     * <p>
     *
     * @param str a string to be parsed.
     */
    public LineTokenizer(String str) {
        currentPosition = 0;
        this.str = str;
        maxPosition = str.length();
    }

    /**
     * Skips white space.
     */
    private void skipWhiteSpace() {
        while ((currentPosition < maxPosition) && Character.isWhitespace(str.charAt(currentPosition))) {
            currentPosition++;
        }
    }

    /**
     * Tests if there are more tokens available from this tokenizer's string.
     *
     * @return <code>true</code> if there are more tokens available from this
     * tokenizer's string; <code>false</code> otherwise.
     */
    public boolean hasMoreTokens() {
        if (stack.size() > 0) {
            return true;
        }
        skipWhiteSpace();
        return (currentPosition < maxPosition);
    }

    /**
     * Returns the next token from this tokenizer.
     *
     * @return the next token from this tokenizer.
     * @throws NoSuchElementException if there are no more tokens in this
     *                                tokenizer's string.
     */
    public String nextToken() {
        int size = stack.size();
        if (size > 0) {
            String t = stack.elementAt(size - 1);
            stack.removeElementAt(size - 1);
            return t;
        }
        skipWhiteSpace();

        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException();
        }

        int start = currentPosition;
        char c = str.charAt(start);
        if (c == '"') {
            currentPosition++;
            boolean filter = false;
            while (currentPosition < maxPosition) {
                c = str.charAt(currentPosition++);
                if (c == '\\') {
                    currentPosition++;
                    filter = true;
                } else if (c == '"') {
                    String s;

                    if (filter) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = start + 1; i < currentPosition - 1; i++) {
                            c = str.charAt(i);
                            if (c != '\\') {
                                sb.append(c);
                            }
                        }
                        s = sb.toString();
                    } else {
                        s = str.substring(start + 1, currentPosition - 1);
                    }
                    return s;
                }
            }
        } else if (singles.indexOf(c) >= 0) {
            currentPosition++;
        } else {
            while ((currentPosition < maxPosition) &&
                    singles.indexOf(str.charAt(currentPosition)) < 0 &&
                    !Character.isWhitespace(str.charAt(currentPosition))) {
                currentPosition++;
            }
        }
        return str.substring(start, currentPosition);
    }
}
