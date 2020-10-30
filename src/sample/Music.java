package sample;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;

public class Music {
    public Integer number;
    private SimpleStringProperty name;
    private SimpleStringProperty path;
    private SimpleStringProperty refactorPath;

    Music(int number,String name, String path){
        var refPath = path.replace("file:/", "");
        refPath = refPath.replace("%", "");
        this.number = number;
        this.name = new SimpleStringProperty(name);
        this.path = new SimpleStringProperty(path);
        this.refactorPath = new SimpleStringProperty(refPath);
    }

    public String getName(){ return name.get();}
    public void setName(String value){ name.set(value);}

    public String getPath(){ return path.get();}
    public void setPath(String value){ path.set(value);}

    public String getRefactorPath(){ return refactorPath.get();}
    public void setRefactorPathPath(String value){ refactorPath.set(value);}

    public Integer getNumber() { return number;}
    public void setNumber(Integer value) { number = value;}
}

