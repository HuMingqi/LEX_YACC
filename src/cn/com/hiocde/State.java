package cn.com.hiocde;

import java.util.*;

class State {
	
	private String id;
	private Map<String, Vector<State>> map=new HashMap<String,Vector<State>>();//string is character
	private boolean multiExit=false;		//same input , different shift->judge NFA IS DFA
	
	public State(String id){
		this.id=id;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Map<String, Vector<State>> getMap(){
		return map;
	}
	
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
	
	public State mapf(String ch){				//assume it's a DFA(also be determined from NFA)
		if(map.containsKey(ch)){
			return (State)map.get(ch).get(0);
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
	
	public void addstate(String id){
		if(statelist==null){
			statelist=id;
		}else{
			char idc=id.charAt(0);
			int len=statelist.length();
			for(int i=0;i<len;++i){
				if(idc>statelist.charAt(i)){
					if(i!=len-1){
						String s0=statelist.substring(0,i+1);
						String s1=statelist.substring(i+1,len);
						statelist=s0+id+s1;
					}else{
						statelist+=id;
					}					
				}
			}
		}
	}
	
	public String getSList(){
		return statelist;
	}
	
}
