
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$search_term = $data["search_term"];
	$user_id = $data["user_id"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
		die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "(SELECT DISTINCT a.tag_id as tag_id, a.tag as tag, 'true' as 'isSelected' from tags a, user_tags b WHERE a.tag_id = b.tag_id and b.user_id=$user_id ";

if ($search_term != ""){
	$query = $query . " and a.tag LIKE '%$search_term%' ";
}
	$query = $query . " ) UNION (select a.tag_id as tag_id, a.tag as tag, 'false' as 'isSelected' from tags a WHERE ";
if ($search_term != ""){
	$query = $query . " a.tag LIKE '%$search_term%' and ";
}
	$query = $query . " a.tag_id NOT IN (Select tag_id from user_tags where user_id = $user_id)) ";
	
	$query = $query . " ORDER BY tag ASC";
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["tag_id"] = $row['tag_id'];
		$jsonArr["tag_name"] = $row['tag'];
		$jsonArr["isSelected"] = $row['isSelected'];
		$jsonMainArr[] = $jsonArr;
	}
	
	header('Content-type: application/json');
	echo json_encode($jsonMainArr);

} catch (Exception $e) {
	echo 'Caught exception: ',  $e->getMessage(), "\n";
} finally{
	$mysqli->close();
}

?>
