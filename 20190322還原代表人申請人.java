/*
	備份還原申請人、代表人 
*/
public void revertWptlapman(String idkey)throws Throwable {
	//搜尋資料
	sql="SELECT a.pamanid,a.lapkey,b.meid FROM wptlappyOriApman a left join wptlproc b on a.idkey=b.idkey where a.idkey='"+idkey+"'";
	String[][]wptlappyOriApman=t.queryFromPool(sql);
	if(wptlappyOriApman.length>0) {
		for(int i=0; i<wptlappyOriApman.length; i++) {
			//取得資料
			String pamanid=wptlappyOriApman[i][0].trim();//讓與人編號
			String lapkey=wptlappyOriApman[i][1].trim();//Key連結代表人(自動取號)
			String meid=wptlappyOriApman[i][2].trim();//傳入参數txtidkey
			//更新資料
			sql="UPDATE wptlapman SET "
			    +"pamanid='"+pamanid
			    +"',lapkey='"+lapkey
			    +"' where meid='"+meid+"'";
			vt.add(vt);

			//刪除資料
			sql="DELETE FROM wptlapre where lapkey='"+lapkey+"'";
			vt.add(vt);
			//搜尋資料
			sql="SELECT id FROM wptlappyOriApre where lapkey='"+lapkey+"'";
			String[][]idArray=t.queryFromPool(sql);
			if(idArray.length>0) {
				for(int j=0; i<idArray.length; i++) {
					String id=idArray[j][0].trim();
					//新增資料
					sql="INSERT INTO wptlapre (id,lapkey) values ('"+id+"','"+lapkey+"')";
					vt.add(vt);
					//刪除資料
					sql="DELETE FROM wptlappyOriApman where lapkey='"+lapkey+"'";
					vt.add(vt);
				}
			}
		}
		//刪除資料
		sql="DELETE FROM wptlappyOriApman where idkey='"+idkey+"'";
		vt.add(vt);
	}
}

