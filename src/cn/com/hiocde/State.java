package cn.com.hiocde;

import java.util.*;

/**
 * <tt>State</tt> is abstract of NFA State , including state id and map relations.  
 * @author Hmqi
 * @see DFAState
 */
class State {								//NFA State , maybe DFA
	
	protected String id;
	protected Map<String, Vector<State>> map=new HashMap<String,Vector<State>>();//string is character
	private boolean multiExit=false;		//judge NFA IS DFA in fact through multivalue/empty edge
	protected String identifiedStr="";		//identified string current state
	
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
	
	/**
	 *Whether do state exist multiple exit or empty edge or not.
	 * @return True if Multiple exit or empty edge
	 */
	public boolean isMultiExit(){
		return multiExit;
	}
	
	public String getIdentifiedStr(){
		return identifiedStr;
	}

	/**
	 * Add a map into the state
	 * @param ch readed symbol
	 * @param state object state
	 */
	public void put(String ch,State state){
		if(map.containsKey(ch)){
			multiExit=true;
			map.get(ch).add(state);
		}else{
			if(ch.equals(DFAM.EMPTY_STRING)){
				multiExit=true;
			}
			
			Vector<State> ss=new Vector<State>();
			ss.add(state);
			map.put(ch,ss);
		}			
	}
	
	/**
	 * Read ch return object states.
	 * @param ch
	 * @return return states.
	 */
	public Vector<State> mapf(String ch){		//the method exports map , not using getMap style . it's stupid , users can modify map casually
		if(map.containsKey(ch)){
			for(State s:map.get(ch)){
				s.identifiedStr=identifiedStr+ch;	//set identified string
			}
			return map.get(ch);
		}else{
			return null;
		}
	}
	
}

/**
 * <tt>DFAState</tt> inherits <tt>State</tt> , represents a DFA state.
 * @author Hmqi
 * @see State
 */
class DFAState extends State{						//to be honest , this inheritance has less meaning
													//because mapf has no natural difference when used.u can only use State class to imple DFA_STATE
	public DFAState(String id) {
		super(id);
		// TODO Auto-generated constructor stub
	}	
	
	@Override	
	/**
	 * Override mapf of father-class.
	 * @param ch
	 * @return return state.
	 */	
	public Vector<State> mapf(String ch){			//at first , i want to use "State mapf(String ch)",but its not override!! NO WAY..
		if(map.containsKey(ch)){
			State dfaS= map.get(ch).get(0);
			dfaS.identifiedStr=identifiedStr+ch;	//set identified string 
			return map.get(ch);						//***it will modify itself when appears circle
		}else{
			return null;
		}
	}
	
}
