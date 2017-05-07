
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$userId = $data["userId"];
$picUrl = $data["picUrl"];
$isPriced = $data["isPriced"];
$price = floatval($data["price"]);
$description = $data["description"];
$isPrivate = $data["isPrivate"];
$watermarkId = $data["watermarkId"];
$category = $data["category"];

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

$query = "INSERT INTO $table(user_id, image_url, is_Priced, price, description, created_date, last_updated_date, status, is_private, watermark_id, category, up_count, down_count) VALUES($userId, '$picUrl', '$isPriced', $price, '$description', now(), now(), 'Y', '$isPrivate', '$watermarkId', '$category', 0, 0)";
//echo $query;
$result = $mysqli->query($query);
if(!$result)
{
	$error = "Register: Data was not inserted into the table " . $mysqli->error . "\n";
	$errorOccurred = true;
}

$jsonMainArr = array();
if ($errorOccurred){
	$jsonArr["status"] = "F";
	$jsonArr["errorMessage"] = $error;
	$jsonMainArr[] = $jsonArr;
} else{
	$res = $mysqli->query("SELECT post_id
	  FROM " .$table. " WHERE user_id = $userId and LOWER(image_url) = LOWER('$picUrl') ORDER BY created_date DESC");
	while ($row = $res->fetch_assoc()) {
		$jsonArr["status"] = "S";
		$jsonArr["postId"] = $row['post_id'];
	    $jsonMainArr[] = $jsonArr;
	}
}

header('Content-type: application/json');
echo json_encode($jsonMainArr);

$mysqli->close();


?>
