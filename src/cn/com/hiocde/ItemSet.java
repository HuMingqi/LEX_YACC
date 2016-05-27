package cn.com.hiocde;

import java.util.*;

/**
 * Include the set of items and map relations.
 * @author Hmqi
 * @see Item
 */
public class ItemSet {
	private Integer id;
	private HashSet<Item> items=new HashSet<Item>();					//***in fact , we should override hashCode and equals methods in Item class
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
		items.add(item);
	}
	
	public HashSet<Item> getItemset(){
		return items;
	}
	
	public void setItemset(HashSet<Item> items){
		this.items=items;
	}
	
	public void put(String ch,Integer id){		//non-multiExit
		map.put(ch,id);
	}
	
	/**
	 * Add one line into LRTable according to self map and reduce/acc item.
	 * @param lrTable
	 * @return True if add line successfully , meaning no s-r or r-r conflict.
	 */
	public boolean addLineTo(Map<Integer,HashMap<String,String>> lrTable ){
		HashMap<String,String> line=new HashMap<String,String>();
		
		for(String key:map.keySet()){
			line.put(key,map.get(key).toString());	//move into and goto
		}
		
		for(Item item:items){
			if(item.getType()==4){
				if(line.put("#","acc")!=null){					
					System.out.println("\nCONFLICT : NOT LR1 GRAMMAR ! PARSING STOPPED");
					System.out.println("ItemSet ID : "+id);
					return false;
				}
			}else if(item.getType()==2){
				Set<String> pres=item.getPres();
				for(String symbol:pres){
					if(line.put(symbol,item.getProdcId())!=null){
						System.out.println("\nCONFLICT : NOT LR1 GRAMMAR ! PARSING STOPPED");
						System.out.println("ItemSet ID : "+id);
						return false;
					}
				}				
			}
		}			
		
		lrTable.put(id,line);
		return true;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof ItemSet){
			ItemSet itemset=(ItemSet)obj;
			HashSet<Item> its=itemset.getItemset();			
			
			if(its.size()==items.size()){
				boolean flag=false;
				for(Item item1:its){
					flag=false;
					for(Item item2:items){
						if(item1.equals(item2)){
							flag=true;
						}
					}
					if(flag==false){
						return false;
					}
				}
				
				return true;
			}			
		}
		
		return super.equals(obj);
	}
	
	public void printSelf(){		
		System.out.println("\nItemSet ID : "+id);
		for(Item item:items){
			item.printSelf();
		}
	}
	
}
