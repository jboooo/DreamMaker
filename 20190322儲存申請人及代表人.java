	/*
	儲存申請人，代表人,申請人一定要先異動，才能取得lapkey的值
	*/
	public void savePamanid(String meid)throws Throwable {
		vt= new Vector(); // 儲存申請人專用的	
		String ispamanid=getValue("ispamanid".trim());//申請人
		String[][] retPamanidTable = getTableData("pamanidTable");
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 判斷儲存申請人
			//檢核
			for (int i = 0; i < retPamanidTable.length; i++) {
				// 檢核:判斷申請人編號為空。
				if (retPamanidTable[i][1].length() == 0) {
					message("申請人編號為空");
					return ;
				}
			}
	
			StringBuffer sbPamanid=new StringBuffer();
			for(int i=0; i<retPamanidTable.length; i++) {
				sbPamanid.append("'"+retPamanidTable[i][0]+"',");
			}
			sbPamanid.setLength(sbPamanid.length()-1);
	
			sql="select pamanid from wptlapman where meid='"+meid+"' and pamanid not in("+sbPamanid.toString()+")";
			String[][]ontInPamanid=t.queryFromPool(sql);
			sa.append(sql+"\n");
			//刪除wptmapman的資料
			if(ontInPamanid.length>0){
			for(int i=0; i<ontInPamanid.length; i++) {
				sql="delete from wptlapman where meid='"+meid+"' and pamanid='"+ontInPamanid[i][0]+"'";
				vt.add(sql);
			}
			}
			//新增wptmapman的資料
			sql="select pamanid from wptlapman where meid='"+meid+"'";
			String[][] newWptlapmanPamanid=t.queryFromPool(sql);
			sa.append(sql+"\n");
			boolean isTrue=false;			
			if(retPamanidTable.length>0){
				for( int i=0; i<retPamanidTable.length; i++) {
					isTrue=false;
					if(newWptlapmanPamanid.length>0){
						for(int j=0; j<newWptlapmanPamanid.length; j++) {
							if(retPamanidTable[i][0].equals(newWptlapmanPamanid[j][0])) {
								isTrue=true;
								break;
							}
						}
					}
					if(!isTrue){
						sql = "insert into wptlapman (meid,pamanid) values ('" + meid + "','" + retPamanidTable[i][0] + "')";
						vt.add(sql);
					}
				}
			}
		}
		execData(vt);//異動資料庫
	
		//儲存代表人
		vt=new Vector();
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 儲存申請人判斷

			for (int i = 0; i < retPamanidTable.length; i++) {
				sql = "select lapkey from wptlapman where meid='" + meid + "' and pamanid ='" + retPamanidTable[i][0] + "'";
				String[][] ret_lapkey = t.queryFromPool(sql);
				sa.append(sql+"\n");
					if(ret_lapkey.length>0){
						sql="delete wptlapre where lapkey='"+ret_lapkey[0][0]+"'";
						vt.add(sql);
					    String[][]idTable=(String[][])get(retPamanidTable[i][0],new String[0][0]);
						for(int j=0;j<idTable.length;j++){
						sql = "insert into wptlapre (lapkey,id) values ('"+ret_lapkey[0][0]+"','" + idTable[j][0] + "')";
						vt.add(sql);
					}
					}
					// 儲存代表人，新增wptlapre的lapkey,id的資料
			} // 結束i迴圈
		} // 結束儲存代表人判斷。
	
		execData(vt);//異動資料庫
	}