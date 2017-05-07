
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$search_term = $data["search_term"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
		die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "SELECT tag_id, tag FROM tags";
	if ($search_term != ""){
		$query = $query . " WHERE tag LIKE '%$search_term%'";
	}
	$query = $query . " ORDER BY tag ASC";
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["user_id"] = $row['user_id'];
		$jsonArr["tag_id"] = $row['tag_id'];
		$jsonArr["tag_name"] = $row['tag_name'];
		$jsonArr["notify"] = $row['notify'];
		$jsonArr["minUpVotes"] = $row['minUpVotes'];
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
