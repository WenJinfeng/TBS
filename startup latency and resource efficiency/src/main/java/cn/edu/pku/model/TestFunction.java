package cn.edu.pku.model;

public enum TestFunction {

    //function
    Base("Base",0),
    FLOAT("float",1),
    IMAGE_PROCESSING("liuyiimageprocessing",2),
    LINPACK("liuyiinpack",3),
    MATRIX("liuyimatrix",4),
    RANDOMIO("liuyirandomio",5),
    SEQUENTIAIO("liuyisequentiaio",6),
    CNN("liuyicnn",7),
    RNN("liuyirnn",8),
    LRTRAINING("liuyimllrtraining",9),
    LRSERVING("liuyimllrprediction",10),
    MEMORY("MEMORY",11),


    //test type
    TEST_COLD_START("cold start", 0),
    TEST_MEMORY("memory", 1),
    TEST_PACKAGE_SIZE("package size", 2)


    ;









    private String name;
    private int index;

    TestFunction(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
