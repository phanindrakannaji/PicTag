
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$user_id = $data["user_id"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$query = "SELECT a.tag_id as tag_id, b.tag as tag FROM user_tags a, tags b WHERE a.user_id = $user_id AND a.tag_id = b.tag_id ORDER BY tag";
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$tagId = $row['tag_id'];
		$tagName = $row['tag'];
		$innerJsonArray = array();
		$innerQuery = "SELECT DISTINCT image_url from posts a, post_tags b where a.post_id = b.post_id and b.tag_id = $tagId ORDER BY created_date desc";
		$innerres = $mysqli->query($innerQuery);
		while ($innerrow = $innerres->fetch_assoc()) {
			$innerJson["tag_id"] = $tagId;
			$innerJson["image_url"] = $innerrow['image_url'];
			$innerJsonArray[] = $innerJson;
		}
		$jsonMain["tag_name"] = $tagName;
		$jsonMain["images_list"] = $innerJsonArray;
		$jsonMainArr[] = $jsonMain;
	}
	
	header('Content-type: application/json');
	echo json_encode($jsonMainArr);

} catch (Exception $e) {
	echo 'Caught exception: ',  $e->getMessage(), "\n";
} finally{
	$mysqli->close();
}

?>
