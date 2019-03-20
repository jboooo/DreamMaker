package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP0113_PL extends hproc{
	public String action(String value)throws Throwable{
		// �i�۩wHTML�����U��쪺�w�]�ȻP���s���ʧ@ 
		// �ǤJ�� value 
		String name=getName();
		if(name.equals("trustor")){
			trustor();
		}else if(name.equals("all")){
			all();
		}
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
	public void all()throws Throwable{   //����
		String[][]ret=getTableData("procidTable");
		String idkey=getValue("idkey");
		//�ˮ�	
		if(ret.length==0){
			message("��ƪ�S�����");
			return ;
		}
		//�]�w����
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
