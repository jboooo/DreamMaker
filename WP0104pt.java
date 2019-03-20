package dai;
import jcx.jform.bTransaction;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  
import java.util.Date;

public class WP0104pt extends bTransaction {
	StringBuffer sa=null;
	talk t=null;

	public boolean action(String value)throws Throwable {
		sa=new StringBuffer();
		t=getTalk("TradeMark");
		if ("�d��".equals(value)) {
			QUERY();
		} else if ("�ק�".equals(value)) {
			if(UPDATE()){
				QUERY();
			}
		} else if ("�R��".equals(value)) {
			delete();
			QUERY();
		}

		return false;
	}

//�d��
	public void QUERY()throws Throwable {
		sa.append("\n-------���X�ӽ�.�d��--------");                            //�ˬd

		Hashtable ht=new Hashtable();
		Vector vt=new Vector();
		talk t=getTalk("TradeMark");
		talk wp=getTalk("wp");
//		String rcvid=getQueryValue("rcvid").trim();
		String rcvid="";
		String meid="";
		String idkey=getQueryValue("idkey").toUpperCase().trim();
		setValue("idkey",idkey);
		String sqlWhere="";

//		if(rcvid.length()==0 & meid.length()==0 & idkey.length()==0) {
//			message("�п�J�d�߱���");
//			return ;
//		}
//�j�M����
//		if(rcvid.length()>0) {
//			sqlWhere+=" and b.rcvid ='"+rcvid+"'";
//		}
		if(getQueryValue("idkey").length()>0) {
			idkey=getQueryValue("idkey").trim();			
			sqlWhere+= " and b.idkey ='"+idkey+"'";
		}else {
			message("�п�J�d�߱���");
			return ;
		}
		
//		if(idkey.length()>0) {
//			sqlWhere+= " and b.idkey ='"+idkey+"'";
//		}

		String sql="select a.meid,b.plsmanid,a.appydate"
		           +",b.debitchk,b.ltdate,b.debitdate,b.debitid,b.procid,b.rcvid"
		           +",a.othid,a.debittype,a.usertype,a.specialtitle,a.janloc,a.janid,a.debittitle"
		           +",c.pamanid,ofeededuct"
		           +" from wptmapply a,wptlproc b,wptlapman c"
		           +" where a.meid=b.meid and b.meid=c.meid"
		           +" and b.procid ='0104'"
		           + sqlWhere
		           + " order by pamanid asc";
		sa.append("\n���"+sql);                                   //�ˬd

		String[][]ret=t.queryFromPool(sql);
		if(ret.length==0) {
			message("�S���o�����");
			return ;
		}
		meid=ret[0][0].trim();
		setValue("meid",meid);
		//�e�U�Hid��m�W
		sql="select unidebitname from wptmcust where custid='"+ret[0][1]+"'";
		String[][]struniname=wp.queryFromPool(sql);
		if(struniname.length>0) {
			ret[0][1]=struniname[0][0].trim()+"\n";
		}
		setValue("plsmanid",ret[0][1]);
		setValue("appydate",dateformat(ret[0][2]));
		setValue("debitchk",ret[0][3]);
		setValue("ltdate",dateformat(ret[0][4]));
		setValue("debitdate",dateformat(ret[0][5]));
		setValue("debitid",ret[0][6]);
		setValue("procid",ret[0][7]);  //����
		setValue("rcvid",ret[0][8]);   //����
		setValue("othid",ret[0][9]);//����
		setValue("debittype",ret[0][10]);//����
		setValue("usertype",ret[0][11]);//����
		setValue("specialtitle",ret[0][12]);//����
		setValue("janloc",ret[0][13]);//����
		setValue("janid",ret[0][14]);//����
		setValue("debittitle",ret[0][15]);//����

		//�ӽФH���h�����
		String str="";
		StringBuffer str_qpamanid=new StringBuffer();                      //�ӽФHid��U��
		String str1="z";
		for (int i=0; i<ret.length; i++) {
			sql="select unioriginname from wptmapman where pamanid='"+ret[i][16]+"'";  //id��W�r
			String[][]strname=wp.queryFromPool(sql);
			if(strname.length>0) {
				str+=strname[0][0].trim()+"\n";
			} else {
				str+=ret[i][16]+"\n";
			}
			str_qpamanid.append("#"+ret[i][16].trim()+"#,");
		}
		str_qpamanid.setLength(str_qpamanid.length()-1);
		setValue("qpamanid",str_qpamanid.toString());               //�h���ӽФH�A�n�Ǩ�U�@�Ӫ��C
		setValue("pamanid",str);
		setValue("ofeededuct",ret[0][17]);//�W�O��K���B

		sql = "select DISTINCT '',a.meid,a.markname,a.appydate,'','',a.appyid"
		      +",b.rcvno,b.rcvid,b.idkey,'',(select LEFT(pamanid,8) FROM WPTLAPMAN WHERE MEID='"+meid+"' for xml path(''))"
		      +" from wptmapply a"
		      +" JOIN wptlproc b ON A.MEID=B.MEID"
		      +" JOIN wptlapman c ON A.MEID=C.MEID"
		      +" where b.procid ='0104'"		//�{�Ǭ�0104
		      +" and c.pamanid in(select pamanid from wptlapman where meid='"+meid+"')"  //�ӽФH�ۦP
		      +" and a.plsmanid in(select plsmanid from wptlproc where meid='"+meid+"')"//�e�U�H�s���ۦP
		      +" and ISNULL(a.appydate,'') in (select ISNULL(appydate,'') from wptmapply where meid='"+meid+"')"//�ӽФ���ۦP
		      +" and left(b.rcvid,10)=left("+ret[0][8].trim()+",10)" //�e10�X�ۦP
		      +" order by a.meid";
		String[][] ret1=t.queryFromPool(sql);
		sa.append("\n���sql:\n"+sql);			                        //�ˬd


		//�p�GŪ�����ơA�N���X�C
//		if(ret1.length==0) {
//			message("�t�ΨS�����");
//			return ;
//		}
//		sa.append("\nret1������:"+ret1.length);                                 //�ˬd

		//�]�wret1[i][1]�O�_���ġC
		for(int i=0; i<ret1.length; i++) {
			ret1[i][0]="0";
			ret1[i][1]=ret1[i][1].trim();           //�h���ťզA���
			if(ret1[i][1].equals(meid)) {
				ret1[i][0]="1";
			}
		}//i�j�鵲��
//		sa.append("\n���ī�ret1������:"+ret1.length);                                 //�ˬd

		//�]�w�N�����Ī���Ʀs�Jvt
		for(int i=0; i<ret1.length; i++) {
			if("1".equals(ret1[i][0])) {
				vt.add(new String[] { ret1[i][0],ret1[i][1],ret1[i][2],ret1[i][3],
				                      ret1[i][4],ret1[i][5],ret1[i][6],ret1[i][7],ret1[i][8],ret1[i][9],ret1[i][10],ret1[i][11]
				                    });
//				sa.append("\n�N���Ī���Ʀs�JVT:"+vt.size());                                 //�ˬd

			}
		}//i�j�鵲��
//		sa.append("\nret1.length:"+ret1.length);                                 //�ˬd

		//�]�w�N�S�����Ī���Ʀs�Jvt
		for(int i=0; i<ret1.length; i++) {
//			sa.append("\n�N�S���Ī���Ʀs�JVT:��");                                   //�ˬd
			if("0".equals(ret1[i][0])) {
				vt.add(new String[] { ret1[i][0],ret1[i][1],ret1[i][2],ret1[i][3],
				                      ret1[i][4],ret1[i][5],ret1[i][6],ret1[i][7],ret1[i][8],ret1[i][9],ret1[i][10],ret1[i][11]
				                    });
			}
		}//i�j�鵲��
//		setValue("field1",sa.toString());                                   //�ˬd

		//�Nvt����Ʃ�^ret1
		ret1=(String[][])vt.toArray(new String[0][0]);


		//�[�J�ɥ�дڡC
		for(int i=0; i<ret1.length; i++) {
			//�ɥ�д�
			sql="select sdtid from wptlproc where idkey='"+ret1[i][9]+"'";
			String[][]ret_sdtid=t.queryFromPool(sql);
			sa.append("\n�ɥ�д�:"+sql);                                      //�ˬd
			if(ret_sdtid[0][0]!=null) {
				if((ret_sdtid[0][0].indexOf("T04"))>=0) {
					ret1[i][11]="1";
				}
			}
		} //�j�鵲��

		setTableData("table1",ret1);
		setEditable("ofeededuct",true);
		setValue("field1",sa.toString());                                   //�ˬd
		
		//��findate�����������
		if(!findateToOpen(idkey)){
			setEditableField();//todo
		}else{
			setEditableField();//todo
			setEditable("ofeededuct",true);
			setEditable("sbndate",true);
			setEditable("table1",true);
			
			//�[�J�u���v����ơC
			for(int i=0; i<ret1.length; i++) {
				
				setEditable("table1",i,3,true); //�ӽФ��
				setEditable("table1",i,6,true); //�ӽи��X
				setEditable("table1",i,7,true); //���ڸ��X
				setEditable("table1",i,8,true); //�ɥ�д�
				
				//�]�w���u���v����A�~��ק��u���v�����ơA�u���v�ɥ����
				sql="select count(*) from wptlprior where meid='"+ret1[i][1].trim()+"' and priogiveup<>1";
				String[][]ret_priodate=t.queryFromPool(sql);
				sa.append("\n�d�߬O�_���u���v:"+sql);                                      //�ˬd
				
				if(ret_priodate[0][0].equals("0")) {
					setEditable("table1",i,4, false);
					setEditable("table1",i,5, false);
				} else {
					//�b��Jtable1���e�A�N�u���v����ƥ[�J�C
					sql = "select priochk1,priotxtdate from wptlproposal where meid='"+ret1[i][1]+"'";
					String[][]ret_pri=t.queryFromPool(sql);
					sa.append("\n�u���v����ƥ[�J:"+sql);                                      //�ˬd				
					if(ret_pri.length>0) {
						ret1[i][4]=ret_pri[0][0].trim();
						ret1[i][5]=ret_pri[0][1].trim();
					}
					//�u���v�����Ƭ��O�A�u���v�ɥ�����N�]��""�A
					if(ret1[i][4].equals("1")) {
						ret1[i][5]="";
						setEditable("table1",i,5,false);
					} else if(ret1[i][4].equals("0")) {
						setEditable("table1",i,5,false);
					}
				}
			}
		
		}		
		
		
		
		
		return ;

	}//�d�ߵ���

//�ק�
	public boolean UPDATE()throws Throwable {
		sa.append("-------���X�ӽСA�ק�--------");                            //�ˬd
		Vector vSQL = new Vector();
		Vector vSQL_wp=new Vector();
		StringBuffer sh=new StringBuffer();

		talk t=getTalk("TradeMark");
		talk wp=getTalk("wp");
		String idkey=getValue("idkey");
		String mdate = datetime.getToday("YYYY/mm/dd");           //Wrkdate���o�@�~���
		String mtime = datetime.getTime("h:m:s");
		String wrkdate = mdate+ " " + mtime; 			   //���oWrkdate
		String mUser=getUser();
		String sql="";                                                   //���ouser

		String debittitle=getValue("debittitle").trim(); // debittitle
		String debittype=getValue("debittype").trim(); // debittype
		String usertype=getValue("usertype").trim(); // usertype
		String specialtitle=getValue("specialtitle").trim(); // specialtitle
		String janloc=getValue("janloc").trim(); // janloc
		String janid=getValue("janid").trim(); // janid
		String appydate=getValue("appydate").trim(); // appydate�ӽФ��
		String ofeededuct=getValue("ofeededuct").trim(); //�W�O��K���B

		String[][] ret = getTableData("table1"); //ret���o���

		String prio_idkey="";
		String ing_idkey="";

		//�ˮ֬O�_�j��32767

		int int_ofeededuct = Integer.parseInt(ofeededuct);

		if((32767-int_ofeededuct)<0) {
			message("�W�O��K���B���i�j��32767");
			return false;
		}

		if(!findateToOpen(idkey)){
			message("�w�g�����A���i�A�ק�");
			return false;
		}

//table1���}�l
		for(int i=0; i<ret.length; i++) {
			if(ret[i][0].equals("1")) {
				if(ret[i][6].trim().length()==0 && ret[i][3].trim().length()==0 && ret[i][7].length()==0){
			
				} else if((ret[i][6].trim()).length()>0 && (ret[i][3].trim()).length()>0 && ret[i][7].length()>0) {
			
				}else{
					message("���ӽФ���A�ӽи��X�Φ��ڸ��X���n�P�ɦ��ΦP�ɨS���~��s��");
					return false;	
				}

//wptmapply ��s�D��appydate��appyid
				sql="update wptmapply set "
				    +"appydate="+noDateToNull(ret[i][3].trim())+","
				    +"appyid='"+ret[i][6].trim()+"'"
				    +" where meid='"+ret[i][1].trim()+"'";
				vSQL.add(sql);
				sh.append("\nwptmapply ��s�D��:\n"+sql);


//�ק�ץ�{�Ǭ�����

				//���o�ӿ���qwptmrcv�ɡA����Omeid�C���@���C
				sql="select wrkman from wptlproc where idkey='"+ret[i][9].trim()+"'";
				String[][]str_wrkman=t.queryFromPool(sql);
//				sh.append("\n���o�ӿ��wrkman,"+str_wrkman[0][0]+"\n");
				String wrkman="";
				if(str_wrkman.length>0) {
					wrkman=str_wrkman[0][0];
				}
				//���olawdate�k�w�����M���w���
				sql="select lawdate,asndate from wptlproc where idkey='"+ret[i][9].trim()+"'";   

				String[][]str_date=t.queryFromPool(sql);
				String strlawdate=str_date[0][0].trim();
				String strasndate=str_date[0][1].trim();
				
				//�ק�delaymk����
				String delaymk="0";                                                     
				if(strlawdate.length()==0 && strasndate.length()==0) {
					delaymk="0";
				} else if(ret[i][3].compareTo(strlawdate)>0 || ret[i][3].compareTo(strasndate)>0) {
					delaymk="1";
				}
				
				//��¾�ȥN�z��
				String jobmark="";
				if(wrkman.equals(mUser)) {                                              
					jobmark="";
				} else {
					jobmark="*";
				}

				//�{�ǧ�����
				String findate=ret[i][3].trim();
				//�ɥ�д�
				String sdtid="";                    
				if("1".equals(ret[i][11]) & (!"".equals(ret[i][4])) ) { //���ɥ���t04
					sdtid="T03,T04";
				} else if(!"".equals(ret[i][4])) { //���u���vt03
					sdtid="T03";
				} else if("1".equals(ret[i][11])) {
					sdtid="T04";
				}
				//�ץ�{����sql
				sql="update wptlproc set "
				    +"jobmark ='"+jobmark.trim()+"',"                                               //��¾�ȥN�z��
				    +"findate ="+noDateToNull(findate.trim())+","                          //���{�ǧ�����
				    +"wrkdate ="+noDateToNull(wrkdate)+","
				    +"delaymk ='"+delaymk+"',"                          //�O�����O
				    +"appydate="+noDateToNull(ret[i][3].trim())+","
				    +"sbmdate="+noDateToNull(ret[i][3].trim())+","                    //���եΡC
				    +"rcvno='"+ret[i][7].trim()+"',"
				    +"appyid='"+ret[i][6].trim()+"',"
				    +"specialtitle='"+specialtitle+"',"
				    +"janloc='"+janloc+"',"
				    +"janid='"+janid+"',"
				    +"ofeededuct='"+ofeededuct+"',"
				    +"sdtid='"+sdtid+"'"                            //�ɥ�д�
				    +" where idkey='"+ret[i][9].trim()+"'";
				sh.append("\n�ץ�{����\n"+sql);
				vSQL.add(sql);

				//�ץ�{�Ǹg��ӿ���O����,�Y�s�ӿ���h�g�J��
				if(jobmark.length()>0) {

					sql="insert into wptlproc_wrkman(GCkey,IDkey,meid,procWrkman,modDateTime,state)"
					    +" VALUES ('"+getGcKey()+"','"+ret[i][9].trim()+"','"+ret[i][1].trim()+"','"+mUser+"',"+noDateToNull(wrkdate)+",'MOD')";
					sh.append("\n�ץ�{�Ǹg��ӿ���O����,�Y�s�ӿ���h�g�J��\n"+sql);
					vSQL.add(sql);
				}
//�W�[�ӽФ��{��:

				//�j�Ming_idKey�Ӧ�wptlproposal,����Omeid�A
				//�T�{�o����ƥH�e���S���s�L�A���s�L�N��update,�S���N��insert into
				sql="select ing_idkey from Wptlproposal where meid='"+ret[i][1].trim()+"'";
//				sql="select ing_idKey from wptlproc where meid='"+ret[i][1].trim()+"' and procid='010D'";
				String[][]ret_010D=t.queryFromPool(sql);
				if(ret_010D.length>0 && ret_010D[0][0].length()>0) {
					sql="update wptlproc set "
					    +"jobmark='"+jobmark+"',"
					    +"meid='"+ret[i][1].trim()+"',"
					    +"caseid='01',"
					    +"procid='010D',"
					    +"findate=null,"
					    +"rcvid='"+ret[i][8].trim()+"',"
					    +"wrkman='"+wrkman+"',"
					    +"wrkdate="+noDateToNull(wrkdate)+","
					    +"ltdate=null,"
					    +"debitid='null',"
					    +"debitdate=null,"
					    +"debitchk=0,"
					    +"lawdate=null,"
					    +"asndate=null,"
					    +"dbbouns=1"
					    +" where idkey='"+ret_010D[0][0].trim()+"'";

					ing_idkey=ret_010D[0][0].trim();                  //�W�[�έק�wptlpropsal�n�Ψ�C
					sa.append("\n�ӽФ��{��010D:\n"+sql);	                       //�ˬd
					sa.append("\ning_idkey:"+ing_idkey);	                //�ˬd

				} else {

					idkey=getIdkey();     //�e���A�[�WPC�Y�i

					sql="insert into wptlproc("
					    +"jobmark,meid,caseid,procid,findate,"
					    +"rcvid,wrkman,wrkdate,ltdate,debitid,"
					    +"debitdate,debitchk,IDKey,lawdate,asndate,"
					    +"dbbouns) values('"
					    +jobmark+"','"+ret[i][1].trim()+"','01','010D',null,'"
					    +ret[i][8].trim()+"','"+wrkman+"',"+noDateToNull(wrkdate)+",null,'null',null,0,'"
					    +idkey+"',null,null,1)";

					ing_idkey=idkey;                  //�W�[�έק�wptlpropsal�n�Ψ�C
					sa.append("\n�ӽФ��{��010D:\n"+sql);	                       //�ˬd
					sa.append("\ning_idkey:"+ing_idkey);	                          //�ˬd

				}
				vSQL.add(sql);
//�[�i���u���v���j�{��:

				//���u���ɥ����(wptmapply.priotxtdate <> null)�B�u���v�����Ƭ��_(wptmapply.priochk1=false)��
				String priochk1=ret[i][4].trim();	                                //���o�u���v������
				sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";//���o�u���v���
				String[][]sql_priodate=t.queryFromPool(sql);
				sa.append("\n���o�u���v���:\n"+sql);

				//�P�_�u���v���(��)and�u���v������(�O)
				if(sql_priodate.length >0 && priochk1.equals("1")) {
					sql="select prio_idkey from wptlproposal where meid='"+ret[i][1].trim()+"'";
					String[][]ret_prio=t.queryFromPool(sql);

					//�P�_���u���v�O�_���s�L
					if(ret_prio.length>0 && ret_prio[0][0].length()>0) {
						//�R���{�Ǥ���prio_idkey:
						sql="update wptlproposal set prio_idkey='' where meid='"+ret[i][1].trim()+"'";
						vSQL.add(sql);

						sa.append("\n�R��wptlproc:\n"+sql+"\n");                    //�ˬd
						//��prio_idkey�R��wptlproc
						sql="delete from wptlproc where idkey='"+ret_prio[0][0]+"'";
						vSQL.add(sql);

						sa.append("\n�R��wptlproc:\n"+sql+"\n");                    //�ˬd
					}
				}

				//�P�_�u���v���(��)and�u���v������(�L)
				if(sql_priodate.length >0 && priochk1.equals("0")) {
					//���o�u���v�ɥ����
					String lawdate=ret[i][5].trim();								
					if(lawdate.length()==0) {
						message("�u���v�ɥ��������g");
						return false;
					}
					//���oAsndate���,lawdate���-5��
					String []tmp  = lawdate.split(" ");                             
					String data = tmp[0].trim();
					data=convert.replace(data,"-","");
					String asndate=datetime.dateAdd(data,"d",-5);//����Ѽƪ��վ�
					asndate=convert.FormatedDate(asndate,"/");
					
					//�j�Mprio_idkey�Ӧ�wptlproposal,����Omeid�A
					//�T�{�o����ƥH�e���S���s�L�A���s�L�N��update,�S���N��insert into
					sql="select prio_idkey from wptlproposal where meid='"+ret[i][1].trim()+"'";
					String[][]ret_prio=t.queryFromPool(sql);

					sa.append("\n�[�i���u���v���j�{��:���s�L�N��update,�S���N��insert into\n"+sql);
					if(ret_prio.length>0 && ret_prio[0][0].length()==12) {           //�P�_���u���v�O�_���s�L
						sql="update wptlproc set "
						    +"jobmark='"+jobmark+"',"
						    +"meid='"+ret[i][1].trim()+"',"
						    +"caseid='01',"
						    +"procid='010C',"
						    +"findate=null,"
						    +"rcvid='"+ret[i][8].trim()+"',"
						    +"wrkman='"+wrkman+"',"
						    +"wrkdate="+noDateToNull(wrkdate)+","
						    +"ltdate=null,"
						    +"debitid='null',"
						    +"debitdate=null,"
						    +"debitchk=0,"
						    +"lawdate="+noDateToNull(lawdate)+","
						    +"asndate="+noDateToNull(asndate)+","
						    +"dbbouns=1"
						    +" where idkey='"+ret_prio[0][0].trim()+"'";

						prio_idkey=ret_prio[0][0].trim();
						sa.append("\n�[���u���v���010C:\n"+sql);	                       //�ˬd

					} else {

						idkey=getIdkey();

						sql="insert into wptlproc("
						    +"jobmark,meid,caseid,procid,findate,"
						    +"rcvid,wrkman,wrkdate,ltdate,debitid,"
						    +"debitdate,debitchk,IDKey,lawdate,asndate,"
						    +"dbbouns)"
						    +" values('"
						    +jobmark+"','"+ret[i][1].trim()+"','01','010C',null,'"
						    +ret[i][8].trim()+"','"+wrkman+"',"+noDateToNull(wrkdate)+",null,'null',null,0,'"
						    +idkey+"',"+noDateToNull(lawdate)+","+noDateToNull(asndate)+",1)";

						prio_idkey=idkey.trim();

						sa.append("\n�[���u���v���010C:\n"+sql);	                       //�ˬd
					} //�����s�u���v
					vSQL.add(sql);
				}


//�O���i���u���v���j�{�ǳ渹: Prio_idKey,�W�[�έק�(mstate) Wptlproposal
				sql="select meid from wptlproposal where meid='"+ret[i][1]+"'";
				String[][]str_meid=t.queryFromPool(sql);

				//�]���u���v���Ҵ���ret[i][5]�b����|�ϥΨ�A�ҥH�N�t�]�b�@���ܼơC�����b�U�����C
				String Priotxtdate=ret[i][5];

//				sa.append("\nPriotxtdate:"+Priotxtdate);                          //�ˬd    

				if(str_meid.length==0 || str_meid[0][0].length()==0) {
					sql="insert into Wptlproposal"
					    +"(meid,rcvid,idKey,priochk1,Priotxtdate,Prio_idKey,ing_idKey)"
					    +" values ('"+ret[i][1].trim()+"','"+ret[i][8].trim()+"','"+ret[i][9].trim()+"','"+ret[i][4].trim()+"',"+noDateToNull(Priotxtdate)+",'"+prio_idkey+"','"+ing_idkey+"')";

					sa.append("\n�{�ǳ渹insert:\n"+sql);                          //�ˬd
				} else {
					sql="update Wptlproposal set"
					    +" meid='"+ret[i][1].trim()+"'"
					    +",rcvid='"+ret[i][8].trim()+"'"
					    +",idKey='"+ret[i][9].trim()+"'"
					    +",priochk1='"+ret[i][4].trim()+"'"
					    +",Priotxtdate="+noDateToNull(Priotxtdate)
					    +",Prio_idKey='"+prio_idkey+"'"
					    +",ing_idKey='"+ing_idkey+"'"
					    +" where meid='"+ret[i][1].trim()+"'";

					sa.append("\n�{�ǳ渹update:\n"+sql);                        //�ˬd
				}

				vSQL.add(sql);

//�ˬd�{�ǬO�_�������дڤw�д�

				if(ret[i][5].length()>0 && ret[i][3].length()>0) { //�ӽи��X�ΥӽФ��>0
					sql="select debitchk,debitid from wptlproc where idkey = '"+ret[i][9]+"'";
					String[][]ret_findate=t.queryFromPool(sql);
					sa.append("\n�ˬd�{�ǬO�_�������дڤw�дڪ��A�ӽи��X�ΥӽФ��:\n"+sql);
					if(ret_findate.length>0 && ret_findate[0][1].length()>0) { //�n�ݦ��ڸ��X�X�Ӧr�C
						sql="update wptmrcv set findate="+noDateToNull(ret[i][3].trim())+" where rcvid='"+ret[i][8].trim()+"'";
						vSQL_wp.add(sql);
						sa.append("\n�ˬd�{�ǬO�_�������дڤw�дڡA��s:\n"+sql);
						sql="update wptlproc set findate="+noDateToNull(ret[i][3].trim())+" where IDKey='"+ret[i][9].trim()+"'";
						vSQL.add(sql);
						sa.append("\n�ˬd�{�ǬO�_�������дڤw�дڡA��s:\n"+sql);
					} else if(ret_findate[0][0].equals("0")) { //���νд�
						sql="update wptmrcv set findate="+noDateToNull(ret[i][3].trim())+" where rcvid='"+ret[i][8].trim()+"'";
						vSQL_wp.add(sql);
						sh.append("\n�ˬd�{�ǬO�_�������дڤw�дڡA��s:\n"+sql);
						sql="update wptlproc set findate="+noDateToNull(ret[i][3].trim())+" where IDKey='"+ret[i][9].trim()+"'";
						vSQL.add(sql);
						sa.append("\n�ˬd�{�ǬO�_�������дڤw�дڡA��s:\n"+sql);
					}

				}


			}
		}


//�s�ɧ�s�e�U�H�Ǧ^�Ӫ����C
		String[][]ret_table2=getTableData("table2");
		for(int i=0; i<ret_table2.length; i++) {

//�s�ɮɧ�swptlproc�����
			sql="UPDATE wptlproc SET "
			    +"plsmanid ='"+ret_table2[i][2].trim()+"',"
			    +"conname ='"+ret_table2[i][3].trim()+"',"
			    +"conEmail ='"+ret_table2[i][4].trim()+"',"
			    +"othid ='"+ret_table2[i][5].trim()+"',"
			    +"specialtitle ='"+ret_table2[i][6].trim()+"',"
			    +"janloc ='"+ret_table2[i][7].trim()+"',"
			    +"janid ='"+ret_table2[i][8].trim()+"'"
			    +" WHERE IDKey ='"+ret_table2[i][1].trim()+"'";

			vSQL.add(sql);
			sa.append("\n----�e�U�H���---\n�s�ɮɧ�swptlproc�����:\n"+sql);	        //�ˬd
//�s�ɮɧ�swptmapply����ơA�����O1�ɡC
			if(ret_table2[i][9].equals("1")) {
				sql="UPDATE wptmapply SET "
				    +"plsmanid ='"+ret_table2[i][2].trim()+"',"
				    +"conname ='"+ret_table2[i][3].trim()+"',"
				    +"othid ='"+ret_table2[i][5].trim()+"',"
				    +"specialtitle ='"+ret_table2[i][6].trim()+"',"
				    +"janloc ='"+ret_table2[i][7].trim()+"',"
				    +"janid ='"+ret_table2[i][8].trim()+"'"
				    +" WHERE meid ='"+ret_table2[i][0].trim()+"'";
				vSQL.add(sql);
				sa.append("\n�s�ɮɧ�swptmapply����ơA�����O1��:\n"+sql+"\n");	        //�ˬd
			}
		}

		try {
			String[] tmp = (String[])vSQL.toArray(new String[0]);
			String[] tmp_wp = (String[])vSQL_wp.toArray(new String[0]);
			t.execFromPool(tmp);
			wp.execFromPool(tmp_wp);
			message("���ʸ�Ʈw���\");
		} catch(Exception e) {
			e.printStackTrace(System.err);
			message("���ʸ�Ʈw����"+e);
			return false;
		}
		return true;


	}//�קﵲ��

	public String delete()throws Throwable {                                  //�R��
		sa.append("\n------------------�R�� ����---------------\n");                  //�ˬd�I
		Vector vSQL = new Vector();
		talk t=getTalk("TradeMark");
		String sql="";
		String mUser=getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		String[][] ret = getTableData("table1");                                           //ret���o���
		for(int i=0; i<ret.length; i++) {
			if("1".equals(ret[i][0])) {
				sql="Delete wptlproc where idkey in(select ing_idkey from wptlproposal where meid='"+ret[i][1].trim()+"')";
				vSQL.add(sql);
				sa.append("\n �R��wptlproc�Aing_idkey :\n"+sql);                        //�ˬd�I
				sql="Delete wptlproc where idkey in(select prio_idkey from wptlproposal where meid='"+ret[i][1].trim()+"')";
				vSQL.add(sql);
				sa.append("\n �R��wptlproc�Aprio_idkey :\n"+sql);                        //�ˬd�I

				sql="insert into wptlproc_wrkman (gckey,idkey,meid,procWrkman,moddatetime,state)"
				    +" VALUES ('"+getGcKey()+"','"+ret[i][9]+"','"+ret[i][1]+"','"+mUser+"',"+noDateToNull(moddatetime)+",'DEL')";
				vSQL.add(sql);
				sa.append("\n �s�Wwptlproc_wrkman :\n"+sql);                        //�ˬd�I
				sql="update wptlproc set "
				    +"Jobmark ='*',"             //��¾�ȥN�z��//todo
				    +"Findate =null,"            //���{�ǧ�����
				    +"Wrkdate ="+noDateToNull(moddatetime)+","
				    +"Appydate = null,"
				    +"Rcvno='',"
				    +"sdtid=''"
				    +" where idkey = '"+ret[i][9].trim()+"'";
				vSQL.add(sql);
				sa.append("\n �ק�wptlproc :\n"+sql);                        //�ˬd�I
				sql="update wptmapply set "
				    +"appydate = null,"
				    +"findate = null,"
				    +"appyid =''"
				    +" where meid ='"+ret[i][1].trim()+"'";
				vSQL.add(sql);
				sa.append("\n �ק�wptmapply :\n"+sql);                        //�ˬd�I

				sql="update Wptlproposal set "
				    +"Prio_idKey=''"
				    +",ing_idKey=''"
				    +",priochk1=''"
				    +",priotxtdate=null"
				    +" where meid='"+ret[i][1].trim()+"'";
				vSQL.add(sql);
				sa.append("\n �ק�Wptlproposal :\n"+sql);                        //�ˬd�I
			}
		}
//		setValue("field2",sa.toString());
		try {
			String[] tmp = (String[])vSQL.toArray(new String[0]);
			t.execFromPool(tmp);
			message("���ʸ�Ʈw���\");
		} catch(Exception e) {
			message("���ʸ�Ʈw����");
			e.printStackTrace(System.err);
			return "";
		}
		return "";

	}
	/*��k:����榡����Ʀ�2013/01/01
	  Ū�ɮɨϥ�:�Y������Ū��"1900"�}�Y�A�^��"";
	  ��ƪ����A2000/01/01,2000-01-01,20000101
	*/
	public String dateformat(String date) {
		if(date.length()<8 || date.indexOf("1900")==0) {
			return"";
		} else if(date.length()>=8) {
			date=date.substring(0,10);
			date=date.replace("-","");
			date=convert.FormatedDate(date,"/");
		}
		return date;
	}

	/*�s�ɮɨϥΡA�B�z���null�����D
	  �b�s�ɪ��a��A������[''�A�]��null�Ȯǥ[''�ܦ�'null'�A��Ʈw�L�k�����C
	*/
	public static String noDateToNull(String date) throws Throwable {
		if(date.length()< 8 || date.indexOf("1900")==0) {
			return null;
		} else {
			date="'"+date+"'";
			return date;
		}
	}
	/*���ogckey
	*�^�ǭȬOString�A�Y�h���I�s�|�۰�+1
	*/
	String gckey=null;  //key�b�~�O�i�H�֥[
	public String getGcKey()throws Throwable {
		talk t=getTalk("TradeMark");
		if(gckey==null) {
			String sql = "select max(gckey) from wptlproc_wrkman";                //���ogckey�B�z
			String[][]ret = t.queryFromPool(sql);
			gckey= operation.add(ret[0][0].trim(),"1");//�r��[1�C
		} else {
			gckey= operation.add(gckey,"1");
		}
		if(gckey.length()<12) {
			gckey=convert.add0(gckey,"12");
		}
		return gckey;
	}
	/*���oidkey
	*�^�ǭȬOString�A�Y�h���I�s�|�۰�+1
	*/

	String numIdkey=null;  //idkey��class����k�i�H�֥[
	public String getIdkey()throws Throwable {
		talk t=getTalk("TradeMark");
		if(numIdkey==null) {
			String sql = "select max(idkey) from wptlproc";                //���oidkey�B�z
			String[][]ret = t.queryFromPool(sql);
			numIdkey=ret[0][0].trim();
			numIdkey=numIdkey.substring(2,numIdkey.length());    //�h���e�G�Ӧr
			numIdkey= operation.add(numIdkey,"1");//�r��[1�C
		} else {
			numIdkey= operation.add(numIdkey,"1");
		}
		if(numIdkey.length()<10) {
			numIdkey = convert.add0(numIdkey,"10");//�Y�S��10�X�N��0;
		}
		return "PC"+numIdkey;
	}

		
	/*�q��Ʈw�����o�Ҧ���쪺��ơA�öi��T��C
	
	*/
	public void setEditableField()throws Throwable{
		setValue("field1","hello");
		String sql="select CompoName from wptmFormComp where FormName='"+getFunctionName().trim()+"'";
		String[][]abc=t.queryFromPool(sql);
		
		for(int i=0;i<abc.length;i++){
			setEditable(abc[i][0],false);
		}

		return ;
	}
	/*�Q��idkey��findate�O�_��}��ק��檺����
	  import java.text.DateFormat;  import java.text.SimpleDateFormat;  import java.util.Date;
	  ������פp��8�Atrue
	  �������1900-01-01 00:00:00.0, true
	  ����j�󤵤ѡAtrue
	*/
	public boolean findateToOpen(String idkey) throws Throwable {
		talk t =getTalk("TradeMark");
		
		String sql="select findate from wptlproc where idkey='"+idkey+"'"	;
		String[][] retFindate=t.queryFromPool(sql);
		String findate="";
		if(retFindate.length>0){
			findate=retFindate[0][0].trim();
		}
		setValue("findate",findate);
	
		if (findate.length() < 8 || findate.equals("1900-01-01 00:00:00.0")) {
			return true;
		}

		//���o���Ѥ��
		Date date = new Date();
		DateFormat dateformat= new SimpleDateFormat("yyyy-MM-dd");
		String today=dateformat.format(date);
		Date dateToday=dateformat.parse(today);
		//�Nfindate�榡��
		Date finDate=dateformat.parse(findate);
		//�������A�ݽ֦b�᭱�C
		return finDate.after(dateToday);
	
	}

}
		




