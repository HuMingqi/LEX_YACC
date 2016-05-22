package cn.com.hiocde;

public class Item {
	private String left;
	private String right;
	private int pos=0;
	private String preSearch;
	private int type;			//1-put_into 2-return 3-ready_return 4-acc
	
	public Item(String left,String right,String pre,int type){
		this.left=left;
		this.right=right;
		this.preSearch=pre;
		this.type=type;
	}
	
	public void replyMove(){
		++pos;
		if(pos==right.length()){
			if(right.charAt(pos-1)=='S'){
				type=4;
			}else{
				type=2;
			}
		}else{
			if(right.charAt(pos)>='A'&&right.charAt(pos)<='Z'){
				type=3;
			}else{
				type=1;
			}
		}
	}
}
