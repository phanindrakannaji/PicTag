
<?php

include "dbinfo.inc";

$data = json_decode(file_get_contents('php://input'), true);

$latitude = floatval($data["latitude"]);
$longitude = floatval($data["longitude"]);
$email = $data["email"];

$table = "friends";

#Connect to MySql server
$mysqli = new mysqli(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_DATABASE);

if ($mysqli->connect_errno) {
    die("[ERROR] Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error);
}
$token = $mysqli->real_escape_string($token);

$query = "UPDATE $table SET latestTimestamp = now(), latitude = $latitude, longitude = $longitude WHERE LOWER(email) = LOWER('$email')";
	$result = $mysqli->query($query);
	if(!$result)
	{
		$error = "Location Update error: " . $mysqli->error;
		die($error);
	}

$res = $mysqli->query("SELECT email, fullName, latestTimestamp, latitude, longitude  FROM " .$table. " WHERE LOWER(email) = LOWER('$email') ORDER BY email ASC");
$jsonMainArr = array();
while ($row = $res->fetch_assoc()) {
    $jsonArr["email"] = $row['email'];
    $jsonArr["fullName"] = $row['fullName'];
    $jsonArr["latestTimestamp"] = $row['latestTimestamp'];
    $jsonArr["latitude"] = $row['latitude'];
    $jsonArr["longitude"] = $row['longitude'];
    $jsonMainArr[] = $jsonArr;
}

header('Content-type: application/json');
echo json_encode($jsonMainArr);

$mysqli->close();


?>
