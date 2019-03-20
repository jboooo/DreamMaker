package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP0104pl extends hproc{
	StringBuffer sa=null;
	public String action(String value)throws Throwable{
	sa=new StringBuffer();
	
	String name=getName();
	setValue("field1","執行WP0104PL");
	if("FROM_STATUS".equals(name)){
		FROM_STATUS();
		setValue("field1","執行預設值");
	}else if("trustor".equals(name)){   //委託人
		trustor();
		setValue("field1","執行委託人");
	}else if("fees".equals(name)){  //規費及請款設定
		fees();
	}else if("all".equals(name)){ //全選
		all();	
		setValue("field1","執行全選");
	}else if("clear".equals(name)){//全不選
		clear();
		setValue("field1","執行全不選");
	}else if("copy".equals(name)){//複製申請日
		copy();
		setValue("field1","執行申請日");
	}
	
	return value;
	}
	
	public String FROM_STATUS()throws Throwable{                       //預設
			// 將底層修改扭打開
			getButton(3).setEnabled(true);   
			getButton(4).setEnabled(true);  

		return "";
	}

	public String trustor()throws Throwable{           //委託人
		Hashtable ht=new Hashtable();
		StringBuffer sb_meid=new StringBuffer();
		StringBuffer sb_idkey=new StringBuffer();
		//String custid=getValue("custid");
		String[][]ret=getTableData("table1");
		String qpamanid=getValue("qpamanid");
			if(ret.length==0){
			message("沒有任何資料，無法設定委託人資料");
			return "";
			}
			for(int i=0;i<ret.length;i++){
				if(ret[i][0].equals("1")){
					sb_meid.append(ret[i][1].trim()+",");
					sb_idkey.append(ret[i][9].trim()+",");
				}
			}
			setValue("text5",sb_meid.toString());
			setValue("text4",sb_idkey.toString());
			sb_meid.setLength(sb_meid.length()-1);
			sb_idkey.setLength(sb_idkey.length()-1);
			ht.put("qpamanid",qpamanid);
		//	ht.put("custid",custid);
			ht.put("meid",sb_meid.toString());
			ht.put("idkey",sb_idkey.toString());
			
			showDialog("委託人","委託人",false,false,-1,-1,1030,600,ht);	
	
	
		return "";
	}
	public String fees()throws Throwable{                //規費
		Hashtable ht=new Hashtable();
		StringBuffer sb_meid=new StringBuffer();
		StringBuffer sb_idkey=new StringBuffer();
		String[][]ret=getTableData("table1");
			if(ret.length==0){
			message("沒有任何資料，無法設定規費及請款設定");
			return "";
			}
			String ofeededuct=getValue("ofeededuct");
			
			for(int i=0;i<ret.length;i++){
				if(ret[i][0].equals("1")){
					sb_meid.append(ret[i][1].trim()+",");
					sb_idkey.append(ret[i][9].trim()+",");
				}
			}

			sb_meid.setLength(sb_meid.length()-1);
			sb_idkey.setLength(sb_idkey.length()-1);
			
			ht.put("meid",sb_meid.toString());
			ht.put("idkey",sb_idkey.toString());
			ht.put("ofeededuct",ofeededuct);

			sa.append("\nmeid:"+sb_meid.toString());                                     //檢查
			sa.append("\nidkey:"+sb_idkey.toString());                                     //檢查
			sa.append("\nofeededuct:"+ofeededuct);                                     //檢查
						
			setValue("field1",sa.toString());                                     //檢查
			
			
			showDialog("規費及請款設定","規費及請款設定",false,false,-1,-1,1030,600,ht);	
	
		return "";
	}
	public String all()throws Throwable{                                //全選
		talk t=getTalk("TradeMark");
		String[][]ret=getTableData("table1");
			
		//檢核	
			if(ret.length==0){
			message("資料表沒有資料");
			return "";
			}
		//設定全選
			
			if(ret.length!=0){
				for(int i=0;i<ret.length;i++){
				ret[i][0]="1";	
				}
			}
		for(int i=0;i<ret.length;i++){
			//設定有優先權日期，才能修改優先權文件齊備，優先權補件期限
			String sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";
			String[][]ret_priodate=t.queryFromPool(sql);
								
			//沒有優先權，優先權文件齊備及優先權補件期限就不讀取值，也不能更動。						
			if(ret_priodate[0][0].equals("0")){
				ret[i][4]="";
				ret[i][5]="";
				setEditable("table1",i,4, false);
				setEditable("table1",i,5, false);
			}
			//在放入table1之前，將優先權的資料加入。
			else{

				//優先權文件齊備為是，優先權補件期限就設為""，優先權補件期限設為不能動
				if(ret[i][4].equals("1")){
					ret[i][5]="";
					setEditable("table1",i,5,false);
				}
				//優先權文件齊備為否，優先權補件期限設為不能動。
				else if(ret[i][4].equals("0")){
					setEditable("table1",i,5,false);
				}								
				}
			
		}//i迴圈結束
			
		setTableData("table1",ret);
		
		return "";
	}
	public String clear()throws Throwable{                                  //全不選
		// 可自定HTML版本各欄位的預設值與按鈕的動作 
		// 傳入值 value 
		talk t=getTalk("TradeMark");
		String[][]ret=getTableData("table1");
		//檢核	
			if(ret.length==0){
			message("資料表沒有資料");
			return "";
			}
		//設定只選輸入的那筆meid	
		for(int i=0;i<ret.length;i++){ 
			//全部都設為"0"
			ret[i][0]="0";
			//ret[i][1]等於meid的話，就設為"1"
			ret[i][1]=ret[i][1].trim();           //去除空白再比較							
			if(ret[i][1].equals(getValue("meid").trim())){
				ret[i][0]="1";
			}
		} //i迴圈結束
			
			
		for(int i=0;i<ret.length;i++){
			//設定有優先權日期，才能修改優先權文件齊備，優先權補件期限
			String sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";
			String[][]ret_priodate=t.queryFromPool(sql);
								
			//沒有優先權，優先權文件齊備及優先權補件期限就不讀取值，也不能更動。						
			if(ret_priodate[0][0].equals("0")){
				ret[i][4]="";
				ret[i][5]="";
				setEditable("table1",i,4, false);
				setEditable("table1",i,5, false);
			}
			//在放入table1之前，將優先權的資料加入。
			else{

				//優先權文件齊備為是，優先權補件期限就設為""，優先權補件期限設為不能動
				if(ret[i][4].equals("1")){
					ret[i][5]="";
					setEditable("table1",i,5,false);
				}
				//優先權文件齊備為否，優先權補件期限設為不能動。
				else if(ret[i][4].equals("0")){
					setEditable("table1",i,5,false);
				}								
				}
			
		}//i迴圈結束
		setTableData("table1",ret);
	
		return "";
	}
	public String copy()throws Throwable{                  //複制申請
	// 可自定HTML版本各欄位的預設值與按鈕的動作 
		// 傳入值 value 
		String[][]ret = getTableData("table1");
		talk t=getTalk("TradeMark");
		//檢核
		if(ret.length==0){
			message("資料表沒有資料");
			return "";
		}
		if(ret[0][3].length()==0){
			message("沒有設定第一筆日期");
			return "";
		}
		//將有打勾的的申請日與第一筆申請日的日期相同。
		for(int i=1;i<ret.length;i++){
			if(ret[i][0].equals("1")){
			ret[i][3]=ret[0][3].trim();
			}
		}//i迴圈結束
		for(int i=0;i<ret.length;i++){
			//設定有優先權日期，才能修改優先權文件齊備，優先權補件期限
			String sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";
			String[][]ret_priodate=t.queryFromPool(sql);
								
			//沒有優先權，優先權文件齊備及優先權補件期限就不讀取值，也不能更動。						
			if(ret_priodate[0][0].equals("0")){
				ret[i][4]="";
				ret[i][5]="";
				setEditable("table1",i,4, false);
				setEditable("table1",i,5, false);
			}
			//在放入table1之前，將優先權的資料加入。
			else{
				//優先權文件齊備為是，優先權補件期限就設為""，優先權補件期限設為不能動
				if(ret[i][4].equals("1")){
					ret[i][5]="";
					setEditable("table1",i,5,false);
				}
				//優先權文件齊備為否，優先權補件期限設為不能動。
				else if(ret[i][4].equals("0")){
					setEditable("table1",i,5,false);
				}								
			}
			
		}//i迴圈結束
		setTableData("table1",ret);
		return "";
	}	
}
