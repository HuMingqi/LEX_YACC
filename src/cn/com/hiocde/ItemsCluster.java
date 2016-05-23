package cn.com.hiocde;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ItemsCluster {
	Vector<ItemSet> cluster=new Vector<ItemSet>();
	Map<String , String> prodcs=new HashMap<String,String>();		//such as R0 : ES , R1 : SaB
	static HashSet<String> non_terminated_s=new HashSet<String>(); 		//register non-terminated-symbol
	
	public void readProdcs(String yacc_gpath){						//read 2-type grammar
		BufferedReader br=null;
		try {
			br=new BufferedReader(new FileReader(yacc_gpath));
			String line;
			int prodcNumber=0;
			
			while((line=br.readLine())!=null){		//generate NFA , the NFA is made up of four sub-NFAs
				if(line.startsWith("//")){
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
		Item item=new Item("RO",prodcs.get("RO").substring(0,1),prodcs.get("RO").substring(1));
		item.addStringToPre("#");
		
		ItemSet itemset=new ItemSet(0);
		itemset.addItem(item);
		HashSet<Item> items=itemset.getItemset();
		
		items=closure(items);
		cluster.add(itemset);
		
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
	
	public HashSet<Item> closure(HashSet<Item> itemset ){		//expand itemset and add pre-search-symbol
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
	
	public void closure_op(Item item,HashSet<Item> newItemset,HashSet<String> visited){			//******so tired!!!
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
					
					if(item1.getType()==3){
						closure_op(item1, newItemset, visited);	
					}
				}
			}
		}else{											//only need update pre-search-symbol , EG: S->.Sa,# | S->.M
			for(Item item2:newItemset){
				if(expect.equals(item2.getLeft())){
					if(item2.getType()==3){
						if(item2.addStringToPre(firstSet)){
							boolean update=false;
							for(Item item3:newItemset){
								if(item3.getType()==3&&item3.getLeft().equals(item2.next())){
									update=true;								//M->.N has existed
								}
							}
							if(update){
								closure_op(item2, newItemset, visited);			//calculate pre-symbol again 
							}							
						}
					}else{
						item2.addStringToPre(firstSet);
					}
				}
			}
		}
	}
	
	public String firstSet(Item item){	
		String after=item.afterExpect();
		HashSet<String> pres=item.getPres();
		String firstSet="";
		
		if(!pres.isEmpty()){
			String temp;
			for(String pre:pres){
				temp="";
				firstSet(after+pre,temp);
				firstSet+=temp;
			}
		}else{		
			firstSet(after,firstSet);
		}
		
		return firstSet;
	}
	
	public void firstSet(String str,String firstSet){
		if(str.length()==0){
			firstSet+="";
		}else if(!non_terminated_s.contains(str.substring(0,1))){
			firstSet+=str.substring(0,1);
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
	
}
