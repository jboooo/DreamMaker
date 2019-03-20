backupMemanid(idkey,keyno);
/*
	備份本所代理人
*/
public void backupMemanid(String idkey,String keyno)throws Throwable{

//刪除資料
sql="DELETE FROM wptlappyOriMeman WHERE idkey = idkey";

String[][]memanidTable=getTableData("memanidTable");
//取得資料
String memanid=getValue("memanid".trim());//本所代理人編號
String memanname=getValue("memanname".trim());//本所代理人名稱
//新增資料
sql="INSERT INTO wptlappyOriMeman (idkey,keyno,memanid,memanname,memanidCHK) "
	+" values ('"+idkey+"','"+keyno+"','"+memanid+"','"+memanname+"','1')";
vt.add(sql);

execData(vt)

}

