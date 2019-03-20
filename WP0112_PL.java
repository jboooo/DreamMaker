package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP0112_PL extends hproc{
	public String action(String value)throws Throwable{
		// 可自定HTML版本各欄位的預設值與按鈕的動作 
		// 傳入值 value 
		trustor();
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
}
