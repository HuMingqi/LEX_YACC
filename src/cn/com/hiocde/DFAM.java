package cn.com.hiocde;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class DFAM {
	
	Map<String, State> DFAStateSet=new HashMap<String,State>();	 //a NFA maybe a DFA , so i use State not DFAState to imp polymorphism 
	Set<String> keyWords=new  HashSet<String>();
	Set<String> alphabet=new HashSet<String>();
	final static String EMPTY_STRING=" ";
	
	public static void main(String[] args) { 
		DFAM dfam=new DFAM();
		dfam.init(args[0]);								//args[0] --- regular grammar file path
		String tokens_file=dfam.run(args[1]);			//args[1] --- source text file path | start lexical analysis
		
		ItemsCluster phaser=new ItemsCluster();
		phaser.phasing(args[2],tokens_file);				//args[2] --- type-2 grammar file path | start phasing
	}

	public void init(String gpath){					//according to regular grammar, construct NFA then convert to DFA
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
			BufferedReader br = new BufferedReader(new FileReader(gpath));	
			String line;
			while((line=br.readLine())!=null){		//generate NFA , the NFA is made up of four sub-NFAs
				if(line.startsWith("//")||"".equals(line)){
					continue;
				}
				switch(line){
					case "#0":
						while(!"#0".equals(line=br.readLine())){		
							if(line.startsWith("//")){
								continue;
							}
							keyWords.add(line);
						}
						break;
					case "#1":
						while(!"#1".equals(line=br.readLine())){
							if(line.startsWith("//")){
								continue;
							}
							productionToMap(NFAStateSet, line, endS1);
						}
						break;
					case "#2":
						while(!"#2".equals(line=br.readLine())){
							if(line.startsWith("//")){
								continue;
							}
							productionToMap(NFAStateSet, line, endS2);
						}
						break;
					case "#3":
						while(!"#3".equals(line=br.readLine())){
							if(line.startsWith("//")){
								continue;
							}
							productionToMap(NFAStateSet, line, endS3);
						}
						break;
					case "#4":
						while(!"#4".equals(line=br.readLine())){
							if(line.startsWith("//")){
								continue;
							}
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
	
	public String run(String text_path){					//start lexical analysis , return tokens　path
		State currentS=DFAStateSet.get("S");
		State oldS=null;		//the fore state of currentS
		StringReader sr=null;
		BufferedWriter bw=null;
		String tokens_path=null;
		
		try {
			//br = new BufferedReader(new FileReader(text_path));							
			int lastIndex;
			if((lastIndex=text_path.lastIndexOf('/'))!=-1){
				tokens_path=text_path.substring(0,lastIndex+1)+"token_stream.txt";
			}else if((lastIndex=text_path.lastIndexOf("\\"))!=-1){						//***in code or storage , '\' is shift-meaning char
				tokens_path=text_path.substring(0,lastIndex+1)+"token_stream.txt";		//***so in there,+1 not +2
			}else{
				System.out.println("Source Text Path Is Wrong!");						
				return null;
			}
			
			sr=preProcess(text_path);				//***discard annotation
			bw =new BufferedWriter(new FileWriter(tokens_path,false));//non-appending style to write 			
			int ich;
			char ch;					
			
			while((ich=sr.read())!=-1){							//***(ich=(char)br.read())!=-1 is wrong!!
				ch=(char)ich;
				if(ch==' '||ch=='\n'||ch=='\r'||ch=='\t'){		//***ch=='\b' capture space fail
					continue;
				}								
				
				oldS=currentS;		
				Vector<State> newS=currentS.mapf(ch+"");//polymorphism
														//***you cant use mapf twice!!because of self_circle
				if(newS!=null){	
					currentS=newS.get(0);		
					if(keyWords.contains(currentS.getIdentifiedStr())){		//check key word
						bw.write("0 "+currentS.getIdentifiedStr()+"\n");
						currentS=DFAStateSet.get("S");						//reset currentS as startS						
					}
					continue;
				}else{				
					//TODO
					String id=oldS.getId();
					if(id.length()>1&&id.charAt(id.length()-2)=='Z'){		//the olds is endS , matching the longest profix!!
						//0-key_word 1-operator 2-limiter 3-identifier 4-const
						if(keyWords.contains(oldS.getIdentifiedStr())){
							bw.write("0 "+oldS.getIdentifiedStr()+"\n");
						}else{
							bw.write(id.charAt(id.length()-1)+" "+oldS.getIdentifiedStr()+"\n");
						}												
						
						currentS=DFAStateSet.get("S");						//reset currentS as startS
						newS=currentS.mapf(ch+"");
						if(newS==null){
							System.out.println("Existing invalid word!");
							System.out.println("Identified String : "+oldS.getIdentifiedStr());
							System.out.println("Error Character : "+ch);
							
							sr.close();
							bw.close();
							return null;
						}else{
							currentS=newS.get(0);
						}						
					}else{
						System.out.println("Existing invalid word!");
						System.out.println("Identified String : "+oldS.getIdentifiedStr());
						System.out.println("Error Character : "+ch);

						sr.close();
						bw.close();
						return null;
					}					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String id=currentS.getId();
		try{
			if(id.length()>1&&id.charAt(id.length()-2)=='Z'){			
				if(keyWords.contains(oldS.getIdentifiedStr())){				
					bw.write("0 "+oldS.getIdentifiedStr()+"\n");				
				}else{
					bw.write(id.charAt(id.length()-1)+" "+currentS.getIdentifiedStr()+"\n");
				}				
				
				bw.write("#\n");
				System.out.println("\nLexcial analysis succeed , waiting for grammar analysis");		//lexcial analysis succeed				
			}else{
				System.out.println("Existing unterminated word!");
				System.out.println("Identified String : "+currentS.getIdentifiedStr());		
				return null;
			}
		}catch(IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				sr.close();
				bw.close();			//***writing file now				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return tokens_path;		
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
			HashSet<State> ss2=null;
			
			while(!queue.isEmpty()){				
				ss=queue.poll();				
				dfaS=DFAStateSet.get(zipState(ss));			
				
				for(String ch:alphabet){
					ss2=closure(move(ss,ch));				//state transfer
					if(ss2.isEmpty())	continue;
					
					stateCode=zipState(ss2);
					if(!DFAStateSet.containsKey(stateCode)){							
						queue.offer(ss2);
						DFAStateSet.put(stateCode, new DFAState(stateCode));		//add new dfaState
					}
					dfaS.put(ch,DFAStateSet.get(stateCode));						//build map relation:ds-->dfaS
										
					if("@".equals(ch)){
						ch="\\@";
					}
					System.out.println("DFA MAP : "+"("+dfaS.getId()+","+stateCode+","+ch+")");
				}				
			}
		}
	}
	
	public void productionToMap(Map<String,State> NFAStateSet,String prodc,State endS){		//support shift meaning and meta char
		char ch=prodc.charAt(0);
		char ch3=prodc.charAt(3);
		if(NFAStateSet.containsKey(ch+"")==false){				
			NFAStateSet.put(ch+"",new State(ch+""));
		}
		
		if(prodc.length()==4){							//***A->a										
			String chs=ch3+"";
			if(ch3=='@'){
				chs=EMPTY_STRING;
			}
						
			State s=NFAStateSet.get(ch+"");
			if(ch3=='a'||ch3=='d'){
				meta_char(s,endS,ch3+"");				//deal with meta char
			}else{					
				s.put(chs,endS);
				
				if(ch3!='@'){
					alphabet.add(ch3+"");				//add into alphabet
				}	
				
				System.out.println("NFA MAP : ("+ch+","+endS.getId()+","+ch3+")");		//output map
			}								
			
		}else if(prodc.length()==5){							
			char ch4=prodc.charAt(4);			
			
			if(ch3!='\\'){											//***A->aB					
				if(NFAStateSet.containsKey(ch4+"")==false){			//if B not exist, new B add to stateSet
					NFAStateSet.put(ch4+"",new State(ch4+""));
				}				
				
				State s1=NFAStateSet.get(ch+"");
				State s2=NFAStateSet.get(ch4+"");
				if(ch3=='a'||ch3=='d'){
					meta_char(s1,s2,ch3+"");				
				}else{					
					s1.put(ch3+"",s2);
					alphabet.add(ch3+"");
					
					System.out.println("NFA MAP : ("+ch+","+ch4+","+ch3+")");		
				}					
			}else{													//***A->\a
				String chs=ch4+"";				
				
				State s=NFAStateSet.get(ch+"");
				s.put(chs,endS);
				
				alphabet.add(ch4+"");
				
				System.out.println("NFA MAP : ("+ch+","+endS.getId()+",\\"+ch4+")");		
			}
		}else{														//***A->\aB
			char ch4=prodc.charAt(4);
			char ch5=prodc.charAt(5);
			if(NFAStateSet.containsKey(ch5+"")==false){			//if B not exist, new B add to stateSet
				NFAStateSet.put(ch5+"",new State(ch5+""));
			}
			
			State s1=NFAStateSet.get(ch);
			State s2=NFAStateSet.get(ch5);
			s1.put(ch4+"",s2);
			
			alphabet.add(ch4+"");
			
			System.out.println("NFA MAP : ("+ch+","+ch5+",\\"+ch4+")");
		}
	}
	
	public void meta_char(State s1,State s2,String meta){
		switch(meta){
		case "a":
			for(char a='a';a<='z';++a){
				s1.put(a+"", s2);
				//String upperCase=(char)(a-32)+"";
				//s1.put(upperCase,s2);
				
				alphabet.add(a+"");					//add into alphabet
				//alphabet.add(upperCase);	
				
				System.out.println("NFA MAP : ("+s1.getId()+","+s2.getId()+","+a+")");		
				//System.out.println("NFA MAP : ("+s1.getId()+","+s2.getId()+","+upperCase+")");
			}
			break;
		case "d":
			for(char integ='0';integ<='9';++integ){
				s1.put(integ+"",s2);
				
				alphabet.add(integ+"");
				
				System.out.println("NFA MAP : ("+s1.getId()+","+s2.getId()+","+integ+")");
			}
			break;
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
			
			if("S".equals(id)){
				return "S";
			}			
					
			if(stateCode==null){
				stateCode=id;
			}else{
				if(id.charAt(0)=='Z'){
					stateCode+=id;
					continue;
				}
				
				char idc=id.charAt(0);
				int len=stateCode.length();
				for(int i=0;i<len;++i){
					if(idc<=stateCode.charAt(i)){	
						if(i==0){
							stateCode=id+stateCode;
						}else{
							String s0=stateCode.substring(0,i);
							String s1=stateCode.substring(i);
							stateCode=s0+id+s1;	
						}
						break;
					}else{
						if(i==len-1){
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
		//sset.add(s);
		
		if(ss!=null){
			for(State st:ss){
				sset.add(st);
				closure_op(st,sset);
			}
		}		
	}
	
	public HashSet<State> move(HashSet<State> sset,String ch){
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
	
	public StringReader preProcess(String file_path) throws IOException{					//discard annotation
		BufferedReader br=new BufferedReader(new FileReader(file_path));
		StringBuffer strbf=new StringBuffer();
		String line;
		
		while((line=br.readLine())!=null){
			if(line.startsWith("//")){
				continue;
			}else{
				strbf.append(line);
			}
		}
		
		return new StringReader(strbf.toString());
		
	}
	
}
