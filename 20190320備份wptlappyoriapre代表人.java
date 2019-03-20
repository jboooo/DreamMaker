/*
	備份wptlappyoriapre代表人
	@戴勝台
*/

public void savewptlappyOriApman(String meid)throws throwable {

	sql="select a.id,b.unichineseprename,b.unioriginprename,a.lapkey "
	    +" from trademark..wptlapre a left join wp..wptmapre b on a.id=b.id "
	    +" where lapkey in "
	    +" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";

	String[][]retLapkey=t.queryFromPool(sql);
	if(retLapkey.length>0) {
		for(int i=0;i<retLapkey.length;i++){
		String id=retLapkey[i][0].trim();
		String unichineseprename=retLapkey[i][1].trim();
		String unioriginprename=retLapkey[i][2].trim();
		String lapkey=retLapkey[i][3].trim();

		//新增資料
		sql="INSERT INTO wptlappyoriapre (idchk,id,oname,cname,lapkey) "
			+" values ('1','"+id+"','"+oname+"','"+cname+"','"+lapkey+"')";
		
		vt.add(sql);
		}
	}
	execData(vt);
}