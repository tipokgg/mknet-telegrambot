package entity;

public class Employee {

    private String fullName;
    private int startRow;

    public Employee(String fullName, int startRow) {
        this.fullName = fullName;
        this.startRow = startRow;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
}
