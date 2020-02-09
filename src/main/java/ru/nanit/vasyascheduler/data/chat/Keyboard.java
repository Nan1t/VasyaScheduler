package ru.nanit.vasyascheduler.data.chat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Keyboard {

    private LinkedList<List<ChatButton>> buttons = new LinkedList<>();

    public List<List<ChatButton>> getButtons(){
        return buttons;
    }

    public void addButton(ChatButton button){
        List<ChatButton> row;

        if(buttons.isEmpty()){
            row = new ArrayList<>();
            buttons.add(row);
        } else{
            row = buttons.getLast();
        }

        row.add(button);
    }

    public void addButtonToNewRow(ChatButton button){
        List<ChatButton> row = new ArrayList<>();
        row.add(button);
        buttons.add(row);
    }
}
