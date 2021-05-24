package cn.edu.pku.adapter.lifecycle;

public interface Activity {
    void init();

    void createFunction(String functionName, String runtime, String role);

    void deleteFunction(String functionName, String qualifier);
}
