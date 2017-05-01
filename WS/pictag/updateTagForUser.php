
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$user_id = $data["user_id"];
	$tag_id = $data["tag_id"];
	$notify = $data["notify"];
	$minUpVotes = $data["minUpVotes"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "UPDATE user_tags SET notify = '$notify', minUpVotes = '$minUpVotes'
			WHERE user_id = $user_id AND tag_id = $tag_id";
	$res = $mysqli->query($query);
	if (!$res){
		echo "Error:" . $mysqli->error . "\n";
	} else{
		echo "Successful";
	}

} catch (Exception $e) {
	echo 'Caught exception: ',  $e->getMessage(), "\n";
} finally{
	$mysqli->close();
}

?>
