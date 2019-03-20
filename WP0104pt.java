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
		if ("查詢".equals(value)) {
			QUERY();
		} else if ("修改".equals(value)) {
			if(UPDATE()){
				QUERY();
			}
		} else if ("刪除".equals(value)) {
			delete();
			QUERY();
		}

		return false;
	}

//查詢
	public void QUERY()throws Throwable {
		sa.append("\n-------提出申請.查詢--------");                            //檢查

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
//			message("請輸入查詢條件");
//			return ;
//		}
//搜尋條件
//		if(rcvid.length()>0) {
//			sqlWhere+=" and b.rcvid ='"+rcvid+"'";
//		}
		if(getQueryValue("idkey").length()>0) {
			idkey=getQueryValue("idkey").trim();			
			sqlWhere+= " and b.idkey ='"+idkey+"'";
		}else {
			message("請輸入查詢條件");
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
		sa.append("\n欄位"+sql);                                   //檢查

		String[][]ret=t.queryFromPool(sql);
		if(ret.length==0) {
			message("沒有這筆資料");
			return ;
		}
		meid=ret[0][0].trim();
		setValue("meid",meid);
		//委託人id轉姓名
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
		setValue("procid",ret[0][7]);  //隱藏
		setValue("rcvid",ret[0][8]);   //隱藏
		setValue("othid",ret[0][9]);//隱藏
		setValue("debittype",ret[0][10]);//隱藏
		setValue("usertype",ret[0][11]);//隱藏
		setValue("specialtitle",ret[0][12]);//隱藏
		setValue("janloc",ret[0][13]);//隱藏
		setValue("janid",ret[0][14]);//隱藏
		setValue("debittitle",ret[0][15]);//隱藏

		//申請人有多筆資料
		String str="";
		StringBuffer str_qpamanid=new StringBuffer();                      //申請人id轉下頁
		String str1="z";
		for (int i=0; i<ret.length; i++) {
			sql="select unioriginname from wptmapman where pamanid='"+ret[i][16]+"'";  //id轉名字
			String[][]strname=wp.queryFromPool(sql);
			if(strname.length>0) {
				str+=strname[0][0].trim()+"\n";
			} else {
				str+=ret[i][16]+"\n";
			}
			str_qpamanid.append("#"+ret[i][16].trim()+"#,");
		}
		str_qpamanid.setLength(str_qpamanid.length()-1);
		setValue("qpamanid",str_qpamanid.toString());               //多筆申請人，要傳到下一個表單。
		setValue("pamanid",str);
		setValue("ofeededuct",ret[0][17]);//規費減免金額

		sql = "select DISTINCT '',a.meid,a.markname,a.appydate,'','',a.appyid"
		      +",b.rcvno,b.rcvid,b.idkey,'',(select LEFT(pamanid,8) FROM WPTLAPMAN WHERE MEID='"+meid+"' for xml path(''))"
		      +" from wptmapply a"
		      +" JOIN wptlproc b ON A.MEID=B.MEID"
		      +" JOIN wptlapman c ON A.MEID=C.MEID"
		      +" where b.procid ='0104'"		//程序為0104
		      +" and c.pamanid in(select pamanid from wptlapman where meid='"+meid+"')"  //申請人相同
		      +" and a.plsmanid in(select plsmanid from wptlproc where meid='"+meid+"')"//委託人編號相同
		      +" and ISNULL(a.appydate,'') in (select ISNULL(appydate,'') from wptmapply where meid='"+meid+"')"//申請日期相同
		      +" and left(b.rcvid,10)=left("+ret[0][8].trim()+",10)" //前10碼相同
		      +" order by a.meid";
		String[][] ret1=t.queryFromPool(sql);
		sa.append("\n表格sql:\n"+sql);			                        //檢查


		//如果讀不到資料，就跳出。
//		if(ret1.length==0) {
//			message("系統沒有資料");
//			return ;
//		}
//		sa.append("\nret1的長度:"+ret1.length);                                 //檢查

		//設定ret1[i][1]是否打勾。
		for(int i=0; i<ret1.length; i++) {
			ret1[i][0]="0";
			ret1[i][1]=ret1[i][1].trim();           //去除空白再比較
			if(ret1[i][1].equals(meid)) {
				ret1[i][0]="1";
			}
		}//i迴圈結束
//		sa.append("\n打勾後ret1的長度:"+ret1.length);                                 //檢查

		//設定將有打勾的資料存入vt
		for(int i=0; i<ret1.length; i++) {
			if("1".equals(ret1[i][0])) {
				vt.add(new String[] { ret1[i][0],ret1[i][1],ret1[i][2],ret1[i][3],
				                      ret1[i][4],ret1[i][5],ret1[i][6],ret1[i][7],ret1[i][8],ret1[i][9],ret1[i][10],ret1[i][11]
				                    });
//				sa.append("\n將打勾的資料存入VT:"+vt.size());                                 //檢查

			}
		}//i迴圈結束
//		sa.append("\nret1.length:"+ret1.length);                                 //檢查

		//設定將沒有打勾的資料存入vt
		for(int i=0; i<ret1.length; i++) {
//			sa.append("\n將沒打勾的資料存入VT:有");                                   //檢查
			if("0".equals(ret1[i][0])) {
				vt.add(new String[] { ret1[i][0],ret1[i][1],ret1[i][2],ret1[i][3],
				                      ret1[i][4],ret1[i][5],ret1[i][6],ret1[i][7],ret1[i][8],ret1[i][9],ret1[i][10],ret1[i][11]
				                    });
			}
		}//i迴圈結束
//		setValue("field1",sa.toString());                                   //檢查

		//將vt的資料放回ret1
		ret1=(String[][])vt.toArray(new String[0][0]);


		//加入補件請款。
		for(int i=0; i<ret1.length; i++) {
			//補件請款
			sql="select sdtid from wptlproc where idkey='"+ret1[i][9]+"'";
			String[][]ret_sdtid=t.queryFromPool(sql);
			sa.append("\n補件請款:"+sql);                                      //檢查
			if(ret_sdtid[0][0]!=null) {
				if((ret_sdtid[0][0].indexOf("T04"))>=0) {
					ret1[i][11]="1";
				}
			}
		} //迴圈結束

		setTableData("table1",ret1);
		setEditable("ofeededuct",true);
		setValue("field1",sa.toString());                                   //檢查
		
		//看findate的條件鎖欄位
		if(!findateToOpen(idkey)){
			setEditableField();//todo
		}else{
			setEditableField();//todo
			setEditable("ofeededuct",true);
			setEditable("sbndate",true);
			setEditable("table1",true);
			
			//加入優先權等資料。
			for(int i=0; i<ret1.length; i++) {
				
				setEditable("table1",i,3,true); //申請日期
				setEditable("table1",i,6,true); //申請號碼
				setEditable("table1",i,7,true); //收據號碼
				setEditable("table1",i,8,true); //補件請款
				
				//設定有優先權日期，才能修改優先權文件齊備，優先權補件期限
				sql="select count(*) from wptlprior where meid='"+ret1[i][1].trim()+"' and priogiveup<>1";
				String[][]ret_priodate=t.queryFromPool(sql);
				sa.append("\n查詢是否有優先權:"+sql);                                      //檢查
				
				if(ret_priodate[0][0].equals("0")) {
					setEditable("table1",i,4, false);
					setEditable("table1",i,5, false);
				} else {
					//在放入table1之前，將優先權的資料加入。
					sql = "select priochk1,priotxtdate from wptlproposal where meid='"+ret1[i][1]+"'";
					String[][]ret_pri=t.queryFromPool(sql);
					sa.append("\n優先權的資料加入:"+sql);                                      //檢查				
					if(ret_pri.length>0) {
						ret1[i][4]=ret_pri[0][0].trim();
						ret1[i][5]=ret_pri[0][1].trim();
					}
					//優先權文件齊備為是，優先權補件期限就設為""，
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

	}//查詢結束

//修改
	public boolean UPDATE()throws Throwable {
		sa.append("-------提出申請，修改--------");                            //檢查
		Vector vSQL = new Vector();
		Vector vSQL_wp=new Vector();
		StringBuffer sh=new StringBuffer();

		talk t=getTalk("TradeMark");
		talk wp=getTalk("wp");
		String idkey=getValue("idkey");
		String mdate = datetime.getToday("YYYY/mm/dd");           //Wrkdate取得作業日期
		String mtime = datetime.getTime("h:m:s");
		String wrkdate = mdate+ " " + mtime; 			   //取得Wrkdate
		String mUser=getUser();
		String sql="";                                                   //取得user

		String debittitle=getValue("debittitle").trim(); // debittitle
		String debittype=getValue("debittype").trim(); // debittype
		String usertype=getValue("usertype").trim(); // usertype
		String specialtitle=getValue("specialtitle").trim(); // specialtitle
		String janloc=getValue("janloc").trim(); // janloc
		String janid=getValue("janid").trim(); // janid
		String appydate=getValue("appydate").trim(); // appydate申請日期
		String ofeededuct=getValue("ofeededuct").trim(); //規費減免金額

		String[][] ret = getTableData("table1"); //ret取得表格

		String prio_idkey="";
		String ing_idkey="";

		//檢核是否大於32767

		int int_ofeededuct = Integer.parseInt(ofeededuct);

		if((32767-int_ofeededuct)<0) {
			message("規費減免金額不可大於32767");
			return false;
		}

		if(!findateToOpen(idkey)){
			message("已經完成，不可再修改");
			return false;
		}

//table1表格開始
		for(int i=0; i<ret.length; i++) {
			if(ret[i][0].equals("1")) {
				if(ret[i][6].trim().length()==0 && ret[i][3].trim().length()==0 && ret[i][7].length()==0){
			
				} else if((ret[i][6].trim()).length()>0 && (ret[i][3].trim()).length()>0 && ret[i][7].length()>0) {
			
				}else{
					message("有申請日期，申請號碼及收據號碼都要同時有或同時沒有才能存檔");
					return false;	
				}

//wptmapply 更新主檔appydate及appyid
				sql="update wptmapply set "
				    +"appydate="+noDateToNull(ret[i][3].trim())+","
				    +"appyid='"+ret[i][6].trim()+"'"
				    +" where meid='"+ret[i][1].trim()+"'";
				vSQL.add(sql);
				sh.append("\nwptmapply 更新主檔:\n"+sql);


//修改案件程序紀錄檔

				//取得承辦員從wptmrcv檔，條件是meid。取一筆。
				sql="select wrkman from wptlproc where idkey='"+ret[i][9].trim()+"'";
				String[][]str_wrkman=t.queryFromPool(sql);
//				sh.append("\n取得承辦員wrkman,"+str_wrkman[0][0]+"\n");
				String wrkman="";
				if(str_wrkman.length>0) {
					wrkman=str_wrkman[0][0];
				}
				//取得lawdate法定期限和指定日期
				sql="select lawdate,asndate from wptlproc where idkey='"+ret[i][9].trim()+"'";   

				String[][]str_date=t.queryFromPool(sql);
				String strlawdate=str_date[0][0].trim();
				String strasndate=str_date[0][1].trim();
				
				//修改delaymk的值
				String delaymk="0";                                                     
				if(strlawdate.length()==0 && strasndate.length()==0) {
					delaymk="0";
				} else if(ret[i][3].compareTo(strlawdate)>0 || ret[i][3].compareTo(strasndate)>0) {
					delaymk="1";
				}
				
				//由職務代理時
				String jobmark="";
				if(wrkman.equals(mUser)) {                                              
					jobmark="";
				} else {
					jobmark="*";
				}

				//程序完成日
				String findate=ret[i][3].trim();
				//補件請款
				String sdtid="";                    
				if("1".equals(ret[i][11]) & (!"".equals(ret[i][4])) ) { //有補件日期t04
					sdtid="T03,T04";
				} else if(!"".equals(ret[i][4])) { //有優先權t03
					sdtid="T03";
				} else if("1".equals(ret[i][11])) {
					sdtid="T04";
				}
				//案件程序檔sql
				sql="update wptlproc set "
				    +"jobmark ='"+jobmark.trim()+"',"                                               //由職務代理時
				    +"findate ="+noDateToNull(findate.trim())+","                          //本程序完成日
				    +"wrkdate ="+noDateToNull(wrkdate)+","
				    +"delaymk ='"+delaymk+"',"                          //逾期註記
				    +"appydate="+noDateToNull(ret[i][3].trim())+","
				    +"sbmdate="+noDateToNull(ret[i][3].trim())+","                    //測試用。
				    +"rcvno='"+ret[i][7].trim()+"',"
				    +"appyid='"+ret[i][6].trim()+"',"
				    +"specialtitle='"+specialtitle+"',"
				    +"janloc='"+janloc+"',"
				    +"janid='"+janid+"',"
				    +"ofeededuct='"+ofeededuct+"',"
				    +"sdtid='"+sdtid+"'"                            //補件請款
				    +" where idkey='"+ret[i][9].trim()+"'";
				sh.append("\n案件程序檔\n"+sql);
				vSQL.add(sql);

				//案件程序經手承辦員記錄檔,若新承辦員則寫入檔
				if(jobmark.length()>0) {

					sql="insert into wptlproc_wrkman(GCkey,IDkey,meid,procWrkman,modDateTime,state)"
					    +" VALUES ('"+getGcKey()+"','"+ret[i][9].trim()+"','"+ret[i][1].trim()+"','"+mUser+"',"+noDateToNull(wrkdate)+",'MOD')";
					sh.append("\n案件程序經手承辦員記錄檔,若新承辦員則寫入檔\n"+sql);
					vSQL.add(sql);
				}
//增加申請中程序:

				//搜尋ing_idKey來自wptlproposal,條件是meid，
				//確認這筆資料以前有沒有存過，有存過就用update,沒有就用insert into
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

					ing_idkey=ret_010D[0][0].trim();                  //增加或修改wptlpropsal要用到。
					sa.append("\n申請中程序010D:\n"+sql);	                       //檢查
					sa.append("\ning_idkey:"+ing_idkey);	                //檢查

				} else {

					idkey=getIdkey();     //前面再加上PC即可

					sql="insert into wptlproc("
					    +"jobmark,meid,caseid,procid,findate,"
					    +"rcvid,wrkman,wrkdate,ltdate,debitid,"
					    +"debitdate,debitchk,IDKey,lawdate,asndate,"
					    +"dbbouns) values('"
					    +jobmark+"','"+ret[i][1].trim()+"','01','010D',null,'"
					    +ret[i][8].trim()+"','"+wrkman+"',"+noDateToNull(wrkdate)+",null,'null',null,0,'"
					    +idkey+"',null,null,1)";

					ing_idkey=idkey;                  //增加或修改wptlpropsal要用到。
					sa.append("\n申請中程序010D:\n"+sql);	                       //檢查
					sa.append("\ning_idkey:"+ing_idkey);	                          //檢查

				}
				vSQL.add(sql);
//加【補優先權文件】程序:

				//有優先補件期限(wptmapply.priotxtdate <> null)且優先權文件齊備為否(wptmapply.priochk1=false)時
				String priochk1=ret[i][4].trim();	                                //取得優先權文件齊備
				sql="select count(*) from wptlprior where meid='"+ret[i][1].trim()+"' and priogiveup<>1";//取得優先權日期
				String[][]sql_priodate=t.queryFromPool(sql);
				sa.append("\n取得優先權日期:\n"+sql);

				//判斷優先權日期(有)and優先權文件齊備(是)
				if(sql_priodate.length >0 && priochk1.equals("1")) {
					sql="select prio_idkey from wptlproposal where meid='"+ret[i][1].trim()+"'";
					String[][]ret_prio=t.queryFromPool(sql);

					//判斷補優先權是否有存過
					if(ret_prio.length>0 && ret_prio[0][0].length()>0) {
						//刪除程序中的prio_idkey:
						sql="update wptlproposal set prio_idkey='' where meid='"+ret[i][1].trim()+"'";
						vSQL.add(sql);

						sa.append("\n刪除wptlproc:\n"+sql+"\n");                    //檢查
						//用prio_idkey刪除wptlproc
						sql="delete from wptlproc where idkey='"+ret_prio[0][0]+"'";
						vSQL.add(sql);

						sa.append("\n刪除wptlproc:\n"+sql+"\n");                    //檢查
					}
				}

				//判斷優先權日期(有)and優先權文件齊備(無)
				if(sql_priodate.length >0 && priochk1.equals("0")) {
					//取得優先權補件期限
					String lawdate=ret[i][5].trim();								
					if(lawdate.length()==0) {
						message("優先權補件期限未填寫");
						return false;
					}
					//取得Asndate日期,lawdate日期-5天
					String []tmp  = lawdate.split(" ");                             
					String data = tmp[0].trim();
					data=convert.replace(data,"-","");
					String asndate=datetime.dateAdd(data,"d",-5);//日期天數的調整
					asndate=convert.FormatedDate(asndate,"/");
					
					//搜尋prio_idkey來自wptlproposal,條件是meid，
					//確認這筆資料以前有沒有存過，有存過就用update,沒有就用insert into
					sql="select prio_idkey from wptlproposal where meid='"+ret[i][1].trim()+"'";
					String[][]ret_prio=t.queryFromPool(sql);

					sa.append("\n加【補優先權文件】程序:有存過就用update,沒有就用insert into\n"+sql);
					if(ret_prio.length>0 && ret_prio[0][0].length()==12) {           //判斷補優先權是否有存過
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
						sa.append("\n加補優先權文件010C:\n"+sql);	                       //檢查

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

						sa.append("\n加補優先權文件010C:\n"+sql);	                       //檢查
					} //結束存優先權
					vSQL.add(sql);
				}


//記錄【補優先權文件】程序單號: Prio_idKey,增加或修改(mstate) Wptlproposal
				sql="select meid from wptlproposal where meid='"+ret[i][1]+"'";
				String[][]str_meid=t.queryFromPool(sql);

				//因為優先權補證期限ret[i][5]在之後會使用到，所以就另設在一個變數。說明在下面有。
				String Priotxtdate=ret[i][5];

//				sa.append("\nPriotxtdate:"+Priotxtdate);                          //檢查    

				if(str_meid.length==0 || str_meid[0][0].length()==0) {
					sql="insert into Wptlproposal"
					    +"(meid,rcvid,idKey,priochk1,Priotxtdate,Prio_idKey,ing_idKey)"
					    +" values ('"+ret[i][1].trim()+"','"+ret[i][8].trim()+"','"+ret[i][9].trim()+"','"+ret[i][4].trim()+"',"+noDateToNull(Priotxtdate)+",'"+prio_idkey+"','"+ing_idkey+"')";

					sa.append("\n程序單號insert:\n"+sql);                          //檢查
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

					sa.append("\n程序單號update:\n"+sql);                        //檢查
				}

				vSQL.add(sql);

//檢查程序是否完成應請款已請款

				if(ret[i][5].length()>0 && ret[i][3].length()>0) { //申請號碼及申請日期>0
					sql="select debitchk,debitid from wptlproc where idkey = '"+ret[i][9]+"'";
					String[][]ret_findate=t.queryFromPool(sql);
					sa.append("\n檢查程序是否完成應請款已請款的，申請號碼及申請日期:\n"+sql);
					if(ret_findate.length>0 && ret_findate[0][1].length()>0) { //要看收據號碼幾個字。
						sql="update wptmrcv set findate="+noDateToNull(ret[i][3].trim())+" where rcvid='"+ret[i][8].trim()+"'";
						vSQL_wp.add(sql);
						sa.append("\n檢查程序是否完成應請款已請款，更新:\n"+sql);
						sql="update wptlproc set findate="+noDateToNull(ret[i][3].trim())+" where IDKey='"+ret[i][9].trim()+"'";
						vSQL.add(sql);
						sa.append("\n檢查程序是否完成應請款已請款，更新:\n"+sql);
					} else if(ret_findate[0][0].equals("0")) { //不用請款
						sql="update wptmrcv set findate="+noDateToNull(ret[i][3].trim())+" where rcvid='"+ret[i][8].trim()+"'";
						vSQL_wp.add(sql);
						sh.append("\n檢查程序是否完成應請款已請款，更新:\n"+sql);
						sql="update wptlproc set findate="+noDateToNull(ret[i][3].trim())+" where IDKey='"+ret[i][9].trim()+"'";
						vSQL.add(sql);
						sa.append("\n檢查程序是否完成應請款已請款，更新:\n"+sql);
					}

				}


			}
		}


//存檔更新委託人傳回來的表格。
		String[][]ret_table2=getTableData("table2");
		for(int i=0; i<ret_table2.length; i++) {

//存檔時更新wptlproc的資料
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
			sa.append("\n----委託人資料---\n存檔時更新wptlproc的資料:\n"+sql);	        //檢查
//存檔時更新wptmapply的資料，當條件是1時。
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
				sa.append("\n存檔時更新wptmapply的資料，當條件是1時:\n"+sql+"\n");	        //檢查
			}
		}

		try {
			String[] tmp = (String[])vSQL.toArray(new String[0]);
			String[] tmp_wp = (String[])vSQL_wp.toArray(new String[0]);
			t.execFromPool(tmp);
			wp.execFromPool(tmp_wp);
			message("異動資料庫成功");
		} catch(Exception e) {
			e.printStackTrace(System.err);
			message("異動資料庫失敗"+e);
			return false;
		}
		return true;


	}//修改結束

	public String delete()throws Throwable {                                  //刪除
		sa.append("\n------------------刪除 執行---------------\n");                  //檢查點
		Vector vSQL = new Vector();
		talk t=getTalk("TradeMark");
		String sql="";
		String mUser=getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		String[][] ret = getTableData("table1");                                           //ret取得表格
		for(int i=0; i<ret.length; i++) {
			if("1".equals(ret[i][0])) {
				sql="Delete wptlproc where idkey in(select ing_idkey from wptlproposal where meid='"+ret[i][1].trim()+"')";
				vSQL.add(sql);
				sa.append("\n 刪除wptlproc，ing_idkey :\n"+sql);                        //檢查點
				sql="Delete wptlproc where idkey in(select prio_idkey from wptlproposal where meid='"+ret[i][1].trim()+"')";
				vSQL.add(sql);
				sa.append("\n 刪除wptlproc，prio_idkey :\n"+sql);                        //檢查點

				sql="insert into wptlproc_wrkman (gckey,idkey,meid,procWrkman,moddatetime,state)"
				    +" VALUES ('"+getGcKey()+"','"+ret[i][9]+"','"+ret[i][1]+"','"+mUser+"',"+noDateToNull(moddatetime)+",'DEL')";
				vSQL.add(sql);
				sa.append("\n 新增wptlproc_wrkman :\n"+sql);                        //檢查點
				sql="update wptlproc set "
				    +"Jobmark ='*',"             //由職務代理時//todo
				    +"Findate =null,"            //本程序完成日
				    +"Wrkdate ="+noDateToNull(moddatetime)+","
				    +"Appydate = null,"
				    +"Rcvno='',"
				    +"sdtid=''"
				    +" where idkey = '"+ret[i][9].trim()+"'";
				vSQL.add(sql);
				sa.append("\n 修改wptlproc :\n"+sql);                        //檢查點
				sql="update wptmapply set "
				    +"appydate = null,"
				    +"findate = null,"
				    +"appyid =''"
				    +" where meid ='"+ret[i][1].trim()+"'";
				vSQL.add(sql);
				sa.append("\n 修改wptmapply :\n"+sql);                        //檢查點

				sql="update Wptlproposal set "
				    +"Prio_idKey=''"
				    +",ing_idKey=''"
				    +",priochk1=''"
				    +",priotxtdate=null"
				    +" where meid='"+ret[i][1].trim()+"'";
				vSQL.add(sql);
				sa.append("\n 修改Wptlproposal :\n"+sql);                        //檢查點
			}
		}
//		setValue("field2",sa.toString());
		try {
			String[] tmp = (String[])vSQL.toArray(new String[0]);
			t.execFromPool(tmp);
			message("異動資料庫成功");
		} catch(Exception e) {
			message("異動資料庫失敗");
			e.printStackTrace(System.err);
			return "";
		}
		return "";

	}
	/*方法:日期格式化轉化成2013/01/01
	  讀檔時使用:若日期資料讀到"1900"開頭，回傳"";
	  資料的型態2000/01/01,2000-01-01,20000101
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

	/*存檔時使用，處理日期null的問題
	  在存檔的地方，日期不加''，因為null值旁加''變成'null'，資料庫無法接受。
	*/
	public static String noDateToNull(String date) throws Throwable {
		if(date.length()< 8 || date.indexOf("1900")==0) {
			return null;
		} else {
			date="'"+date+"'";
			return date;
		}
	}
	/*取得gckey
	*回傳值是String，若多筆呼叫會自動+1
	*/
	String gckey=null;  //key在外是可以累加
	public String getGcKey()throws Throwable {
		talk t=getTalk("TradeMark");
		if(gckey==null) {
			String sql = "select max(gckey) from wptlproc_wrkman";                //取得gckey處理
			String[][]ret = t.queryFromPool(sql);
			gckey= operation.add(ret[0][0].trim(),"1");//字串加1。
		} else {
			gckey= operation.add(gckey,"1");
		}
		if(gckey.length()<12) {
			gckey=convert.add0(gckey,"12");
		}
		return gckey;
	}
	/*取得idkey
	*回傳值是String，若多筆呼叫會自動+1
	*/

	String numIdkey=null;  //idkey為class的方法可以累加
	public String getIdkey()throws Throwable {
		talk t=getTalk("TradeMark");
		if(numIdkey==null) {
			String sql = "select max(idkey) from wptlproc";                //取得idkey處理
			String[][]ret = t.queryFromPool(sql);
			numIdkey=ret[0][0].trim();
			numIdkey=numIdkey.substring(2,numIdkey.length());    //去除前二個字
			numIdkey= operation.add(numIdkey,"1");//字串加1。
		} else {
			numIdkey= operation.add(numIdkey,"1");
		}
		if(numIdkey.length()<10) {
			numIdkey = convert.add0(numIdkey,"10");//若沒有10碼就補0;
		}
		return "PC"+numIdkey;
	}

		
	/*從資料庫中取得所有欄位的資料，並進行禁改。
	
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
	/*利用idkey看findate是否能開放修改欄的條件
	  import java.text.DateFormat;  import java.text.SimpleDateFormat;  import java.util.Date;
	  日期長度小於8，true
	  日期等於1900-01-01 00:00:00.0, true
	  日期大於今天，true
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

		//取得今天日期
		Date date = new Date();
		DateFormat dateformat= new SimpleDateFormat("yyyy-MM-dd");
		String today=dateformat.format(date);
		Date dateToday=dateformat.parse(today);
		//將findate格式化
		Date finDate=dateformat.parse(findate);
		//比較日期，看誰在後面。
		return finDate.after(dateToday);
	
	}

}
		




