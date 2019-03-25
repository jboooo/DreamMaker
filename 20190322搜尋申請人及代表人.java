/*
	搜尋-申請人與代表人，參數:本所案號
*/
public void selectPamanidAndId(String meid)throws Throwable {

	sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.meid,a.lapkey "
	      +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
	      +" where a.meid='"+meid+"' order by a.pamanid desc";

	String[][]pamanidTable = t.queryFromPool(sql);
	sa.append(sql+"\n");
	setTableData("pamanidTable", pamanidTable);
	for(int i=0; i<pamanidTable.length; i++) {
		String lapkey=pamanidTable[i][6].trim();

		sql="select b.id,c.uniOriginPrename,c.uniChinesePrename,a.lapkey"
		    +" from trademark..wptlapman a "
		    +" left join trademark..wptlapre b on a.lapkey = b.lapkey "
		    +" left join wp..wptmapre c on c.id=b.id "
		    +" where a.lapkey = '"+lapkey+"'"
		    +" order by a.pamanid";

		setValue("field1",sql);
		String[][]idTable=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(idTable.length>0) {
			put(pamanidTable[i][0],idTable);
		} else {
			put(pamanidTable[i][0],null);
		}
	}

}