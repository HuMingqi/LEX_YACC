package cn.com.hiocde;

import java.util.*;

public class Item {
	private String prodcId;	//such as R0,R1,...
	private String left;
	private String right;		//if prodc is empty,set right as ""
	private int pos;			//point position
	private HashSet<String> preSearch=new HashSet<String>();
	private int type;			//1-put_into 2-return 3-ready_return 4-acc	
	
	public Item(String prodcId,String left,String right){
		this.prodcId=prodcId;
		this.left=left;
		this.right=right;
		this.pos=0;
		setTypebyPos();
	}
	
	public Item(String prodcId,String left,String right,int pos){			//used when clone
		this.prodcId=prodcId;
		this.left=left;
		this.right=right;
		this.pos=pos;
		setTypebyPos();
	}
	
	public String getProdcId(){
		return prodcId;
	}
	
	public String getLeft(){
		return left;
	}
	
	public int getType(){
		return type; 
	}
	
	public void setTypebyPos(){
		if(pos==right.length()){
			if(right.charAt(pos-1)=='S'){
				type=4;
			}else{
				type=2;
			}
		}else{
			if(ItemsCluster.non_terminated_s.contains(right.charAt(pos)+"")){
				type=3;
			}else{
				type=1;
			}
		}
	}
	
	public HashSet<String> getPres(){
		return preSearch;
	}
	
	public boolean addStringToPre(String symbolStr){			//effective add return true
		boolean effective=false;
		for(int i=0;i<symbolStr.length();++i){
			if(preSearch.add(symbolStr.charAt(i)+"")){
				effective=true;
			}
		}		
		return effective;
	}
	
	public String preToString(){
		String pres="";
		for(String pre:preSearch){
			pres+=pre;
		}
		return pres;
	}
	
	public String next(){				//expected next symbol
		if(type!=4&&type!=2){
			return right.charAt(pos)+"";
		}else{
			return null;
		}		
	}
	
	public String afterExpect(){		//eg: S->.Aab , return ab
		if(type==3){
			return right.substring(pos+1);
		}else{
			return null;
		}
	}
	
	public void replyMove(){			
		++pos;
		setTypebyPos();
	}
	
	public Item clone(){
		Item item_copy=new Item(prodcId, left, right,pos);
		HashSet<String> pres=item_copy.getPres();
		pres=(HashSet<String>) preSearch.clone();		
		return item_copy;
	}		
}
