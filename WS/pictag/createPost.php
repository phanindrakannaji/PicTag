
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$userId = $data["userId"];
$picUrl = $data["picUrl"];
$isPriced = $data["isPriced"];
$price = floatval($data["price"]);
$description = $data["description"];
$isPrivate = $data["isPrivate"];
$watermark = $data["watermark"];
$category = $data["category"];
$tagsJson = $data["tags"];
$tagsJson = str_replace("[", "", $tagsJson);
$tagsJson = str_replace("]", "", $tagsJson);
$tags = array();
$tags = explode(",", $tagsJson);

$table = "posts";

#Connect to MySql server
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
$email = $mysqli->real_escape_string($email);
$picUrl = $mysqli->real_escape_string($picUrl);

$error = "";
$errorOccurred = false;

$query = "INSERT INTO $table(user_id, image_url, is_Priced, price, description, created_date, last_updated_date, status, is_private, watermark, category, up_count, down_count) VALUES($userId, '$picUrl', '$isPriced', $price, '$description', now(), now(), 'Y', '$isPrivate', '$watermark', '$category', 0, 0)";
$result = $mysqli->query($query);
if(!$result)
{
	$error = "CreatePost: Data was not inserted into the table " . $mysqli->error . "\n";
	$errorOccurred = true;
}
$postIdForTags = 0;

$jsonMainArr = array();
if ($errorOccurred){
	$jsonArr["status"] = "F";
	$jsonArr["errorMessage"] = $error;
	$jsonMainArr[] = $jsonArr;
} else{
	$res = $mysqli->query("SELECT post_id
	  FROM " .$table. " WHERE user_id = $userId and LOWER(image_url) = LOWER('$picUrl') and price = $price and description = '$description' ORDER BY created_date DESC");
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["postId"] = $row['post_id'];
		$postIdForTags = $row['post_id'];
	    $jsonMainArr[] = $jsonArr;
	}
}

if (count($tags) > 0){
	foreach ($tags as $tag) {
		$tag = str_replace(" ", "", $tag);
		$query1 = "SELECT tag_id from tags where LOWER(tag) = LOWER('$tag')";
		$res1 = $mysqli->query($query1);
		if(!$res1 || $res1->num_rows == 0){
			$query2 = "INSERT INTO tags (tag, created_date, last_updated_date, last_alerted_date) VALUES (LOWER('$tag'), now(), now(), now())";
			
			$res2 = $mysqli->query($query2);
			if(!$res2){
				$error = "CreateTag: Data was not inserted into the table " . $mysqli->error . "\n";
				$errorOccurred = true;
			}
		} 
		$query3 = "SELECT tag_id from tags where LOWER(tag) = LOWER('$tag')";
		$res3 = $mysqli->query($query3);
		if(!$res3 || $res3->num_rows == 0){
			$error = "CreatePostTag: Data was not inserted into the table " . $mysqli->error . "\n";
			$errorOccurred = true;
		} else{
			while ($row3 = $res3->fetch_assoc()){
				$tag_id = $row3['tag_id'];
				$query4 = "INSERT INTO post_tags(post_id, tag_id, type) VALUES ($postIdForTags, $tag_id, 'A')";
				$res4 = $mysqli->query($query4);
				if(!$res4)
				{
					$error = "CreatePostTag: Data was not inserted into the table " . $mysqli->error . "\n";
					$errorOccurred = true;
				}
			}
		}
	}
}

header('Content-type: application/json');
echo json_encode($jsonMainArr);

$mysqli->close();
?>
