package cn.com.hiocde;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

public class DFAM {
	
	Map<String, DFAState> DFAStateSet=new HashMap<String,DFAState>();
	Set<String> keyWords=new  HashSet<String>();
	Set<String> alphabet=new HashSet<String>();
	final String EMPTY_STRING="@";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void init(String path){			//according to regular grammar, construct NFA then convert to DFA
		Map<String, State> NFAStateSet=new HashMap<String,State>();
		State startS=new State("S");
		//State endS0=new State("Z0");				//Z represent end state
		State endS1=new State("Z1");				//0-key words 1-operator 2-limiter 3-identifier 4-const
		State endS2=new State("Z2");
		State endS3=new State("Z3");
		State endS4=new State("Z4");
		NFAStateSet.put("S",startS);		
		NFAStateSet.put("Z1",endS1);
		NFAStateSet.put("Z2",endS2);
		NFAStateSet.put("Z3",endS3);
		NFAStateSet.put("Z4",endS4);
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));	
			String line;
			while((line=br.readLine())!=null){		//generate NFA
				if(line.startsWith("//")){
					continue;
				}
				switch(line){
					case "#0":
						while((line=br.readLine())!="#0"){
							keyWords.add(line);
						}
						break;
					case "#1":
						while((line=br.readLine())!="#1"){
							productionToMap(NFAStateSet, line, endS1);
						}
						break;
					case "#2":
						while((line=br.readLine())!="#2"){
							productionToMap(NFAStateSet, line, endS2);
						}
						break;
					case "#3":
						while((line=br.readLine())!="#3"){
							productionToMap(NFAStateSet, line, endS3);
						}
						break;
					case "#4":
						while((line=br.readLine())!="#4"){
							productionToMap(NFAStateSet, line, endS4);
						}
						break;
				}						
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void productionToMap(Map<String,State> NFAStateSet,String prodc,State endS){
		if(prodc.length()==4){									//A->a					
			char ch=prodc.charAt(0);										
			if(NFAStateSet.containsKey(ch+"")==false){
				State s=new State(ch+"");		
				s.put(prodc.charAt(3)+"",endS);
				NFAStateSet.put(s.getId(),s);
			}else{
				State s=NFAStateSet.get(ch+"");
				s.put(prodc.charAt(3)+"", endS);
			}
		}else{													//A->aB
			char ch=prodc.charAt(0);			
			char ch4=prodc.charAt(4);
			if(NFAStateSet.containsKey(ch4+"")==false){			//if B not exist, new B add to stateSet
				NFAStateSet.put(ch4+"",new State(ch4+""));
			}
								
			if(NFAStateSet.containsKey(ch+"")==false){
				State s=new State(ch+"");								
				s.put(prodc.charAt(3)+"",NFAStateSet.get(ch4+""));
				NFAStateSet.put(s.getId(),s);
			}else{
				State s=NFAStateSet.get(ch+"");
				s.put(prodc.charAt(3)+"", NFAStateSet.get(ch4+""));
			}					
		}
	}
	
	public DFAState closure(HashSet<State> sset , String id){
		Iterator<State> ite=sset.iterator();
		State s=null;
		HashSet<State> newSset=(HashSet<State>) sset.clone();
		while(ite.hasNext()){							//cant add element to container when using iterator!! 
			s=ite.next();
			closure_op(s,newSset);
		}
		
		DFAState dfaS=new DFAState(id);
		while(ite.hasNext()){					 
			s=ite.next();
			dfaS.addstate(s.getId());
		}
		return dfaS;
	}
	
	public void closure_op(State s,HashSet<State> sset){
		Map map=s.getMap();
		if(map.containsKey(EMPTY_STRING)){
			Vector<State> ss=(Vector<State>) map.get(EMPTY_STRING);
			for(State st:ss){
				sset.add(st);
				closure_op(st,sset);
			}
		}
	}
}
