<?php
header('Content-Type: text/html; charset=utf-8');
$connect = mysql_connect("localhost","ryunha","ghktkgksxla234");
if(!$connect){
    echo 'aaa';
    die('Could not connect : '.mysql_error());
}

mysql_select_db("ryunha",$connect);

$keyword = $_POST['keyword'];
$keyword = urldecode($keyword);
	

$arr = 'select * from insta_app where wholeHashtag LIKE ';
$arr = $arr.$keyword;

$res = mysql_query($arr, $connect);
$result = array();

while($row = mysql_fetch_array($res))
{

	array_push($result,
		array('likenum'=>$row[1], 'shortcode'=>$row[2],'imageUrl'=>$row[3],'wholeHashtag'=>$row[4],'keyword'=>$row[5]));

	
}

echo json_encode(array("result"=>$result));

mysql_close($connect);
?>