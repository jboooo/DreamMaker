package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP0112_PL extends hproc{
	public String action(String value)throws Throwable{
		// �i�۩wHTML�����U��쪺�w�]�ȻP���s���ʧ@ 
		// �ǤJ�� value 
		trustor();
		return value;
	}
	public void trustor()throws Throwable{           //�e�U�H
		Hashtable ht=new Hashtable();
		String meid=getValue("meid");
		String idkey=getValue("idkey");

		ht.put("meid",meid);
		ht.put("idkey",idkey);
			
		showDialog("�e�U�H2","",false,false,-1,-1,1030,600,ht);
		
		return ;	
	}
}
