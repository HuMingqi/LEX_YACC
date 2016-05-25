package cn.com.hiocde;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.sun.org.apache.bcel.internal.generic.GOTO;

public class ItemsCluster {
	Vector<ItemSet> cluster=new Vector<ItemSet>();
	Map<Integer,HashMap<String,String>> LRTable=new HashMap<Integer,HashMap<String,String>>();
	Map<String , String> prodcs=new HashMap<String,String>();		//such as R0 : ES , R1 : SaB
	static HashSet<String> non_terminated_s=new HashSet<String>(); 	//register non-terminated-symbol
	
	public void phasing(String yacc_gpath,String tokens_path){		//start phasing token stream , output acc or no for source file
		readProdcs(yacc_gpath);
		buildCluster();
		if(!fillInTable())	return;									//if exist r-r clash or s-r clash , stop phasing
		
		Stack<Integer> stateStack=new Stack<Integer>();
		Stack<String> symbolStack=new Stack<String>();
		stateStack.push(0);symbolStack.push("#");					//init stack
		
		BufferedReader br=null;		
		
		try{
			br=new BufferedReader(new FileReader(tokens_path));
			String line=null;
			String cur_input=null;
			Integer stack_top;
			String action;
			
			System.out.println("Printing Analysis Stack : ");
			
			while((line=br.readLine())!=null){
				if(line.startsWith("//")){
					continue;
				}
				cur_input=line.substring(0,1);				
				
				boolean flag=true;				
				while(flag){											//for case 2
					stack_top=stateStack.peek();
					action=LRTable.get(stack_top).get(cur_input);		//query LR analysis table
					
					printStack(stateStack, symbolStack, cur_input, action);//print current analysis stack
					
					if(action!=null){
						if(action.equals("acc")){
							System.out.println("\nAnalysis Result : ACC");
							br.close();
							return;
						}else if(action.charAt(0)=='R'){					//reduce
							String prodc=prodcs.get(action);
							if(prodc.charAt(1)!='@'){						//not empty-prodc
								for(int i=0;i<prodc.length()-1;++i){
									symbolStack.pop();
									stateStack.pop();
								}
							}
							symbolStack.push(prodc.substring(0,1));
							action=LRTable.get(stateStack.peek()).get(prodc.substring(0,1));//goto
							stateStack.push(Integer.valueOf(action));																					
						}else{												//shift
							symbolStack.push(cur_input);
							stateStack.push(Integer.valueOf(action));
							flag=false;							
						}
					}else{
						System.out.println("\nAnalysis Result : ERROR");
						br.close();
						return;
					}
				}							
			}			
		}catch(IOException ex){
			ex.printStackTrace();
		}		
	}
	
	public void readProdcs(String yacc_gpath){						//read 2-type grammar
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(yacc_gpath));
			String line;
			int prodcNumber=0;
			
			while((line=br.readLine())!=null){
				if(line.startsWith("//")||"".equals(line)){			//discard annotation and empty line
					continue;
				}
				
				line=line.substring(0,1)+line.substring(3);
				prodcs.put("R"+prodcNumber,line);
				non_terminated_s.add(line.substring(0,1)); 			//register non-terminated-symbol
				++prodcNumber;
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	
	public void buildCluster(){
		Item item=new Item("R0",prodcs.get("R0").substring(0,1),prodcs.get("R0").substring(1));
		item.addStringToPre("#");
		
		int id=0;
		ItemSet itemset=new ItemSet(id++);
		itemset.addItem(item);
		HashSet<Item> items=itemset.getItemset();
		
		itemset.setItemset(closure(items));			//initial itemset
		itemset.printSelf();
		cluster.add(itemset);
		
		Queue<ItemSet> queue=new LinkedList<ItemSet>();
		queue.offer(itemset);
		
		HashSet<String> moveBych=new HashSet<String>();
		
		while(!queue.isEmpty()){
			itemset=queue.poll();
			items=itemset.getItemset();
			
			moveBych.clear();//***you know
			for(Item item1:items){
				String expect;
				if((expect=item1.next())!=null){
					moveBych.add(expect);
				}
			}
			
			for(String ch:moveBych){
				ItemSet itemset1=new ItemSet(id++);
				itemset1.setItemset(closure(move(items,ch)));
				boolean existed=false;
				
				for(ItemSet its:cluster){
					if(its.equals(itemset1)){					//item set has existed
						itemset.put(ch,its.getId());
						--id;
						existed=true;
												
						System.out.println("\nMAP : ("+itemset.getId()+","+its.getId()+","+ch+")");
						break;
					}					
				}
				if(!existed){
					cluster.add(itemset1);
					itemset.put(ch,itemset1.getId());
					
					queue.offer(itemset1);
					
					System.out.print("\nMAP : ("+itemset.getId()+","+itemset1.getId()+","+ch+")");
					itemset1.printSelf();					
				}
			}
		}			
	}
	
	public boolean fillInTable(){			//delegate ItemSet
		boolean succeed=true;
		for(ItemSet items:cluster){
			 succeed=items.addLineTo(LRTable);
			 if(!succeed){
				 return false;
			 }
		}
		
		System.out.println("\nLR1 Analysis Table : ");
		HashMap<String,String> line;
		for(Integer sid:LRTable.keySet()){
			line=LRTable.get(sid);
			String action;
			for(String ch:line.keySet()){
				System.out.println("("+sid+","+ch+","+line.get(ch)+")");
			}
			System.out.println();
		}
		
		return true;
	}
	
	public HashSet<Item> move(HashSet<Item> itemset,String ch){
		HashSet<Item> newItemset=new HashSet<Item>();
		Item item1;
		
		for(Item item:itemset){
			if(item.getType()==1||item.getType()==3){
				if(ch.equals(item.next())){
					item1= item.clone();
					item1.replyMove();
					newItemset.add(item1);
				}				
			}
		}
		
		return newItemset;
	}
	
	public HashSet<Item> closure(HashSet<Item> itemset ){//expand itemset and add pre-search-symbol
		HashSet<Item> newItemset=(HashSet<Item>) itemset.clone();
		Iterator<Item> ite=itemset.iterator();
		Item item=null;
		HashSet<String> visited=new HashSet<String>();	//register visited ready-return symbol
		
		while(ite.hasNext()){
			item=ite.next();
			if(item.getType()==3){							
				closure_op(item,newItemset,visited);	//DFS to expand itemset when the item is type_3
			}
		}		
		
		return newItemset;
	}
	
	public String closure_op(Item item,HashSet<Item> newItemset,HashSet<String> visited){			//******so tired!!!
		String expect=item.next();		
		Item item1=null;
		String firstSet;
		
		firstSet=firstSet(item);						//eg:S->.ABc,#/a	get pre-symbol of A
		
		if(!visited.contains(expect)){					//expand itemset
			visited.add(expect);						//***have to be lay in head
			String prodc;
			String right;					
			
			for(String prodcId:prodcs.keySet()){			
				prodc=prodcs.get(prodcId);
				if(prodc.startsWith(expect)){
					right=prodc.substring(1);
					if("@".equals(right)){				//if prodc is empty
						right="";
					}
					item1=new Item(prodcId,expect,right);
					item1.addStringToPre(firstSet);
					newItemset.add(item1);
					
					if(item1.getType()==3){				//***maybe a left-recursion , it's difficult here						
						String temp=closure_op(item1, newItemset, visited);	
						if(item1.getLeft().equals(item1.next())){		//update first set when left-recursion
							firstSet+=temp;
						}
					}
				}
			}
		}else{											//only need update pre-search-symbol , EG: S->.Sa,# | S->.M
			for(Item item2:newItemset){
				if(expect.equals(item2.getLeft())){
					if(item2.getType()==3&&!expect.equals(item2.next())){		//exclude left-recursion , eg:S->.M
						if(item2.addStringToPre(firstSet)){						//when effective add , update M
							boolean update=false;
							for(Item item3:newItemset){
								if(item3.getLeft().equals(item2.next())){
									update=true;								//M->.N has existed
								}
							}
							if(update){
								closure_op(item2, newItemset, visited);			//calculate pre-symbol again ， dynamic update
							}							
						}
					}else{
						item2.addStringToPre(firstSet);
					}
				}
			}
		}
		return firstSet;
	}
	
	public String firstSet(Item item){	
		String after=item.afterExpect();
		HashSet<String> pres=item.getPres();
		StringBuffer firstSet=new StringBuffer("");
		
		if(!pres.isEmpty()){
			StringBuffer temp=new StringBuffer("");
			for(String pre:pres){
				//String temp="";
				//firstSet(after+pre,temp);				//******for String object , dont post ref!!!
				temp.delete(0,temp.length());			//clear temp
				firstSet(after+pre,temp);
				firstSet.append(temp.toString());
			}
		}else{		
			firstSet(after,firstSet);
		}
		
		return firstSet.toString();
	}
	
	public void firstSet(String str,StringBuffer firstSet){
		if(str.length()==0){
			return;
		}else if(!non_terminated_s.contains(str.substring(0,1))){
			firstSet.append(str.substring(0,1));
		}else{
			String nts=str.substring(0,1);
			for(String prodc:prodcs.values()){
				if(prodc.startsWith(nts)&&!(nts.equals(prodc.charAt(1)+""))){		//exclude left-recursion
					if(prodc.charAt(1)=='@'){
						str=str.substring(1);
					}else{
						str=prodc.substring(1)+str.substring(1);
					}
					firstSet(str,firstSet);
				}
			}
		}
	}
	
	public void printStack(Stack<Integer> states,Stack<String> symbols,String input,String action){		
		String sts="",smbs="";
		
		for(Integer st:states){
			sts+=st.toString()+" ";
		}
		for(String smb:symbols){
			smbs+=smb.toString()+" ";
		}
		
		System.out.println("("+sts+","+smbs+","+input+","+action+")");
	}
	
}
