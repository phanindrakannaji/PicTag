
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$user_id = $data["user_id"];
$post_id = $data["post_id"];
$postOwnerId = $data["post_owner_id"];

$table = "posts";

#Connect to MySql server
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
$error = "";
$errorOccurred = false;

$query = "SELECT post_id, user_id from updownvotes WHERE post_id = $post_id and user_id = $user_id";
$result = $mysqli->query($query);
if(!$result || $result->num_rows == 0)
{
	$insertQuery = "INSERT INTO updownvotes(post_id, user_id, ud_flag, created_date) VALUES ($post_id, $user_id, 'U', now())";
	$mysqli->query($insertQuery);
	$updateUserQuery = "UPDATE users SET reputation = reputation + 1 WHERE user_id = $postOwnerId";
	$mysqli->query($updateUserQuery);
	$updatePostQuery = "UPDATE posts SET up_count = up_count + 1 WHERE post_id = $post_id";
	$mysqli->query($updatePostQuery);
} else{
	$deleteQuery = "DELETE FROM updownvotes WHERE post_id = $post_id and user_id = $user_id";
	$mysqli->query($deleteQuery);
	$updateUserQuery = "UPDATE users SET reputation = reputation - 1 WHERE user_id = $postOwnerId";
	$mysqli->query($updateUserQuery);
	$updatePostQuery = "UPDATE posts SET up_count = up_count - 1 WHERE post_id = $post_id";
	$mysqli->query($updatePostQuery);
}


header('Content-type: application/json');
echo json_encode($jsonMainArr);

$mysqli->close();
?>
