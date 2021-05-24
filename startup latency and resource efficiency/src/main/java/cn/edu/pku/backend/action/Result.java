package cn.edu.pku.backend.action;

public class Result {
	boolean status;
	String msg;
	String action = null;
	Object data;

	Result() {
		status = false;
		msg = null;
		data = "";
	}
}