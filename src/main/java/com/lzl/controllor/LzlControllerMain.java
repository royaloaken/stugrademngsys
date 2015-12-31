package com.lzl.controller;

import com.lzl.NetworkTalker;
import org.json.*;
import java.io.*;
import com.lzl.Reply;
import com.lzl.Request;

public class LzlControllerMain extends Thread{
	private JSONObject reply;
	private JSONArray replyContents;
	private int count=0;
	private String identity;
	private String infotype;

	private NetworkTalker talker;//Communication component to database & view

	public LzlControllerMain(){//setup controller first then vieW & Model
		initializeController();
		//new LzlModelMain();
		//new LzlViewMain();  Deleted out for time being.Add them back when debugging is done.
	}

private	void initializeController(){
	try{
		talker=new NetworkTalker(6000,6001,6002,NetworkTalker.CONTROLLER);
	}
	catch(IOException e){
		System.err.println("IOException occured while creating NetworkTalker: "+e.toString());
	}
}

public void run(){
	while(true){
		//single Thread
		JSONObject requestV=null;//request from view
		//JSONObject requestM=null;//request to model
		JSONObject requestBack=null;//M->C->V
		try{
			requestV=talker.getNextRequest();
		}catch(IOException e){
			System.err.println("IOException occured in CONTROLLER while receiving new request: "+e.toString());
		}
		//System.out.println(request);//DEBUG!!!!!!!

		String action=(String)request.get("Request");//get to know what action the request want
		JSONObject firstDetail=(JSONObject)request.get("Detail"); // get the value of first "Detial" key to convenient next stages

		identity=(String)firstDetail.get("Identity");
		infotype=(String)firstDetail.get("Infotype");

		JSONObject secondDetail=(JSONObject)firstDetail.get("Detail");

		reply=new JSONObject();
		replyContents=new JSONArray();
		count=0;
/*
*/
/*+++++login part+++++++++++
格式{"Request":"SET","Detail":{"Identity":"STUDENT","Infotype
":"ACCOUNT","Detail":{"Username":"Lizzy","Password"："123456"}}}
*/
		if(action.equals("LOGIN")){
			JSONObject username=secondDetail.get("Username");
			JSONObject passwordV=secondDetail.get("Password");
			//Password passwordV=secondDetail.get("Password");
			JSONObject requestCorrectPassword=new JSONObject()
										.put("Request","GET")
										.put("Detail",new JSONObject()
										.put("Identity",identity)
										.put("Infotype",infotype)
										.put("Detail",new JSONObject()
										.put("Username",username)));
			talker.sendRequest(NetworkTalker.MODEL,requestCorrectPassword);
			JSONObject resultOfCorrectPassword=null;
			try{
				resultOfCorrectPassword=talker.getNextRequest();
			}
			catch(IOException e){
				System.err.println("IOException occured"+e.toString());
			}

			passwordM=(String)((JSONObject)((JSONObject)resultOfCorrectPassword.get("Detail")).get("Detail")).get("Password");
			if(passwordV.equals(passwordM)){
				talker.sendRequest(NetworkTalker.VIEW,(new Reply(1)).toJSON());//Return
			}
			else{
				talker.sendRequest(NetworkTalker.VIEW,(new Reply(0)).toJSON());
			}
		}
		else if (action.equals("EXIT")){
			talker.sendRequest(NetworkTalker.MODEL,new JSONObject("Request","EXIT"));
			LzlWebServer.stop();//LAST THING
			talker.close();
			break;
		}
		else{
			//to MODEL
			talker.sendRequest(NetworkTalker.MODEL,new Request(action,identity,infotype,secondDetail).toJSON());
			//talker.sendRequest(NetworkTalker.Model,action);
			//talker.sendRequest(NetworkTalker.Model,identity);
			//talker.sendRequest(NetworkTalker.Model,infotype);
			//talker.sendRequest(NetworkTalker.Model,secondDetail);
		}
		//send back to view
		try{
			requestBack=talker.getNextRequest();
		}catch(IOException e){
			System.err.println("IOException occured in CONTROLLER while receiving back from Model: "+e.toString());
		}
		talker.sendRequest(NetworkTalker.VIEW,requestBack);
		//talker.sendRequest(NetworkTalker.Veiw,action);
		//talker.sendRequest(NetworkTalker.View,identity);
		//talker.sendRequest(NetworkTalker.View,infotype);
		//talker.sendRequest(NetworkTalker.View,secondDetail);
	}
}
}
//get(name)
//put(name,value)
