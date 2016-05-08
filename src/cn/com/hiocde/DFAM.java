package cn.com.hiocde;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import sun.rmi.runtime.Log;

import com.sun.org.apache.xml.internal.security.Init;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

public class DFAM {
	
	Map<String, State> DFAStateSet=new HashMap<String,State>();	 //a NFA maybe a DFA , so i use State not DFAState to imp polymorphism 
	Set<String> keyWords=new  HashSet<String>();
	Set<String> alphabet=new HashSet<String>();
	final String EMPTY_STRING="@";
	
	public static void main(String[] args) { 
		DFAM dfam=new DFAM();
		dfam.init(args[0]);			//args[0] --- regular grammar file path
		dfam.run(args[1]);			//args[1] --- source text file path
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
			while((line=br.readLine())!=null){		//generate NFA , the NFA is made up of four sub-NFAs
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
		
		NFA2DFA(NFAStateSet);
	}
	
	public void run(String text_path){
		State currentS=DFAStateSet.get("S");
		BufferedReader br=null;
		BufferedWriter bw=null;
		
		try {
			br = new BufferedReader(new FileReader(text_path));	
			bw =new BufferedWriter(new FileWriter("token_stream.txt",true));		//appending style to write 
			char ch;
			
			while((ch=(char)br.read())!=-1){
				if(ch=='\b'||ch=='\n'||ch=='\r'||ch=='\t'){
					continue;
				}
				
				currentS=currentS.mapf(ch+"").get(0);		//polymorphism
				if(currentS!=null){
					switch(currentS.getId()){
					case "Z3":								//hit identifier(include key word) which is the most , so as the first item
						if(alphabet.contains(currentS.getIdentifiedStr())){
							bw.write("KEY_WORD "+currentS.getIdentifiedStr()+"\n");
						}else{
							bw.write("IDENTIFIER "+currentS.getIdentifiedStr()+"\n");
						}											
						break;
					case "Z1":
						bw.write("OPERATOR "+currentS.getIdentifiedStr()+"\n");						
						break;
					case "Z4":
						bw.write("CONST "+currentS.getIdentifiedStr()+"\n");						
						break;
					case "Z2":
						bw.write("LIMITER "+currentS.getIdentifiedStr()+"\n");						
						break;
					default:continue;
					}
					currentS=DFAStateSet.get("S");			//reset currentS as startS
				}else{				
					//TODO
					System.out.println("Exist invalid word!");
					return;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		System.out.println("Lexcial analysis Succeed , Waiting for grammar analysis");
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
	
	public void NFA2DFA(Map<String, State> NFA){
		boolean isDFA=true;
		for(State s:NFA.values()){
			if(s.isMultiExit()){
				isDFA=false;				//it's not DFA
			}
		}		
		
		if(isDFA==true){
			DFAStateSet=NFA;
		}else{
			Queue<HashSet<State>> queue=new LinkedList<HashSet<State>>();
			HashSet<State> startS=new HashSet<State>();
			startS.add(NFA.get("S"));
			startS=closure(startS);
			queue.offer(startS);
			
			String stateCode=zipState(startS);
			State dfaS=new DFAState(stateCode);			//statecode as id , it's convenient to index
			DFAStateSet.put(stateCode, dfaS);			
			
			HashSet<State> ss=null;			
			
			while(!queue.isEmpty()){
				ss=queue.poll();
				dfaS=DFAStateSet.get(zipState(ss));			
				
				for(String ch:alphabet){
					ss=closure(Move(ss,ch));				//state transfer
					if(ss.isEmpty())	continue;
					
					stateCode=zipState(ss);
					if(!DFAStateSet.containsKey(stateCode)){					
						queue.offer(ss);
						DFAStateSet.put(stateCode, new DFAState(stateCode));		//add new dfaState
					}
					dfaS.put(ch,DFAStateSet.get(stateCode));						//build map relation:ds-->dfaS									
				}				
			}
		}
	}
	
	public String zipState(HashSet<State> sset){			//hash function:stateSet --> stateCode
		String stateCode=null;
		String id=null;	
		State s=null;
		
		Iterator<State> ite = sset.iterator();
		while(ite.hasNext()){					 
			s=ite.next();
			id=s.getId();						//add id of state to statecode with up-order
			
			switch(id){							//special deal for start_state and terminal_state
			case "S":
				return "S";
			case "Z1":
				return "Z1";
			case "Z2":
				return "Z2";
			case "Z3":
				return "Z3";
			case "Z4":
				return "Z4";
			}
					
			if(stateCode==null){
				stateCode=id;
			}else{
				char idc=id.charAt(0);
				int len=stateCode.length();
				for(int i=0;i<len;++i){
					if(idc>stateCode.charAt(i)){
						if(i!=len-1){
							String s0=stateCode.substring(0,i+1);
							String s1=stateCode.substring(i+1,len);
							stateCode=s0+id+s1;
						}else{
							stateCode+=id;
						}					
					}
				}
			}
		}
		return stateCode;
	}
	
	public HashSet<State> closure(HashSet<State> sset){
		Iterator<State> ite=sset.iterator();
		State s=null;
		HashSet<State> newSset=(HashSet<State>) sset.clone();
		while(ite.hasNext()){							//cant add element to container when using iterator!! 
			s=ite.next();
			closure_op(s,newSset);
		}
		
		return newSset;		
	}
	
	public void closure_op(State s,HashSet<State> sset){		
		Vector<State> ss=s.mapf(EMPTY_STRING);
		for(State st:ss){
			sset.add(st);
			closure_op(st,sset);
		}
	}
	
	public HashSet<State> Move(HashSet<State> sset,String ch){
		Iterator<State> ite=sset.iterator();
		State s=null;
		HashSet<State> newSset=new HashSet<State>();
		Vector<State> mappedS=null;
		while(ite.hasNext()){							
			s=ite.next();	
			mappedS=s.mapf(ch);
			if(mappedS!=null){
				for(State st:mappedS){
					newSset.add(st);
				}
			}
		}
		
		return newSset;
	}
	
}
