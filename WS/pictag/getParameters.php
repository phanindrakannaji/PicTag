
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$name = $data["name"];
	$type = $data["type"];

	$table = "parameters";

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "";
	if ($type == "G"){
		$query = "SELECT name, value FROM " .$table. " 
			WHERE group_name = '$name'
			ORDER BY value ASC";
	} else if ($type == "I"){
		$query = "SELECT name, value FROM " .$table. " 
			WHERE name = '$name'";
	}
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$jsonArr["name"] = $row['name'];
    	$jsonArr["value"] = $row['value'];
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
