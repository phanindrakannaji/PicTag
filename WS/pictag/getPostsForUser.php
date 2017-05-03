
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

	$query = "SELECT user_id, image_url, is_Priced, price, description,
		 created_date, last_updated_date, status, is_private, watermark_id, 
		 category, up_count, down_count 
			FROM posts
			ORDER BY created_date DESC";
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["user_id"] = $row['user_id'];
		$jsonArr["image_url"] = $row['image_url'];
    	$jsonArr["is_Priced"] = $row['is_Priced'];
    	$jsonArr["price"] = $row['price'];
    	$jsonArr["description"] = $row['description'];
    	$jsonArr["watermark_id"] = $row['watermark_id'];
    	$jsonArr["created_date"] = $row['created_date'];
    	$jsonArr["last_updated_date"] = $row['last_updated_date'];
    	$jsonArr["postStatus"] = $row['status'];
    	$jsonArr["is_private"] = $row['is_private'];
    	$jsonArr["category"] = $row['category'];
    	$jsonArr["up_count"] = $row['up_count'];
    	$jsonArr["down_count"] = $row['down_count'];
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
