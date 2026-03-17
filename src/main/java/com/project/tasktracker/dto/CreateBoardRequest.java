package com.project.tasktracker.dto;

public class CreateBoardRequest {
    private String name;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
