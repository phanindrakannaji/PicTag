
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$name = $data["id"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "SELECT a.tag_id as tag_id, b.tag as tag_name, a.notify as notify, a.minUpVotes as minUpVotes FROM user_tags a, tags b 
			WHERE a.user_id = $id
			AND a.tag_id = b.tag_id
			ORDER BY b.tag ASC";
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
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
