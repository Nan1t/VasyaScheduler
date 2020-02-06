package ru.nanit.vasyascheduler.data.chat;

import java.io.InputStream;

public class Media {

    private InputStream stream;
    private String fileName;
    private Type type;

    public Media(Type type){
        this.type = type;
    }

    public Media(Type type, InputStream stream){
        this.type = type;
        this.stream = stream;
    }

    public Media(Type type, InputStream stream, String fileName){
        this.type = type;
        this.stream = stream;
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public Type getType(){
        return type;
    }

    public InputStream getStream(){
        return stream;
    }

    public void setStream(InputStream stream){
        this.stream = stream;
    }

    public enum Type{
        IMAGE,
        VIDEO,
        SOUND,
        DOCUMENT;
    }
}
