	/*
	備份wptlappyoriapre代表人
	@戴勝台
	*/
	
	public void saveWptlappyOriApman(String meid)throws Throwable {
		vt=new Vector();
		//搜尋代表人資料
		sql="select a.id,b.unichineseprename,b.unioriginprename,a.lapkey "
			+" from trademark..wptlapre a left join wp..wptmapre b on a.id=b.id "
			+" where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		//刪除舊有的lapkey
		String[][]retLapkey=t.queryFromPool(sql);
		sa.append(sql+"\n");
		sql="delete from wptlappyoriapre where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		vt.add(sql);
		//新增新的lapkey
		if(retLapkey.length>0) {
			for(int i=0;i<retLapkey.length;i++){
			String id=retLapkey[i][0].trim();
			String unichineseprename=retLapkey[i][1].trim();
			String unioriginprename=retLapkey[i][2].trim();
			String lapkey=retLapkey[i][3].trim();
	
			//新增資料
			sql="INSERT INTO wptlappyoriapre (idchk,id,oname,cname,lapkey) "
				+" values ('1','"+id+"','"+unichineseprename+"','"+unioriginprename+"','"+lapkey+"')";
			
			vt.add(sql);
			}
		}
		execData(vt);//異動資料庫
	}