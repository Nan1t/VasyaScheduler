package ru.nanit.vasyascheduler.data.schedule;

public class SheetPoint {

    private final int col, row;

    public SheetPoint(int col, int row){
        this.col = col;
        this.row = row;
    }

    public int getColumn(){
        return col;
    }

    public int getRow(){
        return row;
    }

}
