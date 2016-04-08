//@@author A0126297X
package main.data;

import java.util.ArrayList;

public class ParseIndexResult {
    private boolean hasValid;
    private boolean hasInvalid;
    private ArrayList<Integer> validIndexes;
    private ArrayList<Integer> invalidIndexes;

    public ParseIndexResult() {
        hasValid = false;
        hasInvalid = false;
        validIndexes = null;
        invalidIndexes = null;
    }

    public boolean hasValidIndex() {
        return hasValid;
    }

    public void setHasValid(boolean hasValid) {
        this.hasValid = hasValid;
    }

    public boolean hasInvalidIndex() {
        return hasInvalid;
    }

    public void setHasInvalid(boolean hasInvalid) {
        this.hasInvalid = hasInvalid;
    }

    public ArrayList<Integer> getValidIndexes() {
        return validIndexes;
    }

    public void setValidIndexes(ArrayList<Integer> validIndexes) {
        this.validIndexes = validIndexes;
    }

    public ArrayList<Integer> getInvalidIndexes() {
        return invalidIndexes;
    }

    public void setInvalidIndexes(ArrayList<Integer> invalidIndexes) {
        this.invalidIndexes = invalidIndexes;
    }

    public String getValidIndexesString() {
        return convertIndexesToString(validIndexes);
    }

    public String getInvalidIndexesString() {
        return convertIndexesToString(invalidIndexes);
    }

    /**
     * This method converts an {@code ArrayList<Integer>} of indexes to
     * {@code String}. This method is used by the UI.
     * 
     * @param indexes
     *            {@code ArrayList<Integer>} to be converted
     * @return {@code String} of converted indexes
     */
    private String convertIndexesToString(ArrayList<Integer> indexes) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isRange = false;

        int start = indexes.get(0);
        stringBuilder.append(start);

        for (int i = 1; i < indexes.size(); i++) {
            int next = indexes.get(i);

            if ((next - 1) == start) {
                isRange = true;
                start = next;
            } else {
                if (isRange) {
                    stringBuilder.append("-" + start);
                    isRange = false;
                } else {
                    stringBuilder.append("," + start);
                }
                start = next;
                stringBuilder.append("," + start);
            }
        }

        if (isRange) {
            // account for having a range at the end
            stringBuilder.append("-" + start);
        }

        return stringBuilder.toString();
    }
}