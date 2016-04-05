package main.data;

public class TaskHeader extends Task {
    public static final int CELL_OFFSET_TODAY = 1;
    public static final int CELL_OFFSET_TOMORROW = 2;
    public static final int CELL_OFFSET_UPCOMING = 3;
    public static final int CELL_OFFSET_SOMEDAY = 4;
    private String title;
    private Type headerType;

    public enum Type {
        TODAY, TOMORROW, UPCOMING, SOMEDAY, OVERDUE
    }

    public TaskHeader(String headerTitle) {
        this.title = headerTitle;

        switch (headerTitle.toLowerCase()) {
            case "today" :
                headerType = Type.TODAY;
                break;
            case "tomorrow" :
                headerType = Type.TOMORROW;
                break;
            case "upcoming" :
                headerType = Type.UPCOMING;
                break;
            case "someday" :
                headerType = Type.SOMEDAY;
                break;
            case "overdue" :
                headerType = Type.OVERDUE;
                break;
            default: // TODO
        }
    }

    public String getTitle() {
        return this.title;
    }

    public Type getType() {
        return headerType;
    }

    @Override
    public String toString() {
        return this.title;
    }

}
