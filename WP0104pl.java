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
	setValue("field1","����WP0104PL");
	if("FROM_STATUS".equals(name)){
		FROM_STATUS();
		setValue("field1","����w�]��");
	}else if("trustor".equals(name)){   //�e�U�H
		trustor();
		setValue("field1","����e�U�H");
	}else if("fees".equals(name)){  //�W�O�νдڳ]�w
		fees();
	}else if("all".equals(name)){ //����
		all();	
		setValue("field1","�������");
	}else if("clear".equals(name)){//������
		clear();
		setValue("field1","���������");
	}else if("copy".equals(name)){//�ƻs�ӽФ�
		copy();
		setValue("field1","����ӽФ�");
	}
	
	return value;
	}
	
	public String FROM_STATUS()throws Throwable{                       //�w�]
			// �N���h�ק�ᥴ�}
			getButton(3).setEnabled(true);   
			getButton(4).setEnabled(true);  

		return "";
	}

	public String trustor()throws Throwable{           //�e�U�H
		Hashtable ht=new Hashtable();
		StringBuffer sb_meid=new StringBuffer();
		StringBuffer sb_idkey=new StringBuffer();
		//String custid=getValue("custid");
		String[][]ret=getTableData("table1");
		String qpamanid=getValue("qpamanid");
			if(ret.length==0){
			message("�S�������ơA�L�k�]�w�e�U�H���");
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
			
			showDialog("�e�U�H","�e�U�H",false,false,-1,-1,1030,600,ht);	
	
	
		return "";
	}
	public String fees()throws Throwable{                //�W�O
		Hashtable ht=new Hashtable();
		StringBuffer sb_meid=new StringBuffer();
		StringBuffer sb_idkey=new StringBuffer();
		String[][]ret=getTableData("table1");
			if(ret.length==0){
			message("�S�������ơA�L�k�]�w�W�O�νдڳ]�w");
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

			sa.append("\nmeid:"+sb_meid.toString());                                     //�ˬd
			sa.append("\nidkey:"+sb_idkey.toString());                                     //�ˬd
			sa.append("\nofeededuct:"+ofeededuct);                                     //�ˬd
						
			setValue("field1",sa.toString());                                     //�ˬd
			
			
			showDialog("�W�O�νдڳ]�w","�W�O�νдڳ]�w",false,false,-1,-1,1030,600,ht);	
	
		return "";
	}
	public String all()throws Throwable{                                //����
		talk t=getTalk("TradeMark");
		String[][]ret=getTableData("table1");
			
		//�ˮ�	
			if(ret.length==0){
			message("��ƪ�S�����");
			return "";
			}
		//�]�w����
			
			if(ret.length!=0){
				for(int i=0;i<ret.length;i++){
				ret[i][0]="1";	
				}
			}
		for(int i=0;i<ret.length;i++){
			//�]�w���u���v����A�~��ק��u���v�����ơA�u���v�ɥ����
			String sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";
			String[][]ret_priodate=t.queryFromPool(sql);
								
			//�S���u���v�A�u���v�����Ƥ��u���v�ɥ�����N��Ū���ȡA�]�����ʡC						
			if(ret_priodate[0][0].equals("0")){
				ret[i][4]="";
				ret[i][5]="";
				setEditable("table1",i,4, false);
				setEditable("table1",i,5, false);
			}
			//�b��Jtable1���e�A�N�u���v����ƥ[�J�C
			else{

				//�u���v�����Ƭ��O�A�u���v�ɥ�����N�]��""�A�u���v�ɥ�����]�������
				if(ret[i][4].equals("1")){
					ret[i][5]="";
					setEditable("table1",i,5,false);
				}
				//�u���v�����Ƭ��_�A�u���v�ɥ�����]������ʡC
				else if(ret[i][4].equals("0")){
					setEditable("table1",i,5,false);
				}								
				}
			
		}//i�j�鵲��
			
		setTableData("table1",ret);
		
		return "";
	}
	public String clear()throws Throwable{                                  //������
		// �i�۩wHTML�����U��쪺�w�]�ȻP���s���ʧ@ 
		// �ǤJ�� value 
		talk t=getTalk("TradeMark");
		String[][]ret=getTableData("table1");
		//�ˮ�	
			if(ret.length==0){
			message("��ƪ�S�����");
			return "";
			}
		//�]�w�u���J������meid	
		for(int i=0;i<ret.length;i++){ 
			//�������]��"0"
			ret[i][0]="0";
			//ret[i][1]����meid���ܡA�N�]��"1"
			ret[i][1]=ret[i][1].trim();           //�h���ťզA���							
			if(ret[i][1].equals(getValue("meid").trim())){
				ret[i][0]="1";
			}
		} //i�j�鵲��
			
			
		for(int i=0;i<ret.length;i++){
			//�]�w���u���v����A�~��ק��u���v�����ơA�u���v�ɥ����
			String sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";
			String[][]ret_priodate=t.queryFromPool(sql);
								
			//�S���u���v�A�u���v�����Ƥ��u���v�ɥ�����N��Ū���ȡA�]�����ʡC						
			if(ret_priodate[0][0].equals("0")){
				ret[i][4]="";
				ret[i][5]="";
				setEditable("table1",i,4, false);
				setEditable("table1",i,5, false);
			}
			//�b��Jtable1���e�A�N�u���v����ƥ[�J�C
			else{

				//�u���v�����Ƭ��O�A�u���v�ɥ�����N�]��""�A�u���v�ɥ�����]�������
				if(ret[i][4].equals("1")){
					ret[i][5]="";
					setEditable("table1",i,5,false);
				}
				//�u���v�����Ƭ��_�A�u���v�ɥ�����]������ʡC
				else if(ret[i][4].equals("0")){
					setEditable("table1",i,5,false);
				}								
				}
			
		}//i�j�鵲��
		setTableData("table1",ret);
	
		return "";
	}
	public String copy()throws Throwable{                  //�ƨ�ӽ�
	// �i�۩wHTML�����U��쪺�w�]�ȻP���s���ʧ@ 
		// �ǤJ�� value 
		String[][]ret = getTableData("table1");
		talk t=getTalk("TradeMark");
		//�ˮ�
		if(ret.length==0){
			message("��ƪ�S�����");
			return "";
		}
		if(ret[0][3].length()==0){
			message("�S���]�w�Ĥ@�����");
			return "";
		}
		//�N�����Ī����ӽФ�P�Ĥ@���ӽФ骺����ۦP�C
		for(int i=1;i<ret.length;i++){
			if(ret[i][0].equals("1")){
			ret[i][3]=ret[0][3].trim();
			}
		}//i�j�鵲��
		for(int i=0;i<ret.length;i++){
			//�]�w���u���v����A�~��ק��u���v�����ơA�u���v�ɥ����
			String sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";
			String[][]ret_priodate=t.queryFromPool(sql);
								
			//�S���u���v�A�u���v�����Ƥ��u���v�ɥ�����N��Ū���ȡA�]�����ʡC						
			if(ret_priodate[0][0].equals("0")){
				ret[i][4]="";
				ret[i][5]="";
				setEditable("table1",i,4, false);
				setEditable("table1",i,5, false);
			}
			//�b��Jtable1���e�A�N�u���v����ƥ[�J�C
			else{
				//�u���v�����Ƭ��O�A�u���v�ɥ�����N�]��""�A�u���v�ɥ�����]�������
				if(ret[i][4].equals("1")){
					ret[i][5]="";
					setEditable("table1",i,5,false);
				}
				//�u���v�����Ƭ��_�A�u���v�ɥ�����]������ʡC
				else if(ret[i][4].equals("0")){
					setEditable("table1",i,5,false);
				}								
			}
			
		}//i�j�鵲��
		setTableData("table1",ret);
		return "";
	}	
}
