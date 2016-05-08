package cn.com.hiocde;

import java.util.*;

class State {								//NFA State , maybe DFA
	
	protected String id;
	protected Map<String, Vector<State>> map=new HashMap<String,Vector<State>>();//string is character
	private boolean multiExit=false;		//judge NFA IS DFA in fact
	
	public State(String id){
		this.id=id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	/*public Map<String, Vector<State>> getMap(){			//getXXX is stupid style when XXX is a object which can be modified!!
		return map;
	}
	*/
	
	public boolean isMultiExit(){
		return multiExit;
	}	

	public void put(String ch,State state){
		if(map.containsKey(ch)){
			multiExit=true;
			map.get(ch).add(state);
		}else{
			Vector<State> ss=new Vector<State>();
			ss.add(state);
			map.put(ch,ss);
		}			
	}
	
	public Vector<State> mapf(String ch){		//the method exports map , not using getMap style . it's stupid , users can modify map casually
		if(map.containsKey(ch)){				
			return map.get(ch);
		}else{
			return null;
		}
	}
	
	public State dfaMapf(String ch){
		return null;
	}
}

class DFAState extends State{	

	public DFAState(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}	
	
	@Override
	public State dfaMapf(String ch){		//at first , i want to use "State mapf(String ch)",but its not override!! NO WAY..
		if(map.containsKey(ch)){
			return map.get(ch).get(0);
		}else{
			return null;
		}
	}
	
}
