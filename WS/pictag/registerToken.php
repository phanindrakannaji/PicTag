
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$token = $data["token"];
$email = $data["email"];
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);
$table = "friends";

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}

$res = $mysqli->query("UPDATE ". $table ." SET token = '".$token."' WHERE LOWER(email) = LOWER('$email')");
if (!$res){
	die("[ERROR] Failed to update Token: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}


?>
