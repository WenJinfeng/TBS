package cn.edu.pku.model;

import java.util.ArrayList;
import java.util.List;

public class MemoryTestData extends BaseData{
    public String xname;
    public String yname;
    public String title;
    public List<String> x;
    public List<Double> y;

    public MemoryTestData(){
        super();
        this.xname = "MB";
        this.yname = "ms";
        this.x = new ArrayList<>();
        this.y = new ArrayList<>();
    }
}
