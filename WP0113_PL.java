package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP0113_PL extends hproc{
	public String action(String value)throws Throwable{
		// 可自定HTML版本各欄位的預設值與按鈕的動作 
		// 傳入值 value 
		String name=getName();
		if(name.equals("trustor")){
			trustor();
		}else if(name.equals("all")){
			all();
		}
		return value;
	}
	public void trustor()throws Throwable{           //委託人
		Hashtable ht=new Hashtable();
		String meid=getValue("meid");
		String idkey=getValue("idkey");

		ht.put("meid",meid);
		ht.put("idkey",idkey);
			
		showDialog("委託人2","",false,false,-1,-1,1030,600,ht);	
		return ;	
	}
	public void all()throws Throwable{   //全選
		String[][]ret=getTableData("procidTable");
		String idkey=getValue("idkey");
		//檢核	
		if(ret.length==0){
			message("資料表沒有資料");
			return ;
		}
		//設定全選
		if(ret.length!=0){
			for(int i=0;i<ret.length;i++){
				if(ret[i][1].equals(idkey)){
				setEditable("procidTable",i,0,false);
				}
				ret[i][0]="1";	
			}
		}

		setTableData("procidTable",ret);
		return ;
	}
	
	
	
}
