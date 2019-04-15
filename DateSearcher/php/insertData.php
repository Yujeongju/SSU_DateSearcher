<?php
header('Content-Type: text/html; charset=utf-8');

$connect = mysql_connect("localhost","ryunha","ghktkgksxla234"); // db와 연결

if(!$connect){
    echo 'aaa';
    die('Could not connect : '.mysql_error());
}

mysql_select_db("ryunha",$connect);	// db 설정

$sql = "create table if not exists insta_app(id int not null, likenum varchar(80),shortcode varchar(80),imageUrl varchar(200), wholeHashtag varchar(200), keyword varchar(80))";


if(!mysql_query($sql,$connect)){
    die('error :'.mysql_error());
}
mysql_query("set session character_set_keyword=utf8");

$keyword = $_POST['keyword'];
$keyword=urldecode($keyword);
$likenum = $_POST['likenum'];
$shortcode = $_POST['shortcode'];
echo 'php_shortcode :'.$shortcode;
$imageUrl = $_POST['imageUrl'];
$wholeHashtag = $_POST['wholeHashtag'];
$wholeHashtag=urldecode($wholeHashtag);

$qry="INSERT INTO insta_app (likenum ,shortcode,imageUrl,wholeHashtag, keyword) VALUE('".$likenum."','".$shortcode."','".$imageUrl."','".$wholeHashtag."','".$keyword."')";

if(!mysql_query($qry,$connect)){
    die('error :'.mysql_error());
}

mysql_close($connect);
?>
