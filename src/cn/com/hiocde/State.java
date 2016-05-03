package cn.com.hiocde;

import java.util.*;

class State {
	
	private String id;
	private Map<String, State> map=new HashMap<String,State>();//string is character
	
	public State(String id){
		this.id=id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void put(String ch,State state){
		map.put(ch,state);
	}
	
	public State mapf(String ch){
		if(map.containsKey(ch)){
			return (State)map.get(ch);
		}else{
			return null;
		}
	}
}

class DFAState extends State{
	
	private String statelist=null;				//zip NFA States with letter's up-order

	public DFAState(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}
	
	public void addstate(String sid){
		
	}
}
