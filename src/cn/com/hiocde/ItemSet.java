package cn.com.hiocde;

import java.util.*;

public class ItemSet {
	private Integer id;
	private HashSet<Item> itemset=new HashSet<Item>();
	private HashMap<String , Integer> map=new HashMap<String,Integer>();
	
	public ItemSet(Integer id){
		this.id=id;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void addItem(Item item){
		itemset.add(item);
	}
	
	public HashSet<Item> getItemset(){
		return itemset;
	}
	
	public void put(String ch,Integer id){		//non-multiExit
		map.put(ch,id);
	}
	
	public void addLineTo(Map<Integer,Map<String,String>> lrTable ){
		Map<String,String> line=new HashMap<String,String>();
		
		for(Item item:itemset){
			if(item.getType()==4){
				line.put("#","acc");						
			}else if(item.getType()==2){
				Set<String> pre=item.getPres();
				for(String symbol:pre){
					line.put(symbol,item.getProdcId());
				}				
			}
		}
		
		for(String key:map.keySet()){
			line.put(key,map.get(key).toString());			
		}
		
		lrTable.put(id,line);
	}
}
