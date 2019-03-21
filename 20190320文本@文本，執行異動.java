execString(String str); //執行指示性商品、商標、描述性說明
/*
	文本@文本，執行異動
*/
public void execString(String str){
vt=new Vector();
String[] strArray = str.split("@");
for (int i = 0; i < strArray.length; i++) {
	vt.add(strArray[i].trim());
}
execData(vt);
}