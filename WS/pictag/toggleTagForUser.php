
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$user_id = $data["user_id"];
	$tag_id = $data["tag_id"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "SELECT user_id, tag_id FROM user_tags WHERE user_id = $user_id AND tag_id = $tag_id";	
	$res = $mysqli->query($query);
	if (!$res || $res->num_rows == 0){
		$insertQuery = "INSERT INTO user_tags(user_id, tag_id, notify, minUpVotes, created_date) VALUES($user_id, $tag_id, 'N', 0, now())";
		$mysqli->query($insertQuery);
		echo "Tag added to your list";
	} else{
		$deleteQuery = "DELETE FROM user_tags WHERE user_id = $user_id AND tag_id = $tag_id";
		$mysqli->query($deleteQuery);
		echo "Tag removed";
	}
	
} catch (Exception $e) {
	echo 'Caught exception: ',  $e->getMessage(), "\n";
} finally{
	$mysqli->close();
}

?>
