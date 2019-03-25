	/*	備份申請人
		@戴勝台
	*/
	
	public void backupPamanid(String idkey,String meid)throws Throwable {
		vt=new Vector();
	
		//刪除資料
		sql="DELETE FROM wptlappyOriApman  WHERE idkey ='"+idkey+"'";
		vt.add(sql);	
		
		sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.lapkey "
					 +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
					 +" where a.meid='"+meid+"' order by a.pamanid desc";				 
		
		String[][]pamanidTable=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(pamanidTable.length>0) {
			for(int i=0;i<pamanidTable.length;i++){
				//取得資料
				String pamanid=pamanidTable[i][0].trim();//讓與人編號
				String cmanname1=pamanidTable[i][1].trim();//讓與人名(中)
				String manname1=pamanidTable[i][2].trim();//讓與人名(原)
				String addr1=pamanidTable[i][3].trim();//讓與人地址(原)
				addr1=addr1.replace("/n","");
				String addr5=pamanidTable[i][4].trim();//讓與人地址(中)
				addr1=addr1.replace("/n","");
				String lapkey =pamanidTable[i][5].trim();//Key連結代表人
				//取得keyno值
				sql="select keyno from wptlproexte where idkey='"+idkey+"'";
				String[][]keynoArray=t.queryFromPool(sql);
				sa.append(sql+"\n");
				String keyno=keynoArray[0][0];
		

				//新增資料
				sql="INSERT INTO wptlappyOriApman (pamanidCHK,pamanid,cmanname1,manname1,addr1,addr5,lapkey,idkey,keyno)"
					+" values ('1','"+pamanid+"','"+cmanname1+"','"+manname1+"','"+addr1+"','"+addr5+"','"+lapkey+"','"+idkey+"','"+keyno+"')";
				vt.add(sql);
			}
		}
	
		execData(vt);//異動資料庫
	}