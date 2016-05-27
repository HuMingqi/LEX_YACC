package cn.com.hiocde;

import java.util.*;

/**
 * <tt>Item</tt> includes three keys : production , dot position , searching symbols
 * @author Hmqi
 *
 */
public class Item {
	private String prodcId;		//such as R0,R1,...
	private String left;
	private String right;		//if prodc is empty,set right as ""
	private int pos;			//point position
	private HashSet<String> preSearch=new HashSet<String>();
	private int type;			//1-shfit 2-reduce 3-ready_reduce 4-acc	
	
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
	
	public int getPos(){
		return pos;
	}
	
	public int getType(){
		return type; 
	}
	
	/**
	 * Dot pos can completely determine the type of item , forbid setting type directly. 
	 */
	public void setTypebyPos(){
		if(pos==right.length()){
			if(pos==0||!"ES".equals(left+right)){		//empty item
				type=2;
			}else{
				type=4;
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
	
	public void setPres(HashSet<String> pres){
		preSearch=pres;
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
	
	/**
	 * 
	 * @return return expected symbol after dot
	 */
	public String next(){				//expected next symbol
		if(type==1||type==3){
			return right.charAt(pos)+"";
		}else{
			return null;
		}		
	}
	
	/**
	 * 
	 * @return return the string after expected symbol , eg: (S,.Aab) , return "ab".
	 */
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
		item_copy.setPres((HashSet<String>) preSearch.clone());		
		return item_copy;
	}		
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Item){
			Item item=(Item)obj;
			if(item.getProdcId()==prodcId&&item.getPos()==pos&&item.getPres().hashCode()==preSearch.hashCode()){
				return true;						//***maybe hashcode repeat , so not always correct
			}else{
				return false;
			}
		}
		
		return super.equals(obj);
	}
	
	public void printSelf(){
		System.out.println("Item : ("+left+"->"+right+","+pos+","+this.preToString()+")");
	}
	
}
