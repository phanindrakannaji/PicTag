
<?php

include "dbinfo.inc";

try{
	$data = json_decode(file_get_contents('php://input'), true);
	$user_id = $data["user_id"];
	$tag_name = $data["tag_name"];

	#Connect to MySql server
	$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

	if ($mysqli->connect_errno) {
	    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
	}

	$tagQuery = "SELECT tag_id from tags where tag = '$tag_name'";
	$tagNameRes = $mysqli->query($tagQuery);
	$tag_id = 0;
	while($tagRow = $tagNameRes->fetch_assoc()){
		$tag_id = $tagRow['tag_id'];
	}

	$query = "SELECT c.post_id as post_id, c.user_id as owner_id, c.image_url as image_url, c.is_Priced as is_Priced, c.price as price, c.description as description, c.created_date as created_date, c.last_updated_date as last_updated_date, c.status as status, c.is_private as is_private, c.watermark as watermark, c.category as category, c.up_count as up_count, c.down_count as down_count, IFNULL(c.upVote, 'N') as upVote from (SELECT post_id, user_id, image_url, is_Priced, price, description, created_date, last_updated_date, status, is_private, watermark, category, up_count, down_count, (SELECT ud_flag from updownvotes where user_id = $user_id and post_id = a.post_id) as upVote FROM posts a WHERE a.post_id IN (SELECT post_id from post_tags WHERE tag_id = $tag_id) ) c ORDER BY created_date DESC";
	$res = $mysqli->query($query);
	$jsonMainArr = array();
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["post_id"] = $row['post_id'];
		$jsonArr["owner_id"] = $row['owner_id'];
		$jsonArr["image_url"] = $row['image_url'];
    	$jsonArr["is_Priced"] = $row['is_Priced'];
    	$jsonArr["price"] = $row['price'];
    	$jsonArr["description"] = $row['description'];
    	$jsonArr["watermark"] = $row['watermark'];
    	$jsonArr["created_date"] = $row['created_date'];
    	$jsonArr["last_updated_date"] = $row['last_updated_date'];
    	$jsonArr["postStatus"] = $row['status'];
    	$jsonArr["is_private"] = $row['is_private'];
    	$jsonArr["category"] = $row['category'];
    	$jsonArr["up_count"] = $row['up_count'];
    	$jsonArr["down_count"] = $row['down_count'];
    	$jsonArr["upVote"] = $row['upVote'];
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
